package com.stignit.app.detection

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.util.Log
import java.util.Locale
import kotlin.math.sqrt

/** Confidence tier for a detected candidate event, ascending in certainty. */
enum class DetectionConfidence { LOW, MEDIUM, HIGH }

/**
 * Multi-stage crash-detection pipeline over the accelerometer + gyroscope.
 *
 * A single "acceleration > X" threshold fires on dropped phones, hard braking and
 * potholes just as readily as on a real collision. This engine instead scores a
 * candidate on how many *independent* signals agree in a narrow time window:
 *
 *  Stage 1  accelerometer gate  — net (gravity-removed) acceleration averages
 *                                 >= [ACCEL_GATE_G] over a [SUSTAIN_WINDOW_MS] window.
 *                                 On its own this is only logged, never escalated.
 *  Stage 2  gyroscope corroboration — peak rotation >= [GYRO_GATE_DEG_PER_SEC] within
 *                                 ~[POST_EVENT_WINDOW_MS] either side of the spike.
 *                                 An accel spike with little rotation reads as a
 *                                 drop / hard brake, not a crash.
 *  Stage 3  speed delta (optional) — a >= [SPEED_DROP_GATE_KMH] drop inside 1 s,
 *                                 only when a ground-speed source is supplied.
 *
 *  Scoring: accel only -> LOW (log only), accel + gyro -> MEDIUM,
 *           accel + gyro + speed -> HIGH.
 *  After any MEDIUM/HIGH the pipeline is muted for [COOLDOWN_MS] — the device is
 *  still settling from the same event.
 *
 * Knows nothing about notifications, the UI or the welfare-check flow; it only
 * calls [onCandidateEvent]. **That callback is invoked on an internal background
 * thread** — hop to wherever you need before touching UI.
 *
 * The thresholds here are first-pass estimates, meant to be tuned against real
 * logcat traces rather than trusted as-is.
 */
class SensorFusionEngine(
    private val sensorManager: SensorManager,
    private val onCandidateEvent: (DetectionConfidence) -> Unit,
    /**
     * Optional current ground speed in m/s (e.g. from FusedLocation), or null when
     * unknown. Polled on a short interval; detection never blocks on it.
     */
    private val currentSpeedMps: (() -> Float?)? = null,
) : SensorEventListener {

    private data class Sample(val tNanos: Long, val value: Float)

    private class PendingCandidate(
        var accelPeakG: Float,
        val accelMeanG: Float,
        var gyroPeakDegPerSec: Float,
    )

    private var sensorThread: HandlerThread? = null
    private var handler: Handler? = null
    @Volatile private var running = false
    private var hasGyroscope = false

    // Low-pass-tracked gravity vector so linear acceleration can be recovered at
    // any device orientation. Seeded from the first sample.
    private val gravity = FloatArray(3)
    private var gravitySeeded = false

    private val accelWindow = ArrayDeque<Sample>()   // net accel magnitude, g
    private val gyroWindow = ArrayDeque<Sample>()     // rotation magnitude, deg/s
    private val speedWindow = ArrayDeque<Sample>()    // ground speed, m/s

    private var pending: PendingCandidate? = null
    private var cooldownUntilNanos = 0L
    private var lastLowEmitNanos = 0L

    private val speedPoller = object : Runnable {
        override fun run() {
            sampleSpeed()
            handler?.postDelayed(this, SPEED_POLL_MS)
        }
    }

    fun start() {
        if (running) return

        // Reset all pipeline state here (not in stop) so it only ever mutates on
        // the caller's thread, before the sensor HandlerThread exists.
        pending = null
        gravitySeeded = false
        cooldownUntilNanos = 0L
        lastLowEmitNanos = 0L
        accelWindow.clear()
        gyroWindow.clear()
        speedWindow.clear()

        val accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        if (accelerometer == null) {
            Log.w(TAG, "No accelerometer on this device — crash detection cannot run")
            return
        }
        val gyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        hasGyroscope = gyroscope != null
        if (!hasGyroscope) {
            Log.w(TAG, "No gyroscope on this device — detection is limited to LOW confidence")
        }

        val thread = HandlerThread("stignit-sensor-fusion").apply { start() }
        val h = Handler(thread.looper)
        sensorThread = thread
        handler = h

        sensorManager.registerListener(this, accelerometer, SAMPLING_PERIOD_US, h)
        if (gyroscope != null) {
            sensorManager.registerListener(this, gyroscope, SAMPLING_PERIOD_US, h)
        }
        if (currentSpeedMps != null) h.postDelayed(speedPoller, SPEED_POLL_MS)

        running = true
        Log.d(TAG, "Started (gyroscope=$hasGyroscope, speedSource=${currentSpeedMps != null})")
    }

    fun stop() {
        if (!running) return
        running = false

        // unregister first so no more callbacks land, then tear down the thread.
        sensorManager.unregisterListener(this)
        handler?.removeCallbacksAndMessages(null)
        sensorThread?.quitSafely()
        sensorThread = null
        handler = null
        Log.d(TAG, "Stopped")
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (!running) return
        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> onAccelerometer(event)
            Sensor.TYPE_GYROSCOPE -> onGyroscope(event)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit

    // --- Stage 1 — accelerometer gate ---------------------------------------

    private fun onAccelerometer(event: SensorEvent) {
        val now = SystemClock.elapsedRealtimeNanos()
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]

        if (!gravitySeeded) {
            gravity[0] = x; gravity[1] = y; gravity[2] = z
            gravitySeeded = true
        } else {
            gravity[0] = GRAVITY_LP_ALPHA * gravity[0] + (1 - GRAVITY_LP_ALPHA) * x
            gravity[1] = GRAVITY_LP_ALPHA * gravity[1] + (1 - GRAVITY_LP_ALPHA) * y
            gravity[2] = GRAVITY_LP_ALPHA * gravity[2] + (1 - GRAVITY_LP_ALPHA) * z
        }

        val lx = x - gravity[0]
        val ly = y - gravity[1]
        val lz = z - gravity[2]
        val netG = sqrt(lx * lx + ly * ly + lz * lz) / SensorManager.GRAVITY_EARTH

        accelWindow.addLast(Sample(now, netG))
        evictOlderThan(accelWindow, now - ACCEL_WINDOW_MS.msToNanos())

        // Keep feeding an in-flight candidate its running accel peak.
        pending?.let { if (netG > it.accelPeakG) it.accelPeakG = netG }

        if (pending != null || now < cooldownUntilNanos) return

        var sum = 0f
        var count = 0
        val from = now - SUSTAIN_WINDOW_MS.msToNanos()
        for (s in accelWindow) {
            if (s.tNanos >= from) {
                sum += s.value
                count++
            }
        }
        if (count < MIN_SUSTAIN_SAMPLES) return

        val meanG = sum / count
        if (meanG >= ACCEL_GATE_G) tripStage1(meanG)
    }

    private fun tripStage1(meanG: Float) {
        val peakG = accelWindow.maxOf { it.value }
        pending = PendingCandidate(
            accelPeakG = peakG,
            accelMeanG = meanG,
            gyroPeakDegPerSec = peakGyroSince(
                SystemClock.elapsedRealtimeNanos() - POST_EVENT_WINDOW_MS.msToNanos()
            ),
        )
        Log.d(
            TAG,
            fmt(
                "Stage 1: accel gate tripped (mean=%.2fg / %dms, peak=%.2fg) — " +
                    "holding %dms for the gyro window",
                meanG, SUSTAIN_WINDOW_MS, peakG, POST_EVENT_WINDOW_MS,
            ),
        )
        handler?.postDelayed(::finalizeCandidate, POST_EVENT_WINDOW_MS)
    }

    // --- Stage 2 — gyroscope corroboration --------------------------------

    private fun onGyroscope(event: SensorEvent) {
        val now = SystemClock.elapsedRealtimeNanos()
        val x = event.values[0]
        val y = event.values[1]
        val z = event.values[2]
        val degPerSec = Math.toDegrees(sqrt(x * x + y * y + z * z).toDouble()).toFloat()

        gyroWindow.addLast(Sample(now, degPerSec))
        evictOlderThan(gyroWindow, now - GYRO_WINDOW_MS.msToNanos())

        pending?.let { if (degPerSec > it.gyroPeakDegPerSec) it.gyroPeakDegPerSec = degPerSec }
    }

    private fun peakGyroSince(fromNanos: Long): Float {
        var peak = 0f
        for (s in gyroWindow) if (s.tNanos >= fromNanos && s.value > peak) peak = s.value
        return peak
    }

    // --- Stage 3 — speed delta (dormant until a speed source is supplied) --

    private fun sampleSpeed() {
        val speed = currentSpeedMps?.invoke() ?: return
        val now = SystemClock.elapsedRealtimeNanos()
        speedWindow.addLast(Sample(now, speed))
        evictOlderThan(speedWindow, now - SPEED_WINDOW_MS.msToNanos())
    }

    /** Largest speed drop (km/h) across any span of <= 1 s in recent history. */
    private fun maxRecentSpeedDropKmh(): Float {
        if (speedWindow.size < 2) return 0f
        val samples = speedWindow.toList()
        var maxDrop = 0f
        for (i in samples.indices) {
            for (j in i + 1 until samples.size) {
                if (samples[j].tNanos - samples[i].tNanos > SPEED_DELTA_SPAN_MS.msToNanos()) break
                val drop = (samples[i].value - samples[j].value) * MPS_TO_KMH
                if (drop > maxDrop) maxDrop = drop
            }
        }
        return maxDrop
    }

    // --- Scoring ----------------------------------------------------------

    private fun finalizeCandidate() {
        val c = pending ?: return
        pending = null

        val gyroCorroborated = hasGyroscope && c.gyroPeakDegPerSec >= GYRO_GATE_DEG_PER_SEC
        val speedDropKmh = maxRecentSpeedDropKmh()
        val speedCorroborated = speedDropKmh >= SPEED_DROP_GATE_KMH

        // TODO(detection): gyro corroboration is mandatory for MEDIUM/HIGH, so a
        //  phone with no gyroscope is permanently capped at LOW under this design.
        //  A speed-delta-only fallback (Stage 3 standing in for Stage 2 when
        //  hasGyroscope == false) is a deliberately deferred decision, not an
        //  oversight — revisit once Stage 3 has a real speed source.
        val confidence = when {
            gyroCorroborated && speedCorroborated -> DetectionConfidence.HIGH
            gyroCorroborated -> DetectionConfidence.MEDIUM
            else -> DetectionConfidence.LOW
        }

        // Repeated LOW candidates from one shaky event would spam the log; collapse them.
        val now = SystemClock.elapsedRealtimeNanos()
        if (confidence == DetectionConfidence.LOW) {
            if (now - lastLowEmitNanos < LOW_EMIT_DEBOUNCE_MS.msToNanos()) return
            lastLowEmitNanos = now
        }

        val readings = fmt(
            "accelMean=%.2fg accelPeak=%.2fg gyroPeak=%.0f deg/s speedDrop=%s",
            c.accelMeanG,
            c.accelPeakG,
            c.gyroPeakDegPerSec,
            if (currentSpeedMps == null) "n/a" else fmt("%.0fkm/h", speedDropKmh),
        )

        when (confidence) {
            DetectionConfidence.LOW ->
                Log.d(TAG, "Candidate: LOW — accel spike, no rotation (drop / hard brake?). $readings")
            DetectionConfidence.MEDIUM ->
                Log.d(TAG, "Candidate: MEDIUM — accel + rotation agree. $readings")
            DetectionConfidence.HIGH ->
                Log.d(TAG, "Candidate: HIGH — accel + rotation + speed drop agree. $readings")
        }

        if (confidence != DetectionConfidence.LOW) {
            cooldownUntilNanos = now + COOLDOWN_MS.msToNanos()
            Log.d(TAG, "Cooldown: detection muted for ${COOLDOWN_MS}ms")
        }

        onCandidateEvent(confidence)
    }

    // --- helpers --------------------------------------------------------------

    private fun evictOlderThan(window: ArrayDeque<Sample>, cutoffNanos: Long) {
        while (window.isNotEmpty() && window.first().tNanos < cutoffNanos) window.removeFirst()
    }

    private fun Long.msToNanos() = this * 1_000_000L

    private fun fmt(pattern: String, vararg args: Any?) = String.format(Locale.US, pattern, *args)

    companion object {
        private const val TAG = "StignIt/SensorFusion"

        /**
         * 100 Hz target. Android treats it as a hint — real devices deliver
         * 100-500 Hz for the accelerometer, emulators often only ~50 Hz. Android
         * 12+ caps delivery at 200 Hz unless the app holds HIGH_SAMPLING_RATE_SENSORS;
         * 100 Hz stays under that, so no extra permission is needed. Stage 1 averages
         * a window precisely because the effective rate is not guaranteed.
         */
        private const val SAMPLING_PERIOD_US = 10_000

        // Stage 1 — accelerometer gate. First-pass values; tune from logcat.
        //
        // GRAVITY_LP_ALPHA: how slowly the gravity estimate adapts. 0.92 keeps the
        // pre-impact baseline steady through a ~100-150ms collision pulse instead of
        // chasing it (which at 0.8 both ate into the impact signal and produced a
        // rebound spike as the force ended). Trade-off: slower to settle after a
        // genuine orientation change, which is fine — orientation changes aren't
        // time-critical, impacts are.
        private const val GRAVITY_LP_ALPHA = 0.92f
        private const val ACCEL_GATE_G = 3.5f
        private const val SUSTAIN_WINDOW_MS = 50L
        private const val MIN_SUSTAIN_SAMPLES = 2
        private const val ACCEL_WINDOW_MS = 400L

        // Stage 2 — gyroscope corroboration.
        private const val GYRO_GATE_DEG_PER_SEC = 300f
        private const val POST_EVENT_WINDOW_MS = 200L
        private const val GYRO_WINDOW_MS = 500L

        // Stage 3 — speed delta (only when a ground-speed source is supplied).
        private const val SPEED_DROP_GATE_KMH = 25f
        private const val SPEED_DELTA_SPAN_MS = 1_000L
        private const val SPEED_POLL_MS = 250L
        private const val SPEED_WINDOW_MS = 3_000L
        private const val MPS_TO_KMH = 3.6f

        private const val COOLDOWN_MS = 12_000L
        private const val LOW_EMIT_DEBOUNCE_MS = 3_000L
    }
}

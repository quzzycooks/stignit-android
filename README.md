# StignIt — Native Android

Fresh Kotlin + Jetpack Compose project. No Capacitor, no Flutter — built
this way because crash detection has to run always-on in the background,
which needs real foreground-service + Doze-survival control that only a
native app gets reliably.

## Opening this project
1. Android Studio (Koala or newer) → **Open** → select this folder.
2. Let Gradle sync. First sync will download the Android Gradle Plugin,
   Kotlin, and Compose dependencies — needs network access to
   `dl.google.com` / `repo.maven.apache.org` (not available in the
   sandbox this was scaffolded in, so this hasn't been built/run yet —
   do a sync + run on your machine as the first step).
3. Run on a device or emulator running API 26+.

## What's ported so far
All 8 screens from the Lovable UI (github.com/quzzycooks/stignit-guard-nigeria)
are converted to Compose and wired together with real navigation:
onboarding → auth → home → welfare-check / situation-room / contacts /
safety / welfare-history. Colors, spacing, and component variants (button
styles, status pills, panels) are ported 1:1 from the web app's design
tokens, not approximated.

Auth flow (phone → OTP → profile → home) is built and wired against a
Retrofit client (`data/net/`, `AuthRepository`, `SessionStore`):
`/v1/auth/otp/request` → `/v1/auth/otp/verify` → `/v1/users/register`,
bearer token persisted between steps, session skips auth on relaunch.
Base URL is `BuildConfig.API_BASE_URL`, set per build type in
`app/build.gradle.kts` — both debug and release point at the deployed
backend `https://stgv3-production.up.railway.app`.

Still open:
- Plus Jakarta Sans isn't bundled — see the note in `Theme.kt`
- "Monitoring" switch on Home is local state only, not yet wired to the
  real `CrashDetectionService` from milestone 1
- Register form: the fixed "Finish setup" footer overlaps the last field
  when scrolled to the bottom — needs extra bottom padding on the scroll
  content
- OTP verify + register only exercised against a mock so far; the live
  backend runs in production mode (no `devCode` in the response) so the
  full flow needs a real SMS-capable Nigerian number to test

## Roadmap (build order)
1. **Foreground service skeleton** ✅
2. **UI screens ported from Lovable** ✅
3. **Sensor fusion core** ✅ ← you are here — `detection/SensorFusionEngine.kt`,
   a 3-stage confidence pipeline (accel gate → gyro corroboration → optional
   speed delta), started/stopped by `CrashDetectionService`. Detection is
   log-only for now: `adb logcat -s StignIt/SensorFusion StignIt/CrashService`.
   Thresholds are first-pass — tune from real logcat traces (drop / shake /
   hard-brake on a device, or emulator Extended Controls → Virtual sensors).
4. **Wire UI to the real service** — Home's monitoring switch actually
   starts/stops detection; route MEDIUM/HIGH candidates to the welfare-check
   screen (currently just a Log.d in `CrashDetectionService.onDetectionCandidate`);
   welfare-check countdown triggers from a real sensor event instead of a
   fixed 30s timer
5. **Backend wiring** — Retrofit client against `stignit-api`, hook auth
   (email/phone OTP) and incident creation on escalate
   - client points at the deployed backend (`stgv3-production.up.railway.app`);
     wire models verified 1:1 against the NestJS DTOs
   - incident creation on escalate still to do

## Package layout
```
com.stignit.app/
├── detection/     sensor fusion (not yet built), foreground service
├── ui/
│   ├── theme/         colors, typography — ported from styles.css
│   ├── components/    Screen, Panel, StignItButton, NavTile, BottomNav, etc.
│   ├── onboarding/    Screen 1
│   ├── auth/          Screen 2
│   ├── home/          Screen 3
│   ├── welfare/        Screen 4 (welfare-check)
│   ├── situationroom/ Screen 5
│   ├── contacts/      Screen 6
│   ├── safety/        Screen 7
│   ├── welfarehistory/ Screen 8
│   └── nav/           StignItNavHost — wires all 8 together
└── MainActivity       hosts StignItNavHost
```

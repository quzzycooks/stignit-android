package com.stignit.app.ui.theme

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.stignit.app.R

// Matches the light-mode palette from styles.css. Dark mode isn't in the
// Lovable source yet, so this theme is light-only for now — add a dark
// ColorScheme here if/when the web design gets one.
private val StignitColors = lightColorScheme(
    background = Background,
    onBackground = Foreground,
    surface = CardColor,
    onSurface = Foreground,
    primary = Primary,
    onPrimary = PrimaryForeground,
    secondary = Secondary,
    onSecondary = SecondaryForeground,
    tertiary = Accent,
    error = Destructive,
    onError = DestructiveForeground,
    outline = Border,
)

// Design tokens that don't map onto Material3's ColorScheme (safe/danger/
// warning + soft variants) are read directly from Color.kt at call sites
// via StignItTheme.colors, defined below.
object StignItExtraColors {
    val danger = Danger
    val dangerForeground = DangerForeground
    val dangerSoft = DangerSoft
    val safe = Safe
    val safeForeground = SafeForeground
    val safeSoft = SafeSoft
    val warning = Warning
    val warningSoft = WarningSoft
    val muted = Muted
    val mutedForeground = MutedForeground
}

// Plus Jakarta Sans — the web app's display/body face (styles.css --font-sans).
// Static latin instances live under res/font/. Weights map: Normal 400,
// Medium 500, SemiBold 600, Bold 700.
val PlusJakartaSans = FontFamily(
    Font(R.font.plus_jakarta_sans_regular, FontWeight.Normal),
    Font(R.font.plus_jakarta_sans_medium, FontWeight.Medium),
    Font(R.font.plus_jakarta_sans_semibold, FontWeight.SemiBold),
    Font(R.font.plus_jakarta_sans_bold, FontWeight.Bold),
)

private val StignitTypography = Typography(
    headlineLarge = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 32.sp, lineHeight = 37.sp),
    headlineMedium = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 28.sp),
    titleLarge = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 18.sp, lineHeight = 24.sp),
    bodyLarge = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Normal, fontSize = 16.sp, lineHeight = 24.sp),
    bodyMedium = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.Normal, fontSize = 14.sp, lineHeight = 20.sp),
    labelLarge = TextStyle(fontFamily = PlusJakartaSans, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, lineHeight = 20.sp),
)

@Composable
fun StignItTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = StignitColors,
        typography = StignitTypography,
    ) {
        // Screens use bare Text(...) with explicit size/weight but no style, which
        // otherwise falls back to FontFamily.Default. Seed the default text style
        // so the whole tree inherits Plus Jakarta Sans.
        CompositionLocalProvider(
            LocalTextStyle provides LocalTextStyle.current.copy(fontFamily = PlusJakartaSans),
            content = content,
        )
    }
}

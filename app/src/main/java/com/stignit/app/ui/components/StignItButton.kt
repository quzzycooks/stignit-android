package com.stignit.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stignit.app.ui.theme.StignItExtraColors

/** Mirrors buttonVariants() in components/ui/button.tsx. */
enum class StignItButtonVariant { Default, Outline, Safe, Sos }

enum class StignItButtonSize(val height: Int, val cornerRadius: Int, val fontSize: Int) {
    Default(48, 12, 16),
    Large(56, 16, 18),
    ExtraLarge(64, 16, 20), // matches the h-16 "xl" size used for SOS/welfare-check actions
}

@Composable
fun StignItButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: StignItButtonVariant = StignItButtonVariant.Default,
    size: StignItButtonSize = StignItButtonSize.Default,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val colors = when (variant) {
        StignItButtonVariant.Default -> ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
        )
        StignItButtonVariant.Outline -> ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
        StignItButtonVariant.Safe -> ButtonDefaults.buttonColors(
            containerColor = StignItExtraColors.safe,
            contentColor = StignItExtraColors.safeForeground,
        )
        StignItButtonVariant.Sos -> ButtonDefaults.buttonColors(
            containerColor = StignItExtraColors.danger,
            contentColor = StignItExtraColors.dangerForeground,
        )
    }

    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(size.height.dp),
        colors = colors,
        shape = RoundedCornerShape(size.cornerRadius.dp),
        contentPadding = PaddingValues(horizontal = 20.dp),
    ) {
        leadingIcon?.let {
            it()
            androidx.compose.foundation.layout.Spacer(Modifier)
        }
        Text(text, fontSize = size.fontSize.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold)
    }
}

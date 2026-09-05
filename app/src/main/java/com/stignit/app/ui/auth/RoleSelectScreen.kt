package com.stignit.app.ui.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stignit.app.data.AccountRole
import com.stignit.app.ui.components.Panel
import com.stignit.app.ui.components.Screen
import com.stignit.app.ui.components.TopBar
import com.stignit.app.ui.components.clickableNoRipple
import com.stignit.app.ui.theme.StignItExtraColors

private data class RoleOption(
    val role: AccountRole,
    val icon: ImageVector,
    val title: String,
    val detail: String,
)

private val options = listOf(
    RoleOption(
        AccountRole.CIVILIAN,
        Icons.Filled.Person,
        "Civilian",
        "You want StignIt watching over you and sharing your welfare status with people you trust.",
    ),
    RoleOption(
        AccountRole.MEDICAL_PERSONNEL,
        Icons.Filled.LocalHospital,
        "Medical Personnel",
        "You're a clinician or paramedic who may respond to incidents in a professional capacity.",
    ),
    RoleOption(
        AccountRole.DRIVER_RESPONDER,
        Icons.Filled.DirectionsCar,
        "Driver / Responder",
        "You drive an ambulance or response vehicle and can be dispatched to incidents.",
    ),
)

/** Inserted after OTP verify, before the role-specific profile step. */
@Composable
fun RoleSelectScreen(
    onRoleSelected: (AccountRole) -> Unit,
    onBack: () -> Unit,
) {
    Screen(scrollable = false) {
        TopBar(onBack = onBack)

        Text(
            "How will you use StignIt?",
            modifier = Modifier.padding(top = 8.dp),
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            lineHeight = 34.sp,
        )
        Text(
            "This decides what your profile collects. You can't switch roles later without support.",
            modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            fontSize = 16.sp,
            color = StignItExtraColors.mutedForeground,
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            options.forEach { option ->
                Panel(modifier = Modifier.clickableNoRipple { onRoleSelected(option.role) }) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .background(MaterialTheme.colorScheme.secondary, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center,
                        ) {
                            Icon(option.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(24.dp))
                        }
                        Column {
                            Text(option.title, fontSize = 17.sp, fontWeight = FontWeight.SemiBold)
                            Text(
                                option.detail,
                                modifier = Modifier.padding(top = 4.dp),
                                fontSize = 14.sp,
                                lineHeight = 19.sp,
                                color = StignItExtraColors.mutedForeground,
                            )
                        }
                    }
                }
            }
        }
    }
}

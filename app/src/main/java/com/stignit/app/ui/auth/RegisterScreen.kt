package com.stignit.app.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stignit.app.data.AccountRole
import com.stignit.app.data.ApiResult
import com.stignit.app.data.net.EmergencyContactBody
import com.stignit.app.data.net.MedicalPersonnelProfileBody
import com.stignit.app.data.net.ResponderProfileBody
import com.stignit.app.data.rememberAuthRepository
import com.stignit.app.ui.components.Screen
import com.stignit.app.ui.components.clickableNoRipple
import com.stignit.app.ui.components.SectionTitle
import com.stignit.app.ui.components.StignItButton
import com.stignit.app.ui.components.StignItButtonSize
import com.stignit.app.ui.components.TopBar
import com.stignit.app.ui.theme.StignItExtraColors
import kotlinx.coroutines.launch

private val equipmentOptions = listOf(
    "defibrillator" to "Defibrillator",
    "oxygen" to "Oxygen",
    "trauma_kit" to "Trauma kit",
    "neonatal" to "Neonatal",
)

private class ContactDraft {
    var name by mutableStateOf("")
    var phone by mutableStateOf("")
    var relationship by mutableStateOf("")

    val isComplete: Boolean
        get() = name.isNotBlank() && phone.trim().length >= 10 && relationship.isNotBlank()

    fun toBody(priority: Int) =
        EmergencyContactBody(name.trim(), phone.trim(), relationship.trim(), priority)
}

/**
 * Profile completion for a phone-verified account (PRD 6.1.1 /users/register).
 * Backend requires 2–4 emergency contacts and rejects users under 16.
 */
@Composable
fun RegisterScreen(
    role: AccountRole,
    onRegistered: () -> Unit,
    onBack: () -> Unit,
) {
    val repo = rememberAuthRepository()
    val scope = rememberCoroutineScope()

    var fullName by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") } // YYYY-MM-DD
    var stateLga by remember { mutableStateOf("") }
    val contacts = remember { mutableStateListOf(ContactDraft(), ContactDraft()) }

    var licenseNumber by remember { mutableStateOf("") }
    var medicalAffiliation by remember { mutableStateOf("") }

    var vehicleType by remember { mutableStateOf("") }
    var responderAffiliation by remember { mutableStateOf("") }
    val selectedEquipment = remember { mutableStateListOf<String>() }
    var otherEquipment by remember { mutableStateOf("") }

    var error by remember { mutableStateOf<String?>(null) }
    var submitting by remember { mutableStateOf(false) }

    val dobValid = Regex("""\d{4}-\d{2}-\d{2}""").matches(dob)
    val roleFieldsValid = when (role) {
        AccountRole.CIVILIAN -> true
        AccountRole.MEDICAL_PERSONNEL -> licenseNumber.isNotBlank() && medicalAffiliation.isNotBlank()
        AccountRole.DRIVER_RESPONDER -> vehicleType.isNotBlank() || responderAffiliation.isNotBlank()
    }
    val canSubmit = fullName.isNotBlank() && dobValid && stateLga.isNotBlank() &&
        contacts.count { it.isComplete } >= 2 && roleFieldsValid && !submitting

    fun submit() {
        if (!canSubmit) return
        submitting = true
        error = null
        scope.launch {
            val bodies = contacts.filter { it.isComplete }
                .mapIndexed { i, c -> c.toBody(i + 1) }
            val medicalProfile = if (role == AccountRole.MEDICAL_PERSONNEL) {
                MedicalPersonnelProfileBody(licenseNumber.trim(), medicalAffiliation.trim())
            } else null
            val responderProfile = if (role == AccountRole.DRIVER_RESPONDER) {
                val equipment = selectedEquipment.toList() + listOfNotNull(otherEquipment.trim().ifBlank { null })
                ResponderProfileBody(
                    vehicleType = vehicleType.trim().ifBlank { null },
                    affiliation = responderAffiliation.trim().ifBlank { null },
                    equipment = equipment,
                )
            } else null
            when (val r = repo.register(fullName, dob, stateLga, bodies, role, medicalProfile, responderProfile)) {
                is ApiResult.Ok -> onRegistered()
                is ApiResult.Err -> {
                    error = r.message
                    submitting = false
                }
            }
        }
    }

    Screen(modifier = Modifier.imePadding(), scrollable = false) {
        TopBar(onBack = onBack)

        Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState())) {
            Text("Set up your safety profile", fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 34.sp)
            Text(
                "Responders and your contacts use this the moment a welfare check goes unanswered.",
                modifier = Modifier.padding(top = 8.dp),
                fontSize = 16.sp,
                color = StignItExtraColors.mutedForeground,
            )

            Spacer(Modifier.height(24.dp))

            Field("Full name", fullName, { fullName = it })
            Spacer(Modifier.height(12.dp))
            Field(
                "Date of birth",
                dob,
                { dob = it },
                placeholder = "YYYY-MM-DD",
                keyboardType = KeyboardType.Number,
            )
            Spacer(Modifier.height(12.dp))
            Field("State / LGA", stateLga, { stateLga = it }, placeholder = "e.g. Lagos / Eti-Osa")

            SectionTitle("Emergency contacts")
            Text(
                "At least 2, up to 4. These people are notified with your location.",
                fontSize = 13.sp,
                color = StignItExtraColors.mutedForeground,
                modifier = Modifier.padding(bottom = 12.dp),
            )

            contacts.forEachIndexed { i, c ->
                Column(modifier = Modifier.padding(bottom = 16.dp)) {
                    Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                        Text("Contact ${i + 1}", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        if (contacts.size > 2) {
                            Text(
                                "Remove",
                                fontSize = 13.sp,
                                color = StignItExtraColors.danger,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .clickableNoRipple { contacts.removeAt(i) },
                            )
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Field("Name", c.name, { c.name = it })
                    Spacer(Modifier.height(8.dp))
                    Field("Phone", c.phone, { c.phone = it }, placeholder = "+234…", keyboardType = KeyboardType.Phone)
                    Spacer(Modifier.height(8.dp))
                    Field("Relationship", c.relationship, { c.relationship = it }, placeholder = "e.g. Brother")
                }
            }

            if (contacts.size < 4) {
                Text(
                    "+ Add another contact",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickableNoRipple { contacts.add(ContactDraft()) },
                )
            }

            when (role) {
                AccountRole.MEDICAL_PERSONNEL -> {
                    SectionTitle("Professional details")
                    Field("License / credential number", licenseNumber, { licenseNumber = it })
                    Spacer(Modifier.height(12.dp))
                    Field("Hospital / clinic affiliation", medicalAffiliation, { medicalAffiliation = it })
                }
                AccountRole.DRIVER_RESPONDER -> {
                    SectionTitle("Vehicle details")
                    Text(
                        "At least one of these two.",
                        fontSize = 13.sp,
                        color = StignItExtraColors.mutedForeground,
                        modifier = Modifier.padding(bottom = 12.dp),
                    )
                    Field("Vehicle type", vehicleType, { vehicleType = it }, placeholder = "e.g. Personal car")
                    Spacer(Modifier.height(12.dp))
                    Field("Ambulance service affiliation", responderAffiliation, { responderAffiliation = it })

                    SectionTitle("Equipment on board")
                    equipmentOptions.forEach { (key, label) ->
                        val checked = selectedEquipment.contains(key)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickableNoRipple {
                                    if (checked) selectedEquipment.remove(key) else selectedEquipment.add(key)
                                },
                        ) {
                            Checkbox(checked = checked, onCheckedChange = null)
                            Text(label, fontSize = 15.sp, modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Field("Other equipment (optional)", otherEquipment, { otherEquipment = it })
                }
                AccountRole.CIVILIAN -> Unit
            }

            if (error != null) {
                Text(
                    error!!,
                    modifier = Modifier.padding(top = 16.dp),
                    fontSize = 13.sp,
                    color = StignItExtraColors.danger,
                )
            }

            Spacer(Modifier.height(24.dp))
        }

        StignItButton(
            text = if (submitting) "Saving…" else "Finish setup",
            onClick = { submit() },
            size = StignItButtonSize.Large,
            modifier = Modifier.padding(bottom = 24.dp),
        )
    }
}

@Composable
private fun Field(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String? = null,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        placeholder = placeholder?.let { { Text(it) } },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier.fillMaxWidth(),
    )
}
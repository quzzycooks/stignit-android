package com.stignit.app.ui.medical

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stignit.app.data.MedicalInfo
import com.stignit.app.ui.components.SectionTitle
import com.stignit.app.ui.components.clickableNoRipple
import com.stignit.app.ui.theme.StignItExtraColors

val BLOOD_TYPES = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-", "Unknown")
val COMMON_ALLERGIES = listOf(
    "Penicillin", "Peanuts", "Latex", "NSAIDs", "Shellfish", "Bee stings", "Iodine", "Sulfa drugs",
)

/** Mutable draft backing [MedicalInfoForm] — create with [rememberMedicalInfoDraft], read back via [toMedicalInfo]. */
class MedicalInfoDraft(initial: MedicalInfo) {
    var bloodType by mutableStateOf(initial.bloodType)
    val selectedAllergies = mutableStateListOf<String>().apply {
        addAll(initial.allergies.filter { it in COMMON_ALLERGIES })
    }
    var otherAllergy by mutableStateOf(initial.allergies.firstOrNull { it !in COMMON_ALLERGIES } ?: "")
    var conditionsText by mutableStateOf(initial.conditions.joinToString(", "))
    var medicationsText by mutableStateOf(initial.medications.joinToString(", "))

    fun toMedicalInfo(): MedicalInfo = MedicalInfo(
        bloodType = bloodType,
        conditions = conditionsText.split(",").map { it.trim() }.filter { it.isNotBlank() },
        medications = medicationsText.split(",").map { it.trim() }.filter { it.isNotBlank() },
        allergies = selectedAllergies.toList() + listOfNotNull(otherAllergy.trim().takeIf { it.isNotBlank() }),
    )
}

@Composable
fun rememberMedicalInfoDraft(initial: MedicalInfo = MedicalInfo()) =
    androidx.compose.runtime.remember(initial) { MedicalInfoDraft(initial) }

/**
 * Blood type, allergies, existing conditions, and current medications — the same
 * fields the backend's MedicalInfoInput already accepts. Used both as a
 * registration step (skippable) and from Settings (editable any time).
 */
@Composable
fun MedicalInfoForm(draft: MedicalInfoDraft, modifier: Modifier = Modifier) {
    Column(modifier = modifier) {
        SectionTitle("Blood type")
        BloodTypeGrid(selected = draft.bloodType, onSelect = { draft.bloodType = it })

        SectionTitle("Allergies")
        AllergyChips(selected = draft.selectedAllergies)
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = draft.otherAllergy,
            onValueChange = { draft.otherAllergy = it },
            label = { Text("Other allergy") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        )

        SectionTitle("Existing conditions")
        OutlinedTextField(
            value = draft.conditionsText,
            onValueChange = { draft.conditionsText = it },
            placeholder = { Text("e.g. Asthma, Diabetes") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        )

        SectionTitle("Current medications")
        OutlinedTextField(
            value = draft.medicationsText,
            onValueChange = { draft.medicationsText = it },
            placeholder = { Text("e.g. Insulin") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Composable
private fun BloodTypeGrid(selected: String?, onSelect: (String) -> Unit) {
    val rows = BLOOD_TYPES.chunked(3)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { type ->
                    val isSelected = selected == type
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                RoundedCornerShape(14.dp),
                            )
                            .clickableNoRipple { onSelect(type) }
                            .padding(vertical = 14.dp),
                        contentAlignment = androidx.compose.ui.Alignment.Center,
                    ) {
                        Text(
                            type,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
                // Pad the last row so short rows don't stretch their cells wider.
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
private fun AllergyChips(selected: androidx.compose.runtime.snapshots.SnapshotStateList<String>) {
    androidx.compose.foundation.layout.FlowRow(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        COMMON_ALLERGIES.forEach { allergy ->
            val isSelected = allergy in selected
            Box(
                modifier = Modifier
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                        RoundedCornerShape(20.dp),
                    )
                    .clickableNoRipple { if (isSelected) selected.remove(allergy) else selected.add(allergy) }
                    .padding(horizontal = 16.dp, vertical = 10.dp),
            ) {
                Text(
                    allergy,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary else StignItExtraColors.mutedForeground,
                )
            }
        }
    }
}

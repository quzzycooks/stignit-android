package com.stignit.app.ui.contacts

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stignit.app.ui.components.*
import com.stignit.app.ui.theme.StignItExtraColors

private data class Contact(val name: String, val relation: String, val phone: String, val primary: Boolean)

private val contacts = listOf(
    Contact("Chidi Okafor", "Brother", "+234 803 221 8890", primary = true),
    Contact("Amaka Bello", "Partner", "+234 807 554 1120", primary = false),
    Contact("Mrs. Ngozi Eze", "Mother", "+234 811 909 4432", primary = false),
)

/** Direct port of src/routes/contacts.tsx. */
@Composable
fun ContactsScreen(onBack: () -> Unit, currentTab: BottomNavTab, onSelectTab: (BottomNavTab) -> Unit) {
    Column(modifier = Modifier.fillMaxSize()) {
        Screen(modifier = Modifier.weight(1f)) {
            TopBar(title = "Emergency Contacts", onBack = onBack)
            Text(
                "These people receive your location, incident details and a link to the Situation Room the moment a welfare check goes unanswered.",
                fontSize = 15.sp,
                color = StignItExtraColors.mutedForeground,
            )

            SectionTitle("Your circle")
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                contacts.forEach { c ->
                    Panel {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            androidx.compose.foundation.layout.Box(
                                modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.secondary, CircleShape),
                                contentAlignment = Alignment.Center,
                            ) {
                                Text(c.name.first().toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(c.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                Text("${c.relation} · ${c.phone}", fontSize = 14.sp, color = StignItExtraColors.mutedForeground)
                                if (c.primary) {
                                    StatusPill(Tone.Safe, "First to be called", modifier = Modifier.padding(top = 8.dp))
                                }
                            }
                            IconButton(
                                onClick = { /* dial in a later milestone */ },
                                modifier = Modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).size(44.dp),
                            ) {
                                Icon(Icons.Filled.Phone, contentDescription = "Call ${c.name}")
                            }
                        }
                    }
                }
            }

            StignItButton(
                text = "Add a contact",
                onClick = { /* wired to backend in milestone 4 */ },
                variant = StignItButtonVariant.Outline,
                size = StignItButtonSize.Large,
                leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) },
                modifier = Modifier.padding(top = 20.dp, bottom = 20.dp),
            )
        }
        BottomNav(current = currentTab, onSelect = onSelectTab)
    }
}

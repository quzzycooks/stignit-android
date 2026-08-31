package com.stignit.app.ui.contacts

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.stignit.app.data.ApiResult
import com.stignit.app.data.EmergencyContact
import com.stignit.app.data.rememberContactsRepository
import com.stignit.app.ui.components.*
import com.stignit.app.ui.theme.StignItExtraColors
import kotlinx.coroutines.launch

/** Direct port of src/routes/contacts.tsx, wired to the real emergency-contacts API. */
@Composable
fun ContactsScreen(onBack: () -> Unit, currentTab: BottomNavTab, onSelectTab: (BottomNavTab) -> Unit) {
    val repo = rememberContactsRepository()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var loading by remember { mutableStateOf(true) }
    var contacts by remember { mutableStateOf<List<EmergencyContact>>(emptyList()) }
    var error by remember { mutableStateOf<String?>(null) }
    var showAddForm by remember { mutableStateOf(false) }
    var savingContact by remember { mutableStateOf(false) }

    suspend fun refresh() {
        when (val r = repo.list()) {
            is ApiResult.Ok -> {
                contacts = r.value
                error = null
            }
            is ApiResult.Err -> error = r.message
        }
        loading = false
    }

    LaunchedEffect(Unit) { refresh() }

    Column(modifier = Modifier.fillMaxSize()) {
        Screen(modifier = Modifier.weight(1f)) {
            TopBar(title = "Emergency Contacts", onBack = onBack)
            Text(
                "These people receive your location, incident details and a link to the Situation Room the moment a welfare check goes unanswered.",
                fontSize = 15.sp,
                color = StignItExtraColors.mutedForeground,
            )

            SectionTitle("Your circle")
            when {
                loading -> Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
                error != null && contacts.isEmpty() -> Text(error!!, color = StignItExtraColors.danger)
                contacts.isEmpty() -> Text(
                    "No emergency contacts yet — add at least one so someone can be reached if you need help.",
                    color = StignItExtraColors.mutedForeground,
                )
                else -> Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    contacts.forEachIndexed { index, c ->
                        Panel {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                Box(
                                    modifier = Modifier.size(48.dp).background(MaterialTheme.colorScheme.secondary, CircleShape),
                                    contentAlignment = Alignment.Center,
                                ) {
                                    Text(c.name.first().uppercaseChar().toString(), fontSize = 18.sp, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                }
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(c.name, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                                    Text("${c.relationship} · ${c.phone}", fontSize = 14.sp, color = StignItExtraColors.mutedForeground)
                                    if (index == 0) {
                                        StatusPill(Tone.Safe, "First to be called", modifier = Modifier.padding(top = 8.dp))
                                    }
                                }
                                IconButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${c.phone}"))
                                        context.startActivity(intent)
                                    },
                                    modifier = Modifier.background(MaterialTheme.colorScheme.surface, RoundedCornerShape(12.dp)).size(44.dp),
                                ) {
                                    Icon(Icons.Filled.Phone, contentDescription = "Call ${c.name}")
                                }
                                IconButton(
                                    onClick = {
                                        scope.launch {
                                            when (val r = repo.remove(c.id)) {
                                                is ApiResult.Ok -> refresh()
                                                is ApiResult.Err -> error = r.message
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(44.dp),
                                ) {
                                    Icon(Icons.Filled.DeleteOutline, contentDescription = "Remove ${c.name}", tint = StignItExtraColors.mutedForeground)
                                }
                            }
                        }
                    }
                }
            }

            if (error != null && contacts.isNotEmpty()) {
                Text(error!!, modifier = Modifier.padding(top = 12.dp), fontSize = 13.sp, color = StignItExtraColors.danger)
            }

            if (showAddForm) {
                AddContactForm(
                    saving = savingContact,
                    onCancel = { showAddForm = false },
                    onSave = { name, phone, relationship ->
                        savingContact = true
                        scope.launch {
                            when (val r = repo.add(name, phone, relationship)) {
                                is ApiResult.Ok -> {
                                    savingContact = false
                                    showAddForm = false
                                    refresh()
                                }
                                is ApiResult.Err -> {
                                    savingContact = false
                                    error = r.message
                                }
                            }
                        }
                    },
                )
            } else if (contacts.size < 4) {
                StignItButton(
                    text = "Add a contact",
                    onClick = { showAddForm = true },
                    variant = StignItButtonVariant.Outline,
                    size = StignItButtonSize.Large,
                    leadingIcon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    modifier = Modifier.padding(top = 20.dp, bottom = 20.dp),
                )
            }
        }
        BottomNav(current = currentTab, onSelect = onSelectTab)
    }
}

@Composable
private fun AddContactForm(
    saving: Boolean,
    onCancel: () -> Unit,
    onSave: (name: String, phone: String, relationship: String) -> Unit,
) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }

    Panel(modifier = Modifier.padding(top = 16.dp)) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("New contact", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Icon(Icons.Filled.Close, contentDescription = "Cancel", modifier = Modifier.clickableNoRipple(onCancel))
        }
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone") },
            placeholder = { Text("+234…") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = relationship,
            onValueChange = { relationship = it },
            label = { Text("Relationship") },
            placeholder = { Text("e.g. Brother") },
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(Modifier.height(16.dp))
        StignItButton(
            text = if (saving) "Saving…" else "Save contact",
            onClick = {
                if (!saving && name.isNotBlank() && phone.isNotBlank() && relationship.isNotBlank()) {
                    onSave(name.trim(), phone.trim(), relationship.trim())
                }
            },
            size = StignItButtonSize.Large,
        )
    }
}

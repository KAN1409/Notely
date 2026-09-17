package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.FollowUpEntity
import com.example.ui.theme.NexAmberAlert
import com.example.ui.theme.NexBlueAccent
import com.example.ui.theme.NexDarkBackground
import com.example.ui.theme.NexDarkBorder
import com.example.ui.theme.NexDarkBorderSubtle
import com.example.ui.theme.NexDarkSurface
import com.example.ui.theme.NexDarkSurfaceElevated
import com.example.ui.theme.NexGreenSuccess
import com.example.ui.theme.NexScarletBright
import com.example.ui.theme.NexScarletPrimary
import com.example.ui.theme.NexTextMuted
import com.example.ui.theme.NexTextPrimary
import com.example.ui.theme.NexTextSecondary
import com.example.viewmodel.NotelyViewModel

@Composable
fun FollowUpsScreen(
    viewModel: NotelyViewModel,
    onBack: () -> Unit,
    onOpenNote: (Long) -> Unit
) {
    val allFollowUps by viewModel.allFollowUps.collectAsState()
    var selectedFilter by remember { mutableStateOf("All") }
    var showAddDialog by remember { mutableStateOf(false) }
    var newSubject by remember { mutableStateOf("") }
    var newPerson by remember { mutableStateOf("") }

    val filteredFollowUps = when (selectedFilter) {
        "Waiting" -> allFollowUps.filter { it.status == "WAITING" }
        "Due" -> allFollowUps.filter { it.status == "DUE" }
        "Overdue" -> allFollowUps.filter { it.status == "OVERDUE" }
        "Resolved" -> allFollowUps.filter { it.status == "RESOLVED" }
        else -> allFollowUps
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NexDarkBackground)
            .testTag("follow_ups_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Top Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NexDarkSurfaceElevated)
                        .border(1.dp, NexDarkBorder, RoundedCornerShape(10.dp))
                        .testTag("follow_ups_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NexTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Follow-ups & Open Loops",
                        style = MaterialTheme.typography.titleMedium,
                        color = NexTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Never let an open commitment drop",
                        style = MaterialTheme.typography.bodySmall,
                        color = NexTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Status Filter Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    "All" to allFollowUps.size,
                    "Waiting" to allFollowUps.count { it.status == "WAITING" },
                    "Due" to allFollowUps.count { it.status == "DUE" },
                    "Overdue" to allFollowUps.count { it.status == "OVERDUE" },
                    "Resolved" to allFollowUps.count { it.status == "RESOLVED" }
                ).forEach { (filter, count) ->
                    val isSelected = selectedFilter == filter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) NexScarletPrimary else NexDarkSurfaceElevated)
                            .border(1.dp, if (isSelected) NexScarletPrimary else NexDarkBorder, RoundedCornerShape(12.dp))
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("filter_followup_$filter")
                    ) {
                        Text(
                            text = "$filter ($count)",
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) Color.White else NexTextSecondary,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Follow-up cards list
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredFollowUps, key = { it.id }) { item ->
                    FollowUpItemCard(
                        followUp = item,
                        onToggleStatus = { viewModel.toggleFollowUpStatus(item) },
                        onResolve = { viewModel.resolveFollowUp(item.id) },
                        onOpenNote = { if (item.noteId != null) onOpenNote(item.noteId) }
                    )
                }
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Add follow-up manual button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = NexScarletPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_follow_up")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Follow-up")
        }

        // Add dialog
        if (showAddDialog) {
            Surface(
                color = NexDarkSurfaceElevated,
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .border(1.dp, NexDarkBorder, RoundedCornerShape(16.dp))
                    .align(Alignment.Center)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text("New Follow-up Loop", style = MaterialTheme.typography.titleSmall, color = NexTextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newSubject,
                        onValueChange = { newSubject = it },
                        placeholder = { Text("Subject (e.g. Call Ahmed for quote approval)", color = NexTextMuted, fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("input_new_followup_subject"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NexScarletPrimary,
                            unfocusedBorderColor = NexDarkBorder,
                            focusedTextColor = NexTextPrimary,
                            unfocusedTextColor = NexTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = newPerson,
                        onValueChange = { newPerson = it },
                        placeholder = { Text("Person involved (e.g. Ahmed)", color = NexTextMuted, fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("input_new_followup_person"),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NexScarletPrimary,
                            unfocusedBorderColor = NexDarkBorder,
                            focusedTextColor = NexTextPrimary,
                            unfocusedTextColor = NexTextPrimary
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        Button(
                            onClick = { showAddDialog = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Text("Cancel", color = NexTextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newSubject.isNotBlank()) {
                                    viewModel.addFollowUp(
                                        noteId = 1L,
                                        subject = newSubject,
                                        person = newPerson.ifBlank { null },
                                        project = "General",
                                        dueDate = System.currentTimeMillis() + 86400000L
                                    )
                                    newSubject = ""
                                    newPerson = ""
                                    showAddDialog = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NexScarletPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_save_follow_up")
                        ) {
                            Text("Create Loop", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FollowUpItemCard(
    followUp: FollowUpEntity,
    onToggleStatus: () -> Unit,
    onResolve: () -> Unit,
    onOpenNote: () -> Unit
) {
    val isResolved = followUp.status == "RESOLVED"
    val statusColor = when (followUp.status) {
        "OVERDUE" -> NexScarletBright
        "DUE" -> NexAmberAlert
        "WAITING" -> NexBlueAccent
        "RESOLVED" -> NexGreenSuccess
        else -> NexScarletPrimary
    }

    Surface(
        color = NexDarkSurfaceElevated,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NexDarkBorder, RoundedCornerShape(14.dp))
            .testTag("follow_up_item_${followUp.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.Top
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isResolved) NexGreenSuccess.copy(alpha = 0.2f) else NexDarkSurface)
                    .border(1.dp, if (isResolved) NexGreenSuccess else statusColor, RoundedCornerShape(6.dp))
                    .clickable { onToggleStatus() },
                contentAlignment = Alignment.Center
            ) {
                if (isResolved) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Resolved",
                        tint = NexGreenSuccess,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = followUp.subject,
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isResolved) NexTextMuted else NexTextPrimary,
                        fontWeight = FontWeight.SemiBold,
                        textDecoration = if (isResolved) TextDecoration.LineThrough else TextDecoration.None
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NexDarkSurface)
                            .border(1.dp, NexDarkBorderSubtle, RoundedCornerShape(6.dp))
                            .padding(horizontal = 7.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = followUp.status,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                }

                if (!followUp.person.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = "With: ${followUp.person}",
                        style = MaterialTheme.typography.bodySmall,
                        color = NexTextSecondary,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val dueDateFormatted = if (followUp.dueDateMillis != null) {
                        java.text.SimpleDateFormat("EEE, MMM dd", java.util.Locale.getDefault()).format(java.util.Date(followUp.dueDateMillis))
                    } else "No due date"

                    Text(
                        text = "Due: $dueDateFormatted",
                        style = MaterialTheme.typography.labelSmall,
                        color = NexTextMuted,
                        fontSize = 11.sp
                    )

                    if (!isResolved) {
                        Text(
                            text = "Resolve",
                            style = MaterialTheme.typography.labelSmall,
                            color = NexScarletPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            modifier = Modifier
                                .clickable { onResolve() }
                                .padding(2.dp)
                        )
                    }
                }
            }
        }
    }
}

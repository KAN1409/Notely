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
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
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
import com.example.data.model.ReminderEntity
import com.example.ui.theme.NexAmberAlert
import com.example.ui.theme.NexDarkBackground
import com.example.ui.theme.NexDarkBorder
import com.example.ui.theme.NexDarkSurface
import com.example.ui.theme.NexDarkSurfaceElevated
import com.example.ui.theme.NexGreenSuccess
import com.example.ui.theme.NexScarletPrimary
import com.example.ui.theme.NexTextMuted
import com.example.ui.theme.NexTextPrimary
import com.example.ui.theme.NexTextSecondary
import com.example.viewmodel.NotelyViewModel
import java.util.Calendar

@Composable
fun CalendarScreen(
    viewModel: NotelyViewModel,
    onBack: () -> Unit,
    onOpenNote: (Long) -> Unit
) {
    val reminders by viewModel.allReminders.collectAsState()
    var selectedDayOffset by remember { mutableStateOf(0) } // 0 = Today
    var showAddModal by remember { mutableStateOf(false) }
    var newReminderTitle by remember { mutableStateOf("") }

    val days = remember {
        val list = mutableListOf<Triple<String, String, Int>>()
        val cal = Calendar.getInstance()
        val dayFormat = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
        val numFormat = java.text.SimpleDateFormat("dd", java.util.Locale.getDefault())

        for (i in 0..6) {
            val dCal = cal.clone() as Calendar
            dCal.add(Calendar.DAY_OF_YEAR, i)
            list.add(Triple(dayFormat.format(dCal.time), numFormat.format(dCal.time), i))
        }
        list
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(NexDarkBackground)
            .testTag("calendar_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            // Header
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
                        .testTag("calendar_back_button")
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
                        text = "Calendar & Schedule",
                        style = MaterialTheme.typography.titleMedium,
                        color = NexTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Structured reminders & commitments",
                        style = MaterialTheme.typography.bodySmall,
                        color = NexTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Horizontal Date strip
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                days.forEach { (dayName, dayNum, offset) ->
                    val isSelected = selectedDayOffset == offset
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) NexScarletPrimary else NexDarkSurfaceElevated)
                            .border(1.dp, if (isSelected) NexScarletPrimary else NexDarkBorder, RoundedCornerShape(12.dp))
                            .clickable { selectedDayOffset = offset }
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("calendar_day_$offset")
                    ) {
                        Text(
                            text = dayName,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isSelected) Color.White else NexTextMuted,
                            fontWeight = FontWeight.Medium,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = dayNum,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isSelected) Color.White else NexTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (selectedDayOffset == 0) "Today's Schedule & Reminders" else "Scheduled for this day",
                style = MaterialTheme.typography.titleSmall,
                color = NexTextPrimary,
                fontWeight = FontWeight.SemiBold
            )

            Spacer(modifier = Modifier.height(10.dp))

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (reminders.isEmpty()) {
                    item {
                        Surface(
                            color = NexDarkSurfaceElevated,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(1.dp, NexDarkBorder, RoundedCornerShape(14.dp))
                        ) {
                            Text(
                                text = "No scheduled reminders. Tap + to add.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = NexTextMuted,
                                modifier = Modifier.padding(20.dp)
                            )
                        }
                    }
                } else {
                    items(reminders, key = { it.id }) { rem ->
                        ReminderTimelineCard(
                            reminder = rem,
                            onToggleComplete = { viewModel.toggleReminderCompleted(rem) },
                            onDelete = { viewModel.deleteReminder(rem.id) },
                            onOpenNote = { onOpenNote(rem.noteId) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }

        // Add Reminder FAB
        FloatingActionButton(
            onClick = { showAddModal = true },
            containerColor = NexScarletPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("fab_add_reminder")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Reminder")
        }

        // Add Reminder Modal
        if (showAddModal) {
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
                    Text("Schedule Reminder", style = MaterialTheme.typography.titleSmall, color = NexTextPrimary, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newReminderTitle,
                        onValueChange = { newReminderTitle = it },
                        placeholder = { Text("Reminder description (e.g. Call supplier)", color = NexTextMuted, fontSize = 13.sp) },
                        modifier = Modifier.fillMaxWidth().testTag("input_new_reminder_title"),
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
                            onClick = { showAddModal = false },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent)
                        ) {
                            Text("Cancel", color = NexTextSecondary)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (newReminderTitle.isNotBlank()) {
                                    val time = System.currentTimeMillis() + 3600000L // 1 hour from now
                                    viewModel.addReminder(1L, newReminderTitle, time)
                                    newReminderTitle = ""
                                    showAddModal = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = NexScarletPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("btn_confirm_add_reminder")
                        ) {
                            Text("Schedule", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ReminderTimelineCard(
    reminder: ReminderEntity,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    onOpenNote: () -> Unit
) {
    val timeFormatted = java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault())
        .format(java.util.Date(reminder.scheduledTimeMillis))

    Surface(
        color = NexDarkSurfaceElevated,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NexDarkBorder, RoundedCornerShape(14.dp))
            .testTag("reminder_item_${reminder.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (reminder.isCompleted) NexGreenSuccess.copy(alpha = 0.2f) else NexDarkSurface)
                    .border(1.dp, if (reminder.isCompleted) NexGreenSuccess else NexDarkBorder, RoundedCornerShape(6.dp))
                    .clickable { onToggleComplete() },
                contentAlignment = Alignment.Center
            ) {
                if (reminder.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = NexGreenSuccess,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = reminder.title,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (reminder.isCompleted) NexTextMuted else NexTextPrimary,
                    fontWeight = FontWeight.Medium,
                    textDecoration = if (reminder.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Alarm, contentDescription = "Alarm", tint = NexAmberAlert, modifier = Modifier.size(11.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = timeFormatted,
                        style = MaterialTheme.typography.bodySmall,
                        color = NexAmberAlert,
                        fontSize = 11.sp
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = NexTextMuted,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

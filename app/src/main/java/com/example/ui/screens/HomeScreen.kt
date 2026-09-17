package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NoteEntity
import com.example.ui.components.ContextChipRow
import com.example.ui.components.MetricCard
import com.example.ui.components.SourceBadge
import com.example.ui.theme.NexAmberAlert
import com.example.ui.theme.NexBlueAccent
import com.example.ui.theme.NexDarkBackground
import com.example.ui.theme.NexDarkBorder
import com.example.ui.theme.NexDarkBorderSubtle
import com.example.ui.theme.NexDarkSurface
import com.example.ui.theme.NexDarkSurfaceElevated
import com.example.ui.theme.NexScarletPrimary
import com.example.ui.theme.NexTextMuted
import com.example.ui.theme.NexTextPrimary
import com.example.ui.theme.NexTextSecondary
import com.example.viewmodel.NotelyViewModel

@Composable
fun HomeScreen(
    viewModel: NotelyViewModel,
    onOpenNote: (Long) -> Unit,
    onOpenSearch: () -> Unit,
    onOpenFollowUps: () -> Unit,
    onOpenCalendar: () -> Unit,
    onOpenSettings: () -> Unit,
    onStartVoice: () -> Unit
) {
    val recentNotes by viewModel.recentNotes.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()
    val openFollowUps by viewModel.openFollowUps.collectAsState()
    val activeReminders by viewModel.activeReminders.collectAsState()
    val selectedChip by viewModel.selectedContextChip.collectAsState()

    val filteredNotes = if (selectedChip == "All") {
        recentNotes
    } else {
        recentNotes.filter { note ->
            note.tags.any { it.equals(selectedChip, ignoreCase = true) } ||
                    note.topics.any { it.equals(selectedChip, ignoreCase = true) }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(NexDarkBackground)
            .testTag("home_screen_content")
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Header: User Greeting & Avatar
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(NexDarkSurfaceElevated)
                            .border(1.dp, NexDarkBorder, RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "N",
                            style = MaterialTheme.typography.titleMedium,
                            color = NexScarletPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "NexNote",
                            style = MaterialTheme.typography.titleMedium,
                            color = NexTextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "Structured Note Intelligence",
                            style = MaterialTheme.typography.bodySmall,
                            color = NexTextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NexDarkSurfaceElevated)
                        .border(1.dp, NexDarkBorder, RoundedCornerShape(10.dp))
                        .testTag("home_settings_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "Settings",
                        tint = NexTextSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }

        // Quick Search Bar with Instant Mic Trigger
        item {
            Surface(
                color = NexDarkSurfaceElevated,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NexDarkBorder, RoundedCornerShape(14.dp))
                    .clickable { onOpenSearch() }
                    .testTag("home_search_trigger")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = NexTextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Search notes, transcripts, OCR...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NexTextMuted,
                            fontSize = 13.sp
                        )
                    }

                    IconButton(
                        onClick = onStartVoice,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(NexDarkSurface)
                            .border(1.dp, NexDarkBorderSubtle, RoundedCornerShape(8.dp))
                            .testTag("home_quick_mic_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = "Quick Speak",
                            tint = NexScarletPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }

        // Quick Ingestion Action Strip
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Voice Memo
                Surface(
                    color = NexDarkSurfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, NexDarkBorder, RoundedCornerShape(10.dp))
                        .clickable { onStartVoice() }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Mic, contentDescription = null, tint = NexScarletPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Voice", color = NexTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }

                // Text Note
                Surface(
                    color = NexDarkSurfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, NexDarkBorder, RoundedCornerShape(10.dp))
                        .clickable { viewModel.navigateTo("CAPTURE") }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = null, tint = NexScarletPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Write", color = NexTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }

                // Scan OCR
                Surface(
                    color = NexDarkSurfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, NexDarkBorder, RoundedCornerShape(10.dp))
                        .clickable { viewModel.navigateTo("CAPTURE") }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, tint = NexScarletPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Scan", color = NexTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }

                // AI Copilot
                Surface(
                    color = NexDarkSurfaceElevated,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .border(1.dp, NexDarkBorder, RoundedCornerShape(10.dp))
                        .clickable { viewModel.navigateTo("ASSISTANT") }
                ) {
                    Row(
                        modifier = Modifier.padding(vertical = 10.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = NexScarletPrimary, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copilot", color = NexTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Context Filter Chips
        item {
            ContextChipRow(
                selectedChip = selectedChip,
                onChipSelected = { viewModel.selectedContextChip.value = it }
            )
        }

        // Metric Card 1: Needs Attention
        item {
            val attentionCount = (openFollowUps.size + activeReminders.size).toString()
            MetricCard(
                title = "Needs Attention",
                subtitle = "${openFollowUps.size} open loops • ${activeReminders.size} scheduled reminders",
                count = attentionCount,
                badgeColor = NexScarletPrimary,
                icon = Icons.Default.NotificationsActive,
                testTag = "metric_needs_attention",
                onClick = onOpenFollowUps
            )
        }

        // Metric Card 2: Today
        item {
            MetricCard(
                title = "Today's Schedule",
                subtitle = "${activeReminders.size} active reminders • ${allNotes.size} total notes in system",
                count = "Today",
                badgeColor = NexBlueAccent,
                icon = Icons.Default.CalendarMonth,
                testTag = "metric_today",
                onClick = onOpenCalendar
            )
        }

        // Metric Card 3: Open Loops
        item {
            MetricCard(
                title = "Open Loops",
                subtitle = "${openFollowUps.count { it.status == "WAITING" }} waiting • ${openFollowUps.count { it.status == "DUE" || it.status == "OVERDUE" }} due",
                count = openFollowUps.size.toString(),
                badgeColor = NexAmberAlert,
                icon = Icons.Default.AccessTime,
                testTag = "metric_open_loops",
                onClick = onOpenFollowUps
            )
        }

        // Recent Captures Section Header
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Captures",
                    style = MaterialTheme.typography.titleSmall,
                    color = NexTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "All notes (${allNotes.size})",
                    style = MaterialTheme.typography.labelSmall,
                    color = NexScarletPrimary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clickable { viewModel.activeBottomTab.value = "NOTES" }
                        .testTag("home_see_all_notes")
                )
            }
        }

        // List of Notes
        if (filteredNotes.isEmpty()) {
            item {
                Surface(
                    color = NexDarkSurfaceElevated,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NexDarkBorder, RoundedCornerShape(14.dp))
                ) {
                    Box(
                        modifier = Modifier.padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No captures yet in this category. Tap + to add.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NexTextMuted
                        )
                    }
                }
            }
        } else {
            items(filteredNotes, key = { it.id }) { note ->
                NoteCardItem(note = note, onClick = { onOpenNote(note.id) })
            }
        }

        item {
            Spacer(modifier = Modifier.height(72.dp))
        }
    }
}

@Composable
fun NoteCardItem(
    note: NoteEntity,
    onClick: () -> Unit
) {
    Surface(
        color = NexDarkSurfaceElevated,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NexDarkBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag("note_card_${note.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SourceBadge(sourceType = note.sourceType)

                val timeStr = java.text.SimpleDateFormat("MMM dd, h:mm a", java.util.Locale.getDefault())
                    .format(java.util.Date(note.createdAt))
                Text(
                    text = timeStr,
                    style = MaterialTheme.typography.bodySmall,
                    color = NexTextMuted,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = note.title,
                style = MaterialTheme.typography.titleSmall,
                color = NexTextPrimary,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = note.summary.ifBlank { note.originalInput },
                style = MaterialTheme.typography.bodySmall,
                color = NexTextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 18.sp
            )

            // Tags & Extracted Scope Row
            val chips = (note.projects + note.people + note.topics).take(3)
            if (chips.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    chips.forEach { chip ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NexDarkSurface)
                                .border(1.dp, NexDarkBorderSubtle, RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = chip,
                                style = MaterialTheme.typography.labelSmall,
                                color = NexTextSecondary,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

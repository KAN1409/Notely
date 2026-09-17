package com.example.ui.screens

import android.content.Intent
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.NoteEntity
import com.example.ui.components.SourceBadge
import com.example.ui.theme.NexDarkBackground
import com.example.ui.theme.NexDarkBorder
import com.example.ui.theme.NexDarkBorderSubtle
import com.example.ui.theme.NexDarkSurface
import com.example.ui.theme.NexDarkSurfaceElevated
import com.example.ui.theme.NexDarkSurfaceHigher
import com.example.ui.theme.NexGreenSuccess
import com.example.ui.theme.NexScarletBright
import com.example.ui.theme.NexScarletPrimary
import com.example.ui.theme.NexTextMuted
import com.example.ui.theme.NexTextPrimary
import com.example.ui.theme.NexTextSecondary
import com.example.viewmodel.NotelyViewModel

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Hub
import androidx.compose.material3.CircularProgressIndicator
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

@Composable
fun NoteDetailScreen(
    viewModel: NotelyViewModel,
    onBack: () -> Unit,
    onEditNote: (Long) -> Unit,
    onOpenRelatedNote: (Long) -> Unit = {},
    onNavigateToCalendar: () -> Unit = {},
    onNavigateToFollowUps: () -> Unit = {}
) {
    val note by viewModel.selectedNote.collectAsState()
    val context = LocalContext.current

    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Overview", "Transcript", "Media & OCR")

    // Audio player states
    val isPlaying by viewModel.playerHelper.isPlaying.collectAsState()
    val currentPositionMs by viewModel.playerHelper.currentPositionMs.collectAsState()
    val durationMs by viewModel.playerHelper.durationMs.collectAsState()
    val speed by viewModel.playerHelper.speed.collectAsState()

    if (note == null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(NexDarkBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("Note not found or deleted.", color = NexTextSecondary)
        }
        return
    }

    val currentNote = note!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexDarkBackground)
            .testTag("note_detail_screen")
    ) {
        // Top App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NexDarkSurfaceElevated)
                        .border(1.dp, NexDarkBorder, RoundedCornerShape(10.dp))
                        .testTag("note_detail_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NexTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                SourceBadge(sourceType = currentNote.sourceType)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, currentNote.title)
                            putExtra(Intent.EXTRA_TEXT, "${currentNote.title}\n\n${currentNote.summary.ifBlank { currentNote.originalInput }}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share note via"))
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NexDarkSurfaceElevated)
                        .border(1.dp, NexDarkBorder, RoundedCornerShape(10.dp))
                        .testTag("note_detail_share_button")
                ) {
                    Icon(imageVector = Icons.Default.Share, contentDescription = "Share", tint = NexTextPrimary, modifier = Modifier.size(18.dp))
                }

                IconButton(
                    onClick = { onEditNote(currentNote.id) },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NexDarkSurfaceElevated)
                        .border(1.dp, NexDarkBorder, RoundedCornerShape(10.dp))
                        .testTag("note_detail_edit_button")
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", tint = NexTextPrimary, modifier = Modifier.size(18.dp))
                }

                IconButton(
                    onClick = {
                        viewModel.deleteNote(currentNote.id)
                        onBack()
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NexDarkSurfaceElevated)
                        .border(1.dp, NexDarkBorder, RoundedCornerShape(10.dp))
                        .testTag("note_detail_delete_button")
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete", tint = NexScarletBright, modifier = Modifier.size(18.dp))
                }
            }
        }

        // Title and Date Header
        Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            Text(
                text = currentNote.title.ifBlank { "Untitled Note" },
                style = MaterialTheme.typography.titleLarge,
                color = NexTextPrimary,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )

            Spacer(modifier = Modifier.height(4.dp))

            val dateFormatted = java.text.SimpleDateFormat("EEEE, MMM dd, yyyy • h:mm a", java.util.Locale.getDefault())
                .format(java.util.Date(currentNote.createdAt))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = "Created Time",
                    tint = NexTextMuted,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = dateFormatted,
                    style = MaterialTheme.typography.bodySmall,
                    color = NexTextMuted,
                    fontSize = 11.sp
                )
            }
        }

        // Voice Player Bar (if note is voice)
        if (currentNote.sourceType == "VOICE" && !currentNote.mediaPath.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                color = NexDarkSurfaceElevated,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .border(1.dp, NexDarkBorder, RoundedCornerShape(12.dp))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                if (isPlaying) {
                                    viewModel.playerHelper.pause()
                                } else {
                                    if (currentPositionMs > 0) {
                                        viewModel.playerHelper.resume()
                                    } else {
                                        viewModel.playerHelper.loadAndPlay(currentNote.mediaPath, currentNote.mediaDurationMs.coerceAtLeast(60000L))
                                    }
                                }
                            },
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(NexScarletPrimary)
                                .testTag("btn_play_pause_detail")
                        ) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Slider(
                            value = if (durationMs > 0) (currentPositionMs.toFloat() / durationMs.toFloat()) else 0f,
                            onValueChange = { frac ->
                                val target = (frac * durationMs).toLong()
                                viewModel.playerHelper.seekTo(target)
                            },
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = NexScarletPrimary,
                                activeTrackColor = NexScarletPrimary,
                                inactiveTrackColor = NexDarkBorder
                            )
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "${formatTime(currentPositionMs)} / ${formatTime(durationMs.coerceAtLeast(currentNote.mediaDurationMs))}",
                            style = MaterialTheme.typography.labelSmall,
                            color = NexTextSecondary,
                            fontSize = 10.sp
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NexDarkSurfaceHigher)
                                .border(1.dp, NexDarkBorder, RoundedCornerShape(6.dp))
                                .clickable { viewModel.playerHelper.toggleSpeed() }
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "${speed}x",
                                style = MaterialTheme.typography.labelSmall,
                                color = NexTextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tabs
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = NexDarkBackground,
            contentColor = NexTextPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = NexScarletPrimary,
                    height = 2.dp
                )
            },
            divider = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(1.dp)
                        .background(NexDarkBorder)
                )
            }
        ) {
            tabs.forEachIndexed { index, tabTitle ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = {
                        Text(
                            text = tabTitle,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (selectedTab == index) NexTextPrimary else NexTextMuted,
                            fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    },
                    modifier = Modifier.testTag("tab_detail_$index")
                )
            }
        }

        // Tab Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                0 -> OverviewTab(
                    note = currentNote,
                    viewModel = viewModel,
                    onNavigateToCalendar = onNavigateToCalendar,
                    onNavigateToFollowUps = onNavigateToFollowUps,
                    onOpenRelatedNote = onOpenRelatedNote
                )
                1 -> TranscriptTab(note = currentNote)
                2 -> MediaOcrTab(note = currentNote)
            }
        }
    }
}

@Composable
private fun OverviewTab(
    note: NoteEntity,
    viewModel: NotelyViewModel,
    onNavigateToCalendar: () -> Unit,
    onNavigateToFollowUps: () -> Unit,
    onOpenRelatedNote: (Long) -> Unit
) {
    val context = LocalContext.current
    val transformState by viewModel.noteTransformState.collectAsState()
    val allRelationships by viewModel.allRelationships.collectAsState()
    val allNotes by viewModel.allNotes.collectAsState()

    val relatedRelationships = allRelationships.filter { it.sourceNoteId == note.id || it.targetNoteId == note.id }

    val aiTools = listOf(
        "✉️ Email Draft" to "EMAIL",
        "⚡ Action Plan" to "ACTION_PLAN",
        "📝 3 Bullets" to "BULLETS",
        "☑️ Checklist" to "CHECKLIST",
        "🇸🇦 Arabic Summary" to "ARABIC"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // AI Smart Actions Carousel
        item {
            Column {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = NexScarletPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Instant AI Transforms",
                        style = MaterialTheme.typography.labelSmall,
                        color = NexTextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    aiTools.forEach { (label, actionCode) ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(NexDarkSurfaceElevated)
                                .border(1.dp, NexDarkBorder, RoundedCornerShape(20.dp))
                                .clickable { viewModel.performNoteTransform(note.id, actionCode) }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = NexTextPrimary,
                                fontWeight = FontWeight.Medium,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // Active AI Transform Result Card (if any)
        if (transformState.isTransforming || transformState.resultText != null) {
            item {
                Surface(
                    color = NexDarkSurfaceElevated,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NexScarletPrimary.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = NexScarletPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "AI Result: ${transformState.transformType ?: "Transform"}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = NexTextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                            }

                            IconButton(
                                onClick = { viewModel.clearTransformState() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close",
                                    tint = NexTextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        if (transformState.isTransforming) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 12.dp)
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                    color = NexScarletPrimary
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = "Synthesizing AI output...",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = NexTextSecondary
                                )
                            }
                        } else {
                            val res = transformState.resultText ?: ""
                            Text(
                                text = res,
                                style = MaterialTheme.typography.bodyMedium,
                                color = NexTextPrimary,
                                lineHeight = 20.sp,
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                        clipboard.setPrimaryClip(ClipData.newPlainText("AI Transform", res))
                                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy",
                                        tint = NexTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(6.dp))

                                IconButton(
                                    onClick = {
                                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_SUBJECT, "AI Transform: ${note.title}")
                                            putExtra(Intent.EXTRA_TEXT, res)
                                        }
                                        context.startActivity(Intent.createChooser(shareIntent, "Share Transform"))
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Share,
                                        contentDescription = "Share",
                                        tint = NexTextSecondary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Executive Summary Card
        item {
            Surface(
                color = NexDarkSurfaceElevated,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NexDarkBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Executive Summary",
                            style = MaterialTheme.typography.labelMedium,
                            color = NexTextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NexDarkSurface)
                                .border(1.dp, NexDarkBorderSubtle, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = note.noteType,
                                style = MaterialTheme.typography.labelSmall,
                                color = NexScarletPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = note.summary.ifBlank { note.originalInput },
                        style = MaterialTheme.typography.bodyMedium,
                        color = NexTextPrimary,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // Key Points / Takeaways
        if (note.keyPoints.isNotEmpty()) {
            item {
                Surface(
                    color = NexDarkSurfaceElevated,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NexDarkBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Key Takeaways",
                            style = MaterialTheme.typography.labelMedium,
                            color = NexTextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        note.keyPoints.forEachIndexed { idx, point ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(NexScarletPrimary)
                                        .align(Alignment.CenterVertically)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = point,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = NexTextPrimary,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Suggested Actions
        if (note.suggestedActions.isNotEmpty()) {
            item {
                Surface(
                    color = NexDarkSurfaceElevated,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NexDarkBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Actionable Steps (${note.suggestedActions.size})",
                            style = MaterialTheme.typography.labelMedium,
                            color = NexTextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        note.suggestedActions.forEachIndexed { index, actionText ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .padding(vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Action item",
                                    tint = NexScarletPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Text(
                                    text = actionText,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = NexTextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Key Entities: Projects, People, Topics
        item {
            Surface(
                color = NexDarkSurfaceElevated,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NexDarkBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Extracted Entities & Metadata",
                        style = MaterialTheme.typography.labelMedium,
                        color = NexTextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    if (note.projects.isNotEmpty()) {
                        Text("Projects", style = MaterialTheme.typography.labelSmall, color = NexTextMuted, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            note.projects.forEach { proj ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NexDarkSurface)
                                        .border(1.dp, NexDarkBorder, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(proj, color = NexTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (note.people.isNotEmpty()) {
                        Text("People Involved", style = MaterialTheme.typography.labelSmall, color = NexTextMuted, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            note.people.forEach { person ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NexDarkSurface)
                                        .border(1.dp, NexDarkBorder, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(person, color = NexTextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    if (note.topics.isNotEmpty()) {
                        Text("Topics & Tags", style = MaterialTheme.typography.labelSmall, color = NexTextMuted, fontSize = 11.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            note.topics.forEach { topic ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(NexDarkSurface)
                                        .border(1.dp, NexDarkBorder, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("#$topic", color = NexTextSecondary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Connected Knowledge Graph Linkages
        if (relatedRelationships.isNotEmpty()) {
            item {
                Surface(
                    color = NexDarkSurfaceElevated,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NexDarkBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Hub,
                                contentDescription = null,
                                tint = NexScarletPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Connected Notes (${relatedRelationships.size})",
                                style = MaterialTheme.typography.labelMedium,
                                color = NexTextSecondary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        relatedRelationships.forEach { rel ->
                            val targetId = if (rel.sourceNoteId == note.id) rel.targetNoteId else rel.sourceNoteId
                            val targetNote = allNotes.find { it.id == targetId }
                            val targetTitle = targetNote?.title ?: "Related Note #$targetId"

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(NexDarkSurface)
                                    .border(1.dp, NexDarkBorderSubtle, RoundedCornerShape(8.dp))
                                    .clickable { onOpenRelatedNote(targetId) }
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = targetTitle,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = NexTextPrimary,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = rel.explanation.ifBlank { rel.relationshipType },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NexTextMuted,
                                        fontSize = 11.sp,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Text(
                                    text = "Open →",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NexScarletPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Navigation to Calendar / Follow-ups

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onNavigateToCalendar,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NexDarkSurfaceElevated),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexDarkBorder)
                ) {
                    Icon(imageVector = Icons.Default.CalendarMonth, contentDescription = "Calendar", tint = NexScarletPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Calendar", color = NexTextPrimary, fontSize = 12.sp)
                }

                Button(
                    onClick = onNavigateToFollowUps,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = NexDarkSurfaceElevated),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NexDarkBorder)
                ) {
                    Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = "Follow ups", tint = NexScarletPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Follow-ups", color = NexTextPrimary, fontSize = 12.sp)
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun TranscriptTab(note: NoteEntity) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                color = NexDarkSurfaceElevated,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NexDarkBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Cleaned Transcript",
                            style = MaterialTheme.typography.labelMedium,
                            color = NexTextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NexDarkSurface)
                                .border(1.dp, NexDarkBorderSubtle, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Transcribed",
                                style = MaterialTheme.typography.labelSmall,
                                color = NexTextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val transcriptText = note.cleanedTranscript
                        ?: note.rawTranscript
                        ?: note.originalInput.ifBlank { "No transcript text recorded." }

                    Text(
                        text = transcriptText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NexTextPrimary,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun MediaOcrTab(note: NoteEntity) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // OCR Extracted Text Card
        item {
            Surface(
                color = NexDarkSurfaceElevated,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NexDarkBorder, RoundedCornerShape(14.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "OCR Text Extraction",
                            style = MaterialTheme.typography.labelMedium,
                            color = NexTextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(NexDarkSurface)
                                .border(1.dp, NexDarkBorderSubtle, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Optical Scan",
                                style = MaterialTheme.typography.labelSmall,
                                color = NexTextMuted,
                                fontSize = 10.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val ocrText = if (!note.ocrText.isNullOrBlank()) {
                        note.ocrText
                    } else if (note.sourceType == "IMAGE" || note.sourceType == "SCREENSHOT") {
                        note.originalInput.ifBlank { "No OCR text extracted from this note." }
                    } else {
                        "No OCR text extracted from this note."
                    }

                    Text(
                        text = ocrText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = NexTextPrimary,
                        lineHeight = 22.sp
                    )
                }
            }
        }

        // Media URI Reference
        if (!note.mediaPath.isNullOrBlank()) {
            item {
                Surface(
                    color = NexDarkSurfaceElevated,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NexDarkBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "Media Reference Path",
                            style = MaterialTheme.typography.labelMedium,
                            color = NexTextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = note.mediaPath ?: "",
                            style = MaterialTheme.typography.bodySmall,
                            color = NexTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

private fun formatTime(millis: Long): String {
    val totalSeconds = (millis / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(java.util.Locale.US, "%02d:%02d", minutes, seconds)
}

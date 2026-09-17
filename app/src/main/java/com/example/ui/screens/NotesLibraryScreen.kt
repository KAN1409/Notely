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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NexDarkBackground
import com.example.ui.theme.NexDarkBorder
import com.example.ui.theme.NexDarkSurfaceElevated
import com.example.ui.theme.NexScarletPrimary
import com.example.ui.theme.NexTextMuted
import com.example.ui.theme.NexTextPrimary
import com.example.ui.theme.NexTextSecondary
import com.example.viewmodel.NotelyViewModel

@Composable
fun NotesLibraryScreen(
    viewModel: NotelyViewModel,
    onOpenNote: (Long) -> Unit
) {
    val allNotes by viewModel.allNotes.collectAsState()
    var selectedTypeFilter by remember { mutableStateOf("All") }

    val filtered = when (selectedTypeFilter) {
        "Voice" -> allNotes.filter { it.sourceType == "VOICE" }
        "OCR Docs" -> allNotes.filter { it.sourceType == "IMAGE" || it.sourceType == "SCREENSHOT" }
        "Links" -> allNotes.filter { it.sourceType == "URL" }
        "Text" -> allNotes.filter { it.sourceType == "TEXT" }
        else -> allNotes
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexDarkBackground)
            .padding(horizontal = 16.dp)
            .testTag("notes_library_screen")
    ) {
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = "Notes Library",
            style = MaterialTheme.typography.titleLarge,
            color = NexTextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
        )
        Text(
            text = "${allNotes.size} structured notes in repository",
            style = MaterialTheme.typography.bodySmall,
            color = NexTextSecondary,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Type filter pills
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Voice", "OCR Docs", "Links", "Text").forEach { type ->
                val isSelected = selectedTypeFilter == type
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) NexScarletPrimary else NexDarkSurfaceElevated)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) NexScarletPrimary else NexDarkBorder,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedTypeFilter = type }
                        .padding(horizontal = 14.dp, vertical = 7.dp)
                        .testTag("library_filter_$type")
                ) {
                    Text(
                        text = type,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) Color.White else NexTextSecondary,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        if (filtered.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No notes found in this category.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NexTextMuted
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filtered, key = { it.id }) { note ->
                    NoteCardItem(note = note, onClick = { onOpenNote(note.id) })
                }
                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }
}

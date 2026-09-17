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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
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
fun SearchScreen(
    viewModel: NotelyViewModel,
    onBack: () -> Unit,
    onOpenNote: (Long) -> Unit
) {
    val query by viewModel.searchQuery.collectAsState()
    val filterType by viewModel.searchFilterType.collectAsState()
    val results by viewModel.searchResults.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexDarkBackground)
            .padding(16.dp)
            .testTag("search_screen")
    ) {
        // Search Input Bar
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
                    .testTag("search_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = NexTextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            OutlinedTextField(
                value = query,
                onValueChange = { viewModel.searchQuery.value = it },
                placeholder = { Text("Search notes, people, projects, OCR...", color = NexTextMuted, fontSize = 13.sp) },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = "Search", tint = NexScarletPrimary, modifier = Modifier.size(18.dp))
                },
                trailingIcon = {
                    if (query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.searchQuery.value = "" }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = "Clear", tint = NexTextMuted, modifier = Modifier.size(16.dp))
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("search_text_input"),
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NexScarletPrimary,
                    unfocusedBorderColor = NexDarkBorder,
                    focusedContainerColor = NexDarkSurfaceElevated,
                    unfocusedContainerColor = NexDarkSurfaceElevated,
                    focusedTextColor = NexTextPrimary,
                    unfocusedTextColor = NexTextPrimary
                ),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("All", "Notes", "Images", "Links", "People").forEach { filter ->
                val isSelected = filterType == filter
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) NexScarletPrimary else NexDarkSurfaceElevated)
                        .border(1.dp, if (isSelected) NexScarletPrimary else NexDarkBorder, RoundedCornerShape(12.dp))
                        .clickable { viewModel.searchFilterType.value = filter }
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("search_filter_$filter")
                ) {
                    Text(
                        text = filter,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) Color.White else NexTextSecondary,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Suggested Queries
        if (query.isBlank()) {
            Text("Suggested searches:", style = MaterialTheme.typography.labelSmall, color = NexTextMuted)
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("marble", "Ahmed", "Negma", "lighting", "quotation", "Japandi").forEach { term ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(NexDarkSurfaceElevated)
                            .border(1.dp, NexDarkBorder, RoundedCornerShape(8.dp))
                            .clickable { viewModel.searchQuery.value = term }
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(term, style = MaterialTheme.typography.bodySmall, color = NexTextSecondary, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(14.dp))
        }

        // Search Results List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(results, key = { it.id }) { note ->
                NoteCardItem(note = note, onClick = { onOpenNote(note.id) })
            }

            if (results.isEmpty()) {
                item {
                    Surface(
                        color = NexDarkSurfaceElevated,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NexDarkBorder, RoundedCornerShape(14.dp))
                    ) {
                        Text(
                            text = "No matching notes found.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = NexTextMuted,
                            modifier = Modifier.padding(20.dp)
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(60.dp))
            }
        }
    }
}

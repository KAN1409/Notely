package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import com.example.ui.theme.NexTextPrimary
import com.example.ui.theme.NexTextSecondary
import com.example.viewmodel.NotelyViewModel

@Composable
fun EditNoteScreen(
    viewModel: NotelyViewModel,
    onBack: () -> Unit
) {
    val note by viewModel.selectedNote.collectAsState()

    if (note == null) {
        onBack()
        return
    }

    val cur = note!!
    var title by remember { mutableStateOf(cur.title) }
    var summary by remember { mutableStateOf(cur.summary) }
    var peopleText by remember { mutableStateOf(cur.people.joinToString(", ")) }
    var projectsText by remember { mutableStateOf(cur.projects.joinToString(", ")) }
    var topicsText by remember { mutableStateOf(cur.topics.joinToString(", ")) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexDarkBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("edit_note_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                        .testTag("edit_note_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NexTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Edit Note",
                    style = MaterialTheme.typography.titleMedium,
                    color = NexTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }

            Button(
                onClick = {
                    val updatedPeople = peopleText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val updatedProjects = projectsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    val updatedTopics = topicsText.split(",").map { it.trim() }.filter { it.isNotEmpty() }

                    val updated = cur.copy(
                        title = title,
                        summary = summary,
                        people = updatedPeople,
                        projects = updatedProjects,
                        topics = updatedTopics,
                        tags = (updatedTopics + updatedProjects).distinct(),
                        updatedAt = System.currentTimeMillis()
                    )
                    viewModel.updateNote(updated)
                    onBack()
                },
                colors = ButtonDefaults.buttonColors(containerColor = NexScarletPrimary),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.testTag("btn_save_edited_note")
            ) {
                Icon(imageVector = Icons.Default.Check, contentDescription = "Save", tint = Color.White, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Save", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text("Title", style = MaterialTheme.typography.labelMedium, color = NexTextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            modifier = Modifier.fillMaxWidth().testTag("edit_note_title_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NexScarletPrimary,
                unfocusedBorderColor = NexDarkBorder,
                focusedContainerColor = NexDarkSurfaceElevated,
                unfocusedContainerColor = NexDarkSurfaceElevated,
                focusedTextColor = NexTextPrimary,
                unfocusedTextColor = NexTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text("Summary & Synthesized Insights", style = MaterialTheme.typography.labelMedium, color = NexTextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = summary,
            onValueChange = { summary = it },
            modifier = Modifier.fillMaxWidth().height(130.dp).testTag("edit_note_summary_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NexScarletPrimary,
                unfocusedBorderColor = NexDarkBorder,
                focusedContainerColor = NexDarkSurfaceElevated,
                unfocusedContainerColor = NexDarkSurfaceElevated,
                focusedTextColor = NexTextPrimary,
                unfocusedTextColor = NexTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text("Associated Projects (comma separated)", style = MaterialTheme.typography.labelMedium, color = NexTextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = projectsText,
            onValueChange = { projectsText = it },
            modifier = Modifier.fillMaxWidth().testTag("edit_note_projects_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NexScarletPrimary,
                unfocusedBorderColor = NexDarkBorder,
                focusedContainerColor = NexDarkSurfaceElevated,
                unfocusedContainerColor = NexDarkSurfaceElevated,
                focusedTextColor = NexTextPrimary,
                unfocusedTextColor = NexTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text("People Involved (comma separated)", style = MaterialTheme.typography.labelMedium, color = NexTextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = peopleText,
            onValueChange = { peopleText = it },
            modifier = Modifier.fillMaxWidth().testTag("edit_note_people_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NexScarletPrimary,
                unfocusedBorderColor = NexDarkBorder,
                focusedContainerColor = NexDarkSurfaceElevated,
                unfocusedContainerColor = NexDarkSurfaceElevated,
                focusedTextColor = NexTextPrimary,
                unfocusedTextColor = NexTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text("Topics & Tags (comma separated)", style = MaterialTheme.typography.labelMedium, color = NexTextSecondary)
        Spacer(modifier = Modifier.height(6.dp))
        OutlinedTextField(
            value = topicsText,
            onValueChange = { topicsText = it },
            modifier = Modifier.fillMaxWidth().testTag("edit_note_topics_input"),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NexScarletPrimary,
                unfocusedBorderColor = NexDarkBorder,
                focusedContainerColor = NexDarkSurfaceElevated,
                unfocusedContainerColor = NexDarkSurfaceElevated,
                focusedTextColor = NexTextPrimary,
                unfocusedTextColor = NexTextPrimary
            )
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

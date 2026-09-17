package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.TrendingUp
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mic
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NexAmberAlert
import com.example.ui.theme.NexBlueAccent
import com.example.ui.theme.NexDarkBackground
import com.example.ui.theme.NexDarkBorder
import com.example.ui.theme.NexDarkSurfaceElevated
import com.example.ui.theme.NexGreenSuccess
import com.example.ui.theme.NexScarletPrimary
import com.example.ui.theme.NexTextMuted
import com.example.ui.theme.NexTextPrimary
import com.example.ui.theme.NexTextSecondary
import com.example.viewmodel.NotelyViewModel

@Composable
fun StatsScreen(
    viewModel: NotelyViewModel,
    onBack: () -> Unit
) {
    val allNotes by viewModel.allNotes.collectAsState()
    val allFollowUps by viewModel.allFollowUps.collectAsState()

    val voiceCount = allNotes.count { it.sourceType == "VOICE" }
    val imageCount = allNotes.count { it.sourceType == "IMAGE" || it.sourceType == "SCREENSHOT" }
    val linkCount = allNotes.count { it.sourceType == "URL" }
    val textCount = allNotes.count { it.sourceType == "TEXT" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexDarkBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("stats_screen")
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
                    text = "Intelligence Statistics",
                    style = MaterialTheme.typography.titleMedium,
                    color = NexTextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = "Repository metrics & loop resolution",
                    style = MaterialTheme.typography.bodySmall,
                    color = NexTextSecondary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Total Notes Hero Card
        Surface(
            color = NexDarkSurfaceElevated,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NexDarkBorder, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Total Knowledge Ingested", style = MaterialTheme.typography.labelMedium, color = NexTextSecondary)
                    Icon(imageVector = Icons.AutoMirrored.Filled.TrendingUp, contentDescription = "Growth", tint = NexScarletPrimary, modifier = Modifier.size(20.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${allNotes.size} Notes",
                    style = MaterialTheme.typography.headlineMedium,
                    color = NexTextPrimary,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "${allFollowUps.count { it.status == "RESOLVED" }} resolved loops • ${allFollowUps.count { it.status != "RESOLVED" }} open commitments",
                    style = MaterialTheme.typography.bodySmall,
                    color = NexScarletPrimary,
                    fontSize = 12.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Distribution by Source Type
        Text("Capture Channels", style = MaterialTheme.typography.titleSmall, color = NexTextPrimary, fontWeight = FontWeight.SemiBold)
        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatChannelBox(icon = Icons.Default.Mic, count = voiceCount, label = "Voice", modifier = Modifier.weight(1f))
            StatChannelBox(icon = Icons.Default.Image, count = imageCount, label = "OCR Docs", modifier = Modifier.weight(1f))
            StatChannelBox(icon = Icons.Default.Link, count = linkCount, label = "Links", modifier = Modifier.weight(1f))
            StatChannelBox(icon = Icons.Default.Description, count = textCount, label = "Text", modifier = Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Follow-up Completion Status
        Surface(
            color = NexDarkSurfaceElevated,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NexDarkBorder, RoundedCornerShape(14.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Open Loops & Actionability", style = MaterialTheme.typography.titleSmall, color = NexTextPrimary, fontWeight = FontWeight.SemiBold)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Total Follow-ups Tracked", style = MaterialTheme.typography.bodyMedium, color = NexTextSecondary)
                    Text("${allFollowUps.size}", style = MaterialTheme.typography.bodyMedium, color = NexTextPrimary, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Currently Open / Waiting", style = MaterialTheme.typography.bodyMedium, color = NexTextSecondary)
                    Text("${allFollowUps.count { it.status != "RESOLVED" }}", style = MaterialTheme.typography.bodyMedium, color = NexAmberAlert, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Successfully Closed & Resolved", style = MaterialTheme.typography.bodyMedium, color = NexTextSecondary)
                    Text("${allFollowUps.count { it.status == "RESOLVED" }}", style = MaterialTheme.typography.bodyMedium, color = NexGreenSuccess, fontWeight = FontWeight.Bold)
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
private fun StatChannelBox(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    count: Int,
    label: String,
    modifier: Modifier = Modifier
) {
    Surface(
        color = NexDarkSurfaceElevated,
        shape = RoundedCornerShape(12.dp),
        modifier = modifier.border(1.dp, NexDarkBorder, RoundedCornerShape(12.dp))
    ) {
        Column(
            modifier = Modifier.padding(10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = NexScarletPrimary, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = "$count", style = MaterialTheme.typography.titleSmall, color = NexTextPrimary, fontWeight = FontWeight.Bold)
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = NexTextMuted, fontSize = 10.sp)
        }
    }
}

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material3.Icon
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.AnimatedProcessingPrism
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

@Composable
fun AiProcessingScreen(viewModel: NotelyViewModel) {
    val steps by viewModel.processingSteps.collectAsState()
    val tagline by viewModel.processingTagline.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexDarkBackground)
            .padding(24.dp)
            .testTag("ai_processing_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Text(
                text = "Processing Ingest...",
                style = MaterialTheme.typography.titleLarge,
                color = NexTextPrimary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = tagline,
                style = MaterialTheme.typography.bodyMedium,
                color = NexScarletPrimary,
                textAlign = TextAlign.Center
            )
        }

        // Center Prism Animation
        AnimatedProcessingPrism(modifier = Modifier.padding(vertical = 16.dp))

        // Step Checklist
        Surface(
            color = NexDarkSurfaceElevated,
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, NexDarkBorder, RoundedCornerShape(14.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                steps.forEach { (stepName, isDone) ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    if (isDone) NexGreenSuccess.copy(alpha = 0.2f) else NexDarkSurface
                                )
                                .border(1.dp, if (isDone) NexGreenSuccess else NexDarkBorder, RoundedCornerShape(6.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isDone) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Completed",
                                    tint = NexGreenSuccess,
                                    modifier = Modifier.size(12.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.HourglassEmpty,
                                    contentDescription = "Pending",
                                    tint = NexTextMuted,
                                    modifier = Modifier.size(10.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Text(
                            text = stepName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isDone) NexTextPrimary else NexTextMuted,
                            fontWeight = if (isDone) FontWeight.Medium else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        // Bottom subtext
        Text(
            text = "Analyzing note structure and extracting entities",
            style = MaterialTheme.typography.bodySmall,
            color = NexTextMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 12.dp),
            fontSize = 11.sp
        )
    }
}

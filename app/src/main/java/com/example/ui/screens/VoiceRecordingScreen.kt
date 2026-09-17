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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.WaveformVisualizer
import com.example.ui.theme.NexDarkBackground
import com.example.ui.theme.NexDarkBorder
import com.example.ui.theme.NexDarkBorderSubtle
import com.example.ui.theme.NexDarkSurface
import com.example.ui.theme.NexDarkSurfaceElevated
import com.example.ui.theme.NexScarletBright
import com.example.ui.theme.NexScarletPrimary
import com.example.ui.theme.NexTextMuted
import com.example.ui.theme.NexTextPrimary
import com.example.ui.theme.NexTextSecondary
import com.example.viewmodel.NotelyViewModel

@Composable
fun VoiceRecordingScreen(
    viewModel: NotelyViewModel,
    onCancel: () -> Unit
) {
    val elapsedSeconds by viewModel.recorderHelper.elapsedSeconds.collectAsState()
    val amplitudes by viewModel.recorderHelper.amplitudes.collectAsState()
    val liveTranscript by viewModel.liveRecordingTranscript.collectAsState()
    val selectedLanguage by viewModel.liveTranscriptLanguage.collectAsState()

    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexDarkBackground)
            .padding(16.dp)
            .testTag("voice_recording_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = {
                    viewModel.recorderHelper.stopRecording()
                    onCancel()
                },
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NexDarkSurfaceElevated)
                    .border(1.dp, NexDarkBorder, RoundedCornerShape(10.dp))
                    .testTag("recording_cancel_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cancel",
                    tint = NexTextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Recording Voice",
                    style = MaterialTheme.typography.titleSmall,
                    color = NexScarletBright,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = selectedLanguage,
                    style = MaterialTheme.typography.labelSmall,
                    color = NexTextSecondary,
                    fontSize = 11.sp
                )
            }

            IconButton(
                onClick = {
                    viewModel.liveTranscriptLanguage.value = when (selectedLanguage) {
                        "Arabic • English (Auto)" -> "Arabic (عربي)"
                        "Arabic (عربي)" -> "English"
                        else -> "Arabic • English (Auto)"
                    }
                },
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(NexDarkSurfaceElevated)
                    .border(1.dp, NexDarkBorder, RoundedCornerShape(10.dp))
                    .testTag("recording_lang_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Audio Language",
                    tint = NexTextSecondary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Center Waveform & Timer
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = timeFormatted,
                style = MaterialTheme.typography.headlineLarge,
                color = NexTextPrimary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Waveform canvas
            WaveformVisualizer(
                amplitudes = if (amplitudes.isNotEmpty()) amplitudes else List(24) { (it % 5 + 1) * 0.15f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 24.dp),
                barColor = NexScarletPrimary
            )
        }

        // Live transcription Card
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.GraphicEq,
                            contentDescription = "Live",
                            tint = NexScarletPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Live Transcription",
                            style = MaterialTheme.typography.labelMedium,
                            color = NexTextSecondary,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(NexDarkSurface)
                            .border(1.dp, NexDarkBorderSubtle, RoundedCornerShape(6.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Auto-detect",
                            style = MaterialTheme.typography.labelSmall,
                            color = NexTextMuted,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = liveTranscript.ifBlank { "Listening... Start speaking in Arabic or English to capture your thoughts." },
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (liveTranscript.isNotBlank()) NexTextPrimary else NexTextMuted,
                    lineHeight = 22.sp,
                    textAlign = if (liveTranscript.contains(Regex("""[\u0600-\u06FF]"""))) TextAlign.Right else TextAlign.Left
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = if (liveTranscript.isNotBlank()) "Recognizing live speech..." else "Audio captured • Structured notes generated upon finish",
                    style = MaterialTheme.typography.labelSmall,
                    color = NexTextMuted,
                    fontSize = 11.sp
                )
            }
        }

        // Bottom Stop/Save button
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(NexScarletPrimary)
                    .clickable { viewModel.stopRecordingAndProcess() }
                    .testTag("recording_stop_save_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = "Stop & Save",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Tap to finish & understand",
                style = MaterialTheme.typography.bodySmall,
                color = NexTextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

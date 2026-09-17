package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NexDarkBackground
import com.example.ui.theme.NexDarkBorder
import com.example.ui.theme.NexDarkSurface
import com.example.ui.theme.NexDarkSurfaceElevated
import com.example.ui.theme.NexScarletPrimary
import com.example.ui.theme.NexTextMuted
import com.example.ui.theme.NexTextPrimary
import com.example.ui.theme.NexTextSecondary
import com.example.viewmodel.NotelyViewModel

@Composable
fun CaptureScreen(
    viewModel: NotelyViewModel,
    onClose: () -> Unit,
    onStartVoice: () -> Unit
) {
    var activeInputMode by remember { mutableStateOf("TILES") } // TILES, TEXT, LINK, IMAGE_PROMPT
    var textInput by remember { mutableStateOf("") }
    var linkInput by remember { mutableStateOf("") }
    var imagePromptInput by remember { mutableStateOf("") }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            viewModel.processImageCapture(uri)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(NexDarkBackground)
            .padding(16.dp)
            .testTag("capture_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = {
                        if (activeInputMode != "TILES") {
                            activeInputMode = "TILES"
                        } else {
                            onClose()
                        }
                    },
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(NexDarkSurfaceElevated)
                        .border(1.dp, NexDarkBorder, RoundedCornerShape(10.dp))
                        .testTag("capture_back_button")
                ) {
                    Icon(
                        imageVector = if (activeInputMode != "TILES") Icons.AutoMirrored.Filled.ArrowBack else Icons.Default.Close,
                        contentDescription = "Close",
                        tint = NexTextPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Capture",
                        style = MaterialTheme.typography.titleMedium,
                        color = NexTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                    Text(
                        text = "Ingest ideas, documents & media",
                        style = MaterialTheme.typography.bodySmall,
                        color = NexTextSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (activeInputMode) {
            "TEXT" -> {
                Text(
                    text = "Write a Note",
                    style = MaterialTheme.typography.titleSmall,
                    color = NexTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = textInput,
                    onValueChange = { textInput = it },
                    placeholder = { Text("Meeting with Ahmed regarding Negma marble quotation...", color = NexTextMuted, fontSize = 14.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("capture_text_input"),
                    shape = RoundedCornerShape(14.dp),
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
                Button(
                    onClick = {
                        if (textInput.isNotBlank()) {
                            viewModel.processTextInput(textInput)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("capture_submit_text_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NexScarletPrimary)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Save", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Process & Understand", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }

            "LINK" -> {
                Text(
                    text = "Add a URL",
                    style = MaterialTheme.typography.titleSmall,
                    color = NexTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = linkInput,
                    onValueChange = { linkInput = it },
                    placeholder = { Text("https://example.com/article...", color = NexTextMuted, fontSize = 14.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("capture_link_input"),
                    shape = RoundedCornerShape(14.dp),
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
                Button(
                    onClick = {
                        if (linkInput.isNotBlank()) {
                            viewModel.processUrlCapture(linkInput)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("capture_submit_link_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NexScarletPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Link, contentDescription = "Fetch", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Ingest & Summarize Link", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }

            "IMAGE_PROMPT" -> {
                Text(
                    text = "Scan or Upload Document",
                    style = MaterialTheme.typography.titleSmall,
                    color = NexTextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Upload an image or paste quotation/receipt text below to run OCR extraction:",
                    style = MaterialTheme.typography.bodySmall,
                    color = NexTextSecondary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { photoPickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("capture_pick_gallery_image"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NexDarkSurfaceElevated)
                ) {
                    Icon(imageVector = Icons.Default.Image, contentDescription = "Pick Image", tint = NexTextPrimary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Choose Image from Gallery", color = NexTextPrimary, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text("Or paste OCR text directly:", style = MaterialTheme.typography.labelSmall, color = NexTextSecondary)
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = imagePromptInput,
                    onValueChange = { imagePromptInput = it },
                    placeholder = { Text("QUOTATION #QT-8821\nSupplier: Ahmed Marble...\nPrice: 2,450 EGP / m²", color = NexTextMuted, fontSize = 13.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .testTag("capture_ocr_text_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NexScarletPrimary,
                        unfocusedBorderColor = NexDarkBorder,
                        focusedContainerColor = NexDarkSurfaceElevated,
                        unfocusedContainerColor = NexDarkSurfaceElevated,
                        focusedTextColor = NexTextPrimary,
                        unfocusedTextColor = NexTextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = {
                        val text = imagePromptInput.ifBlank {
                            "QUOTATION #QT-8821\nSupplier: Ahmed Marble & Granite Co.\nProject: Negma Project - Reception\nItem: Galala Cream Marble (Polished)\nUnit Price: 2,450 EGP / m²\nEstimated Quantity: 180 m²\nTotal Estimate: 441,000 EGP\nValidity: 30 September 2026"
                        }
                        viewModel.processImageCapture(null, text)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("capture_submit_ocr_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NexScarletPrimary)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Send, contentDescription = "Run OCR", tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Extract Structured Document", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }

            else -> {
                // 6 Action Tiles
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    item {
                        CaptureTile(
                            icon = Icons.Default.Edit,
                            title = "Write a note",
                            subtitle = "Text & thoughts",
                            testTag = "tile_text",
                            onClick = { activeInputMode = "TEXT" }
                        )
                    }
                    item {
                        CaptureTile(
                            icon = Icons.Default.Mic,
                            title = "Voice recording",
                            subtitle = "Speak & transcribe",
                            testTag = "tile_voice",
                            onClick = onStartVoice
                        )
                    }
                    item {
                        CaptureTile(
                            icon = Icons.Default.CameraAlt,
                            title = "Scan OCR",
                            subtitle = "Receipts & docs",
                            testTag = "tile_camera",
                            onClick = { activeInputMode = "IMAGE_PROMPT" }
                        )
                    }
                    item {
                        CaptureTile(
                            icon = Icons.Default.Image,
                            title = "From gallery",
                            subtitle = "Screenshots & photos",
                            testTag = "tile_gallery",
                            onClick = { photoPickerLauncher.launch("image/*") }
                        )
                    }
                    item {
                        CaptureTile(
                            icon = Icons.Default.Link,
                            title = "Add a URL",
                            subtitle = "Web link & summary",
                            testTag = "tile_link",
                            onClick = { activeInputMode = "LINK" }
                        )
                    }
                    item {
                        CaptureTile(
                            icon = Icons.Default.Share,
                            title = "Quick share",
                            subtitle = "Shared system text",
                            testTag = "tile_share",
                            onClick = { activeInputMode = "TEXT" }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Quick speak bottom section
                Surface(
                    color = NexDarkSurfaceElevated,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NexDarkBorder, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Instant Voice Capture",
                            style = MaterialTheme.typography.titleSmall,
                            color = NexTextPrimary,
                            fontWeight = FontWeight.SemiBold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Real-time transcription & intelligence",
                            style = MaterialTheme.typography.bodySmall,
                            color = NexTextSecondary,
                            fontSize = 11.sp
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(NexScarletPrimary)
                                .clickable { onStartVoice() }
                                .testTag("capture_bottom_pulsating_mic")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Record",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Tap to speak",
                            style = MaterialTheme.typography.labelSmall,
                            color = NexTextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CaptureTile(
    icon: ImageVector,
    title: String,
    subtitle: String,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        color = NexDarkSurfaceElevated,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .height(115.dp)
            .border(1.dp, NexDarkBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(NexDarkSurface)
                    .border(1.dp, NexDarkBorder, RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = NexScarletPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    color = NexTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = NexTextMuted,
                    fontSize = 11.sp
                )
            }
        }
    }
}

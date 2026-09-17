package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NexDarkBorder
import com.example.ui.theme.NexDarkBorderSubtle
import com.example.ui.theme.NexDarkSurface
import com.example.ui.theme.NexDarkSurfaceElevated
import com.example.ui.theme.NexScarletPrimary
import com.example.ui.theme.NexScarletSubtle
import com.example.ui.theme.NexTextMuted
import com.example.ui.theme.NexTextPrimary
import com.example.ui.theme.NexTextSecondary

@Composable
fun NotelyTopBar(
    title: String,
    subtitle: String? = null,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (onBack != null) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .testTag("top_bar_back")
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
            }

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = NexTextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = NexTextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            actions()
        }
    }
}

@Composable
fun NotelyBottomBar(
    currentTab: String,
    onTabSelected: (String) -> Unit,
    onCaptureClick: () -> Unit
) {
    Surface(
        color = NexDarkSurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = NexDarkBorder,
                shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
            ),
        shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            BottomNavItem(
                icon = Icons.Default.Home,
                label = "Home",
                selected = currentTab == "HOME",
                testTag = "nav_home",
                onClick = { onTabSelected("HOME") }
            )
            BottomNavItem(
                icon = Icons.Default.Description,
                label = "Notes",
                selected = currentTab == "NOTES",
                testTag = "nav_notes",
                onClick = { onTabSelected("NOTES") }
            )

            // Center Floating Capture Button (Scarlet Accent)
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(46.dp)
                    .clip(CircleShape)
                    .background(NexScarletPrimary)
                    .clickable { onCaptureClick() }
                    .testTag("nav_capture_center_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Quick Capture",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }

            BottomNavItem(
                icon = Icons.Default.AutoAwesome,
                label = "Copilot",
                selected = currentTab == "ASSISTANT",
                testTag = "nav_assistant",
                onClick = { onTabSelected("ASSISTANT") }
            )
            BottomNavItem(
                icon = Icons.Default.CalendarMonth,
                label = "Schedule",
                selected = currentTab == "CALENDAR",
                testTag = "nav_schedule",
                onClick = { onTabSelected("CALENDAR") }
            )
        }
    }
}

@Composable
fun NexBottomBar(
    currentTab: String,
    onTabSelected: (String) -> Unit,
    onCaptureClick: () -> Unit
) = NotelyBottomBar(currentTab, onTabSelected, onCaptureClick)

@Composable
private fun BottomNavItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    testTag: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = if (selected) NexScarletPrimary else NexTextMuted,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected) NexTextPrimary else NexTextMuted,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 10.sp
        )
    }
}

@Composable
fun ContextChipRow(
    selectedChip: String,
    onChipSelected: (String) -> Unit
) {
    val chips = listOf("All", "Work", "Personal", "Health", "Ideas", "Procurement")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        chips.forEach { chip ->
            val isSelected = selectedChip == chip
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isSelected) NexScarletPrimary else NexDarkSurfaceElevated)
                    .border(
                        width = 1.dp,
                        color = if (isSelected) NexScarletPrimary else NexDarkBorder,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .clickable { onChipSelected(chip) }
                    .padding(horizontal = 14.dp, vertical = 7.dp)
                    .testTag("context_chip_$chip")
            ) {
                Text(
                    text = chip,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) Color.White else NexTextSecondary,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                    fontSize = 12.sp
                )
            }
        }
    }
}

@Composable
fun MetricCard(
    title: String,
    subtitle: String,
    count: String,
    badgeColor: Color,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        color = NexDarkSurfaceElevated,
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(width = 1.dp, color = NexDarkBorder, shape = RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(badgeColor.copy(alpha = 0.12f))
                        .border(1.dp, badgeColor.copy(alpha = 0.25f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = badgeColor,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleSmall,
                        color = NexTextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = NexTextSecondary,
                        fontSize = 12.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(NexDarkSurface)
                        .border(1.dp, NexDarkBorderSubtle, RoundedCornerShape(8.dp))
                        .padding(horizontal = 9.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = count,
                        style = MaterialTheme.typography.labelMedium,
                        color = NexTextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "Open",
                    tint = NexTextMuted,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}

@Composable
fun SourceBadge(sourceType: String) {
    val (icon, text) = when (sourceType) {
        "VOICE" -> Pair(Icons.Default.Mic, "Voice")
        "IMAGE", "SCREENSHOT" -> Pair(Icons.Default.Image, "OCR Document")
        "URL" -> Pair(Icons.Default.Link, "Link Reference")
        else -> Pair(Icons.Default.Description, "Note")
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(NexDarkSurface)
            .border(1.dp, NexDarkBorder, RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = NexTextSecondary,
            modifier = Modifier.size(12.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = NexTextSecondary,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp
        )
    }
}

@Composable
fun WaveformVisualizer(
    amplitudes: List<Float>,
    modifier: Modifier = Modifier,
    barColor: Color = NexScarletPrimary
) {
    Canvas(modifier = modifier) {
        val count = amplitudes.size.coerceAtLeast(1)
        val barWidth = size.width / (count * 1.6f).coerceAtLeast(1f)
        val spacing = barWidth * 0.6f
        val centerY = size.height / 2f

        amplitudes.forEachIndexed { index, amp ->
            val barHeight = (amp * size.height * 0.85f).coerceAtLeast(6f)
            val x = index * (barWidth + spacing) + spacing
            drawLine(
                color = barColor,
                start = Offset(x, centerY - (barHeight / 2f)),
                end = Offset(x, centerY + (barHeight / 2f)),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun AnimatedProcessingPrism(modifier: Modifier = Modifier) {
    val transition = rememberInfiniteTransition(label = "prism")
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )
    val pulse by transition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(120.dp)
            .scale(pulse)
    ) {
        Canvas(modifier = Modifier.size(110.dp).rotate(rotation)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension / 2.4f

            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NexScarletPrimary.copy(alpha = 0.25f),
                        NexDarkBorder.copy(alpha = 0.10f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = radius * 1.2f
                )
            )

            drawCircle(
                color = NexDarkBorder,
                radius = radius,
                style = Stroke(width = 2f)
            )

            drawCircle(
                color = NexScarletPrimary,
                radius = radius * 0.7f,
                style = Stroke(width = 3f)
            )
        }

        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(NexScarletPrimary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = "Intelligence",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

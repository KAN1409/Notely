package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val NexDarkColorScheme = darkColorScheme(
    primary = NexScarletPrimary,
    onPrimary = NexDarkBackground,
    primaryContainer = NexScarletDark,
    onPrimaryContainer = NexTextPrimary,
    secondary = NexScarletBright,
    onSecondary = NexDarkBackground,
    tertiary = NexBlueAccent,
    onTertiary = NexTextPrimary,
    background = NexDarkBackground,
    onBackground = NexTextPrimary,
    surface = NexDarkSurface,
    onSurface = NexTextPrimary,
    surfaceVariant = NexDarkSurfaceElevated,
    onSurfaceVariant = NexTextSecondary,
    outline = NexDarkBorder,
    error = NexScarletBright
)

private val NexLightColorScheme = darkColorScheme(
    primary = NexScarletPrimary,
    onPrimary = NexDarkBackground,
    background = NexDarkBackground,
    onBackground = NexTextPrimary,
    surface = NexDarkSurfaceElevated,
    onSurface = NexTextPrimary,
    surfaceVariant = NexDarkSurfaceHigher,
    onSurfaceVariant = NexTextSecondary,
    outline = NexDarkBorder,
    error = NexScarletBright
)

@Composable
fun NotelyTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) NexDarkColorScheme else NexLightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun NexNoteTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) = NotelyTheme(darkTheme = darkTheme, content = content)

@Composable
fun NexTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) = NotelyTheme(darkTheme = darkTheme, content = content)

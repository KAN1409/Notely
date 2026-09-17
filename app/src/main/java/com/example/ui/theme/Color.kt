package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// ==========================================
// NexNote Architectural Dark Palette
// 70% OLED Black • 20% Graphite/Charcoal • 5% Subtle Gray Borders • 5% Scarlet Accent
// ==========================================

// OLED Canvas & Charcoal Surfaces
val NexDarkBackground = Color(0xFF050505)       // 70% Pure OLED Black
val NexDarkSurface = Color(0xFF101112)          // 20% Deep Charcoal Surface
val NexDarkSurfaceElevated = Color(0xFF16181A)  // Layered Charcoal Card Surface
val NexDarkSurfaceHigher = Color(0xFF1D1F21)    // High Elevation Surface

// Structural Borders & Lines
val NexDarkBorder = Color(0xFF26282B)           // 5% Subtle Gray Border
val NexDarkBorderSubtle = Color(0xFF1C1E20)     // Low-contrast Boundary

// Scarlet Identity Accent (5% Accent only)
val NexScarletPrimary = Color(0xFFE52B32)       // Core Scarlet Accent
val NexScarletBright = Color(0xFFF2383F)        // Vibrant Indicator
val NexScarletDark = Color(0xFF9E1B20)          // Deep Container Scarlet
val NexScarletSubtle = Color(0x28E52B32)        // Low-opacity Scarlet Tint

// Semantic Status Indicators (Clean, Muted)
val NexGreenSuccess = Color(0xFF2ECA6A)         // Resolved / Success Emerald
val NexAmberAlert = Color(0xFFE5982B)           // Warning / Due Amber
val NexCoralRed = Color(0xFFF2383F)             // Overdue / Destructive Scarlet
val NexBlueAccent = Color(0xFF3B82F6)           // Informational Blue
val NexPurpleAccent = Color(0xFF8B5CF6)         // Media/OCR Accent
val NexCyanAccent = Color(0xFF06B6D4)           // Voice Accent

// High-Contrast Typography
val NexTextPrimary = Color(0xFFF0F1F3)          // Crisp Crisp Off-White
val NexTextSecondary = Color(0xFF8E9298)        // Neutral Secondary Text
val NexTextMuted = Color(0xFF55585E)            // Subdued Caption/Placeholder

// Backward Compatibility Aliases for Notely*
val NotelyDarkBackground = NexDarkBackground
val NotelyDarkSurface = NexDarkSurface
val NotelyDarkSurfaceElevated = NexDarkSurfaceElevated
val NotelyDarkSurfaceHigher = NexDarkSurfaceHigher
val NotelyDarkBorder = NexDarkBorder
val NotelyTealPrimary = NexScarletPrimary
val NotelyTealDark = NexScarletDark
val NotelyCyanAccent = NexCyanAccent
val NotelyBlueAccent = NexBlueAccent
val NotelyPurpleAccent = NexPurpleAccent
val NotelyAmberAlert = NexAmberAlert
val NotelyCoralRed = NexCoralRed
val NotelyGreenSuccess = NexGreenSuccess
val NotelyTextPrimary = NexTextPrimary
val NotelyTextSecondary = NexTextSecondary
val NotelyTextMuted = NexTextMuted

// Daylight Toggle Fallbacks
val NotelyLightBackground = Color(0xFF0C0D0E)
val NotelyLightSurface = Color(0xFF141517)
val NotelyLightSurfaceElevated = Color(0xFF1B1D1F)
val NotelyLightBorder = Color(0xFF2A2D30)
val NotelyLightTextPrimary = Color(0xFFF0F1F3)
val NotelyLightTextSecondary = Color(0xFF8E9298)

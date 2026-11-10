package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val GhostLightColorScheme = lightColorScheme(
    primary = GhostPrimary,
    onPrimary = GhostOnPrimary,
    primaryContainer = GhostPrimaryContainer,
    onPrimaryContainer = GhostOnPrimaryContainer,
    secondary = GhostSecondary,
    onSecondary = GhostOnSecondary,
    secondaryContainer = GhostSecondaryContainer,
    onSecondaryContainer = GhostOnSecondaryContainer,
    tertiary = GhostOnTertiaryFixedVariant,
    onTertiary = GhostOnPrimary,
    tertiaryContainer = GhostTertiaryFixed,
    onTertiaryContainer = GhostOnTertiaryFixed,
    background = GhostSurface,
    onBackground = GhostOnSurface,
    surface = GhostSurface,
    onSurface = GhostOnSurface,
    surfaceVariant = GhostSurfaceContainerHighest,
    onSurfaceVariant = GhostOnSurfaceVariant,
    surfaceContainerLowest = GhostSurfaceContainerLowest,
    surfaceContainerLow = GhostSurfaceContainerLow,
    surfaceContainer = GhostSurfaceContainer,
    surfaceContainerHigh = GhostSurfaceContainerHigh,
    surfaceContainerHighest = GhostSurfaceContainerHighest,
    inverseSurface = GhostInverseSurface,
    inverseOnSurface = GhostInverseOnSurface,
    outline = GhostOutline,
    outlineVariant = GhostOutlineVariant,
    error = GhostError,
    errorContainer = GhostErrorContainer
)

@Composable
fun GhostPoseTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = GhostLightColorScheme,
        typography = Typography,
        content = content
    )
}

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    GhostPoseTheme(content = content)
}

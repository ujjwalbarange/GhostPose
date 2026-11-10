package com.example

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.FilterCenterFocus
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Opacity
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.ui.theme.GhostInverseOnSurface
import com.example.ui.theme.GhostInverseSurface
import com.example.ui.theme.GhostOnPrimary
import com.example.ui.theme.GhostOnSecondaryContainer
import com.example.ui.theme.GhostOnSurface
import com.example.ui.theme.GhostOnSurfaceVariant
import com.example.ui.theme.GhostOnTertiaryContainer
import com.example.ui.theme.GhostOnTertiaryFixedVariant
import com.example.ui.theme.GhostOutline
import com.example.ui.theme.GhostOutlineVariant
import com.example.ui.theme.GhostPrimary
import com.example.ui.theme.GhostPrimaryFixedDim
import com.example.ui.theme.GhostSecondary
import com.example.ui.theme.GhostSecondaryContainer
import com.example.ui.theme.GhostSurfaceContainer
import com.example.ui.theme.GhostSurfaceContainerHigh
import com.example.ui.theme.GhostSurfaceContainerHighest
import com.example.ui.theme.GhostSurfaceContainerLow
import com.example.ui.theme.GhostSurfaceContainerLowest
import com.example.ui.theme.GhostTertiaryFixed
import kotlin.math.roundToInt

/**
 * Draws a subtle 3x3 rule-of-thirds alignment grid when Crop & Align is toggled on.
 * Stick-man silhouette removed per user request.
 */
@Composable
fun NeonGhostSilhouetteAndGrid(
    showGridAndGuide: Boolean,
    alpha: Float,
    isMirrored: Boolean,
    modifier: Modifier = Modifier
) {
    if (!showGridAndGuide) return
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val gridColor = Color.White.copy(alpha = 0.28f)

            // 3x3 Viewfinder Grid Lines
            drawLine(gridColor, Offset(w / 3f, 0f), Offset(w / 3f, h), strokeWidth = 1.dp.toPx())
            drawLine(gridColor, Offset(2f * w / 3f, 0f), Offset(2f * w / 3f, h), strokeWidth = 1.dp.toPx())
            drawLine(gridColor, Offset(0f, h / 3f), Offset(w, h / 3f), strokeWidth = 1.dp.toPx())
            drawLine(gridColor, Offset(0f, 2f * h / 3f), Offset(w, 2f * h / 3f), strokeWidth = 1.dp.toPx())
        }
    }
}

/**
 * Top Floating Pill Anchor ("Ghost Pose Menu") + Controls Toggle + Cross (Close) Button
 * with confirmation dialog before closing the overlay.
 * Optimized: Zero infinite transitions so idle CPU/GPU and battery usage remain 0%.
 */
@Composable
fun FloatingOverlayTopBar(
    isLockedPassthrough: Boolean,
    isHudExpanded: Boolean,
    onToggleLockMode: () -> Unit,
    onToggleExpandHud: () -> Unit,
    onCloseOverlay: () -> Unit,
    onDragAnchor: (Float, Float) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var showCloseConfirmation by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left Anchor: Ghost Pose Menu Pill (Draggable + Tappable to toggle Locked/Edit mode)
            Surface(
                shape = CircleShape,
                color = GhostSurfaceContainerLowest.copy(alpha = 0.94f),
                shadowElevation = 4.dp,
                modifier = Modifier
                    .testTag("floating_controller_anchor")
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onDragAnchor(dragAmount.x, dragAmount.y)
                        }
                    }
                    .clickable { onToggleLockMode() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(10.dp)) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(GhostTertiaryFixed.copy(alpha = 0.45f))
                        )
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isLockedPassthrough) GhostOnTertiaryFixedVariant else Color(0xFFD97706)
                                )
                        )
                    }
                    Text(
                        text = "Ghost Pose Menu",
                        style = MaterialTheme.typography.labelMedium,
                        color = GhostOnSurface,
                        maxLines = 1,
                        softWrap = false
                    )
                    Icon(
                        imageVector = if (isLockedPassthrough) Icons.Default.Lock else Icons.Default.LockOpen,
                        contentDescription = if (isLockedPassthrough) "Locked Passthrough" else "Edit Mode Unlocked",
                        tint = if (isLockedPassthrough) GhostOnTertiaryFixedVariant else GhostSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            // Right Controls Toggle Pill + Cross (X) Close Button
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Surface(
                    shape = CircleShape,
                    color = GhostSurfaceContainerLowest.copy(alpha = 0.94f),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .testTag("expand_hud_badge")
                        .clickable { onToggleExpandHud() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sensors,
                            contentDescription = "Toggle Controls",
                            tint = GhostOnTertiaryContainer,
                            modifier = Modifier.size(15.dp)
                        )
                        Text(
                            text = if (isHudExpanded) "Hide Panel" else "Controls",
                            style = MaterialTheme.typography.labelMedium,
                            color = GhostOnSurface,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }

                Surface(
                    shape = CircleShape,
                    color = GhostInverseSurface.copy(alpha = 0.90f),
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .size(36.dp)
                        .testTag("close_overlay_top_button")
                        .clickable { showCloseConfirmation = true }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Overlay",
                            tint = GhostInverseOnSurface,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Confirmation Card when user taps the Cross (X) button
        if (showCloseConfirmation) {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = GhostSurfaceContainerLowest.copy(alpha = 0.97f),
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("close_overlay_confirm_card")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Close Overlay?",
                        style = MaterialTheme.typography.headlineSmall,
                        color = GhostOnSurface
                    )
                    Text(
                        text = "Are you sure you want to close the GhostPose camera overlay?",
                        style = MaterialTheme.typography.bodySmall,
                        color = GhostSecondary
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp, Alignment.End)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = GhostSurfaceContainer,
                            modifier = Modifier
                                .testTag("cancel_close_overlay_button")
                                .clickable { showCloseConfirmation = false }
                        ) {
                            Text(
                                text = "No, Keep Open",
                                style = MaterialTheme.typography.labelMedium,
                                color = GhostOnSurface,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = GhostPrimary,
                            modifier = Modifier
                                .testTag("confirm_close_overlay_button")
                                .clickable {
                                    showCloseConfirmation = false
                                    onCloseOverlay()
                                }
                        ) {
                            Text(
                                text = "Yes, Close",
                                style = MaterialTheme.typography.labelMedium,
                                color = GhostOnPrimary,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Floating Controller HUD Drawer Card with hold-and-drag minus handle ("—").
 * Optimized for WindowManager: avoids per-frame layout height resizing (expandVertically/animateContentSize)
 * so SurfaceFlinger never stretches/distorts icons or text during HUD toggle.
 */
@Composable
fun FloatingOverlayControlsCard(
    alpha: Float,
    isMirrored: Boolean,
    isLockedPassthrough: Boolean,
    showGridAndGuide: Boolean,
    isHudMinimized: Boolean,
    onAlphaChange: (Float) -> Unit,
    onToggleMirror: () -> Unit,
    onResetTransform: () -> Unit,
    onToggleGridAndGuide: () -> Unit,
    onToggleLockPassthrough: () -> Unit,
    onOpenSavePoseModal: () -> Unit,
    onToggleMinimizeHud: () -> Unit,
    onCloseOverlay: () -> Unit,
    onDragPanel: (Float, Float) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    var showBottomCloseConfirmation by remember { mutableStateOf(false) }
    var isDraggingPanel by remember { mutableStateOf(false) }

    val handleWidth = if (isDraggingPanel) 36.dp else 28.dp
    val handleHeight = if (isDraggingPanel) 5.dp else 4.dp
    val handleColor = if (isDraggingPanel) GhostPrimary else GhostOutlineVariant
    val lockButtonColor = if (isLockedPassthrough) GhostOnTertiaryFixedVariant else GhostPrimary
    val flipBgColor = if (isMirrored) GhostInverseSurface else GhostSurfaceContainerLow
    val gridBgColor = if (showGridAndGuide) GhostInverseSurface else GhostSurfaceContainerLow

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Main Frosted Controller Panel Card (static elevation to avoid per-frame shadow reallocation)
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = GhostSurfaceContainerLowest.copy(alpha = 0.96f),
            shadowElevation = 10.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Strip: Hold-and-drag minus sign pill + Title, Hide HUD Toggle, Close
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Draggable Minus Sign ("—") & Title Handle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .testTag("overlay_controls_drag_handle")
                            .pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { isDraggingPanel = true },
                                    onDragEnd = { isDraggingPanel = false },
                                    onDragCancel = { isDraggingPanel = false }
                                ) { change, dragAmount ->
                                    change.consume()
                                    onDragPanel(dragAmount.x, dragAmount.y)
                                }
                            }
                            .padding(vertical = 6.dp, horizontal = 4.dp)
                    ) {
                        // Tactile Minus Sign Drag Pill
                        Box(
                            modifier = Modifier
                                .size(width = 38.dp, height = 24.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isDraggingPanel) GhostSurfaceContainerHigh else GhostSurfaceContainerLow
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(width = handleWidth, height = handleHeight)
                                    .clip(CircleShape)
                                    .background(handleColor)
                            )
                        }
                        Text(
                            text = "Overlay Controls",
                            style = MaterialTheme.typography.headlineSmall,
                            color = GhostOnSurface,
                            maxLines = 1,
                            softWrap = false
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = GhostSurfaceContainer,
                            modifier = Modifier
                                .testTag("toggle_hud_button")
                                .clickable { onToggleMinimizeHud() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = if (isHudMinimized) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                    contentDescription = "Toggle HUD Visibility",
                                    tint = GhostOnSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                                Text(
                                    text = if (isHudMinimized) "Expand HUD" else "Hide HUD",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = GhostOnSurfaceVariant,
                                    maxLines = 1,
                                    softWrap = false
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = GhostSurfaceContainer,
                            modifier = Modifier
                                .size(28.dp)
                                .testTag("close_overlay_button")
                                .clickable { showBottomCloseConfirmation = true }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Close Overlay",
                                    tint = GhostOnSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }

                if (showBottomCloseConfirmation) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = GhostSurfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Close overlay?",
                                style = MaterialTheme.typography.labelLarge,
                                color = GhostOnSurface
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Surface(
                                    shape = CircleShape,
                                    color = GhostSurfaceContainerHigh,
                                    modifier = Modifier.clickable { showBottomCloseConfirmation = false }
                                ) {
                                    Text(
                                        text = "No",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = GhostOnSurface,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                                Surface(
                                    shape = CircleShape,
                                    color = GhostPrimary,
                                    modifier = Modifier.clickable {
                                        showBottomCloseConfirmation = false
                                        onCloseOverlay()
                                    }
                                ) {
                                    Text(
                                        text = "Yes, Close",
                                        style = MaterialTheme.typography.labelMedium,
                                        color = GhostOnPrimary,
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Instant single-frame expand/collapse inside WindowManager so icons/text never distort
                if (!isHudMinimized) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Opacity Slider Row
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = GhostSurfaceContainerLow,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Opacity,
                                            contentDescription = null,
                                            tint = GhostOnSurfaceVariant,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Guide Opacity",
                                            style = MaterialTheme.typography.labelMedium,
                                            color = GhostOnSurfaceVariant
                                        )
                                    }
                                    Surface(
                                        shape = CircleShape,
                                        color = GhostPrimary
                                    ) {
                                        Text(
                                            text = "${(alpha * 100).roundToInt()}%",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = GhostOnPrimary,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }

                                Slider(
                                    value = alpha.coerceIn(0.1f, 1.0f),
                                    onValueChange = onAlphaChange,
                                    valueRange = 0.1f..1.0f,
                                    colors = SliderDefaults.colors(
                                        thumbColor = GhostPrimary,
                                        activeTrackColor = GhostPrimary,
                                        inactiveTrackColor = GhostSurfaceContainerHighest
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(32.dp)
                                        .testTag("opacity_slider")
                                )

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Faint 10%", style = MaterialTheme.typography.labelSmall, color = GhostOutline)
                                    Text("Balanced", style = MaterialTheme.typography.labelSmall, color = GhostOutline)
                                    Text("Solid 100%", style = MaterialTheme.typography.labelSmall, color = GhostOutline)
                                }
                            }
                        }

                        // Transform Quick Controls Tool Strip (3-Column Grid)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // 1. Flip Horizontal Toggle
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = flipBgColor,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("flip_horizontal_button")
                                    .clickable { onToggleMirror() }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Flip,
                                        contentDescription = "Flip Horizontal",
                                        tint = if (isMirrored) GhostTertiaryFixed else GhostSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = if (isMirrored) "Flip H (Mirrored)" else "Flip H (Standard)",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isMirrored) GhostInverseOnSurface else GhostOnSurface,
                                        maxLines = 1,
                                        softWrap = false,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }

                            // 2. Reset Scale Action
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = GhostSurfaceContainerLow,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("reset_scale_button")
                                    .clickable { onResetTransform() }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.RestartAlt,
                                        contentDescription = "Reset Scale",
                                        tint = GhostSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Reset Scale",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GhostOnSurface,
                                        maxLines = 1
                                    )
                                }
                            }

                            // 3. Crop & Align / Grid Action
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = gridBgColor,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("crop_align_button")
                                    .clickable { onToggleGridAndGuide() }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Crop,
                                        contentDescription = "Crop and Align Grid",
                                        tint = if (showGridAndGuide) GhostTertiaryFixed else GhostSecondary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "Crop & Align",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (showGridAndGuide) GhostInverseOnSurface else GhostOnSurface,
                                        maxLines = 1
                                    )
                                }
                            }
                        }

                        // Primary Action (Lock Pose / Passthrough) & Save Action Module
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = lockButtonColor,
                                shadowElevation = 4.dp,
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("lock_pose_passthrough_button")
                                    .clickable { onToggleLockPassthrough() }
                            ) {
                                Column(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isLockedPassthrough) Icons.Default.CheckCircle else Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = GhostTertiaryFixed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = if (isLockedPassthrough) "Passthrough Active" else "Lock Pose (Passthrough)",
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = GhostOnPrimary
                                        )
                                    }
                                    Text(
                                        text = if (isLockedPassthrough) {
                                            "Tap viewfinder anywhere to shoot"
                                        } else {
                                            "Taps pass directly to shutter"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GhostPrimaryFixedDim
                                    )
                                }
                            }

                            // Save Pose Button
                            Surface(
                                shape = CircleShape,
                                color = GhostSurfaceContainer,
                                shadowElevation = 2.dp,
                                modifier = Modifier
                                    .size(50.dp)
                                    .testTag("open_save_pose_modal_button")
                                    .clickable { onOpenSavePoseModal() }
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.BookmarkAdd,
                                        contentDescription = "Save Pose to Library",
                                        tint = GhostOnSurface,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Slide-up Bottom Sheet Modal for saving a reference pose (`ghostpose_save_pose_modal_fbb19db8.html` / Screenshot 3).
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SavePoseModalSheet(
    initialName: String,
    imageUri: String,
    currentAlpha: Float,
    isMirrored: Boolean,
    onSavePose: (name: String, category: String, tagBadge: String, quickShortcut: Boolean) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    var poseName by remember(initialName) {
        mutableStateOf(if (initialName.isBlank()) "Cafe Side Profile" else initialName)
    }
    val availableCategories = remember {
        mutableStateListOf("Casual", "Streetwear", "Group", "Golden Hour", "Portraits", "Full Body")
    }
    val selectedCategories = remember {
        mutableStateListOf("Casual", "Golden Hour")
    }
    var quickShortcutEnabled by remember { mutableStateOf(true) }
    var showAddTagField by remember { mutableStateOf(false) }
    var customTagInput by remember { mutableStateOf("") }

    Surface(
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        color = GhostSurfaceContainerLowest,
        shadowElevation = 24.dp,
        modifier = modifier
            .fillMaxWidth()
            .testTag("save_pose_modal_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Drag Handle Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(GhostSurfaceContainerHighest)
                )
            }

            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "Save Reference Pose",
                            style = MaterialTheme.typography.headlineMedium,
                            color = GhostOnSurface
                        )
                        Box(
                            modifier = Modifier
                                .size(22.dp)
                                .clip(CircleShape)
                                .background(GhostSecondaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoFixHigh,
                                contentDescription = null,
                                tint = GhostOnSecondaryContainer,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Add this pose to your library for instant viewfinder overlay anytime.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GhostSecondary
                    )
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(GhostSurfaceContainerLow)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close modal",
                        tint = GhostSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pose Preview Thumbnail Section
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(136.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(GhostSurfaceContainer)
                ) {
                    if (imageUri.isNotBlank()) {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = "Pose preview thumbnail",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxSize()
                                .scale(scaleX = if (isMirrored) -1f else 1f, scaleY = 1f)
                        )
                    } else {
                        NeonGhostSilhouetteAndGrid(
                            showGridAndGuide = false,
                            alpha = 0.85f,
                            isMirrored = isMirrored
                        )
                    }

                    // Ratio 1:1 badge
                    Surface(
                        shape = CircleShape,
                        color = GhostInverseSurface.copy(alpha = 0.85f),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(GhostTertiaryFixed)
                            )
                            Text(
                                text = "Ratio 1:1",
                                style = MaterialTheme.typography.labelSmall,
                                color = GhostOnPrimary
                            )
                        }
                    }

                    // AI Locked / Opacity badge
                    Surface(
                        shape = CircleShape,
                        color = GhostInverseSurface.copy(alpha = 0.85f),
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterCenterFocus,
                                contentDescription = null,
                                tint = GhostOnPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "${(currentAlpha * 100).roundToInt()}% Locked",
                                style = MaterialTheme.typography.labelSmall,
                                color = GhostOnPrimary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Pose Name Input Field
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Pose Name",
                    style = MaterialTheme.typography.labelMedium,
                    color = GhostOnSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "Captured Just Now",
                    style = MaterialTheme.typography.labelSmall,
                    color = GhostSecondary
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            OutlinedTextField(
                value = poseName,
                onValueChange = { poseName = it },
                singleLine = true,
                shape = CircleShape,
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        tint = GhostSecondary,
                        modifier = Modifier.size(18.dp)
                    )
                },
                trailingIcon = {
                    if (poseName.isNotEmpty()) {
                        IconButton(onClick = { poseName = "" }) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Clear pose name",
                                tint = GhostSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = GhostSurfaceContainer,
                    unfocusedContainerColor = GhostSurfaceContainerLow,
                    focusedBorderColor = GhostPrimary,
                    unfocusedBorderColor = Color.Transparent
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pose_name_input")
            )

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Keep it descriptive to search easily in live shoot mode.",
                style = MaterialTheme.typography.bodySmall,
                color = GhostSecondary,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Category Selector Chips
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Category",
                    style = MaterialTheme.typography.labelMedium,
                    color = GhostOnSurface,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${selectedCategories.size} Selected",
                    style = MaterialTheme.typography.labelSmall,
                    color = GhostSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                availableCategories.forEach { category ->
                    val isSelected = selectedCategories.contains(category)
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) GhostPrimary else GhostSurfaceContainer,
                        modifier = Modifier.clickable {
                            if (isSelected && selectedCategories.size > 1) {
                                selectedCategories.remove(category)
                            } else if (!isSelected) {
                                selectedCategories.add(category)
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = GhostOnPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = category,
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isSelected) GhostOnPrimary else GhostSecondary
                            )
                        }
                    }
                }

                // + Custom Tag Chip
                Surface(
                    shape = CircleShape,
                    color = GhostSurfaceContainerLow,
                    modifier = Modifier.clickable { showAddTagField = !showAddTagField }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = GhostSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Custom Tag",
                            style = MaterialTheme.typography.labelMedium,
                            color = GhostSecondary
                        )
                    }
                }
            }

            if (showAddTagField) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = customTagInput,
                        onValueChange = { customTagInput = it },
                        placeholder = { Text("New tag name...") },
                        singleLine = true,
                        shape = CircleShape,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = CircleShape,
                        color = GhostPrimary,
                        modifier = Modifier.clickable {
                            val trimmed = customTagInput.trim()
                            if (trimmed.isNotEmpty() && !availableCategories.contains(trimmed)) {
                                availableCategories.add(trimmed)
                                selectedCategories.add(trimmed)
                                customTagInput = ""
                                showAddTagField = false
                            }
                        }
                    ) {
                        Text(
                            text = "Add",
                            color = GhostOnPrimary,
                            style = MaterialTheme.typography.labelMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Collection / Shortcuts Toggle Row
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = GhostSurfaceContainerLow,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = GhostSurfaceContainerLowest,
                            shadowElevation = 1.dp,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = GhostPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Add to Quick Overlay Shortcuts",
                                style = MaterialTheme.typography.headlineSmall,
                                color = GhostOnSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "Pins pose inside camera HUD dock",
                                style = MaterialTheme.typography.bodySmall,
                                color = GhostSecondary
                            )
                        }
                    }

                    Switch(
                        checked = quickShortcutEnabled,
                        onCheckedChange = { quickShortcutEnabled = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = GhostSurfaceContainerLowest,
                            checkedTrackColor = GhostPrimary,
                            uncheckedThumbColor = GhostSurfaceContainerLowest,
                            uncheckedTrackColor = GhostSurfaceContainerHighest
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Primary Save to Library CTA
            Surface(
                shape = CircleShape,
                color = GhostPrimary,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("confirm_save_pose_button")
                    .clickable {
                        val finalName = poseName.trim().ifEmpty { "Saved Pose" }
                        val primaryCategory = selectedCategories.firstOrNull {
                            it in listOf("Portraits", "Full Body", "Aesthetic")
                        } ?: "Portraits"
                        val badge = selectedCategories.firstOrNull() ?: "Studio"
                        onSavePose(finalName, primaryCategory, badge, quickShortcutEnabled)
                    }
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = GhostOnPrimary,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Save to Library",
                        style = MaterialTheme.typography.headlineSmall,
                        color = GhostOnPrimary
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Secondary Discard Pose Button
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(42.dp)
                    .clip(CircleShape)
                    .clickable { onDismiss() },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Discard Pose",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = GhostSecondary
                )
            }
        }
    }
}

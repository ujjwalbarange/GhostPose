package com.example

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.format.DateUtils
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.ui.unit.IntOffset
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterCenterFocus
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MotionPhotosOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.StopCircle
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.GhostInverseOnSurface
import com.example.ui.theme.GhostInverseSurface
import com.example.ui.theme.GhostOnPrimary
import com.example.ui.theme.GhostOnSurface
import com.example.ui.theme.GhostOnSurfaceVariant
import com.example.ui.theme.GhostOnTertiaryFixedVariant
import com.example.ui.theme.GhostOutline
import com.example.ui.theme.GhostPrimary
import com.example.ui.theme.GhostPrimaryContainer
import com.example.ui.theme.GhostSecondary
import com.example.ui.theme.GhostSurface
import com.example.ui.theme.GhostSurfaceContainer
import com.example.ui.theme.GhostSurfaceContainerHigh
import com.example.ui.theme.GhostSurfaceContainerLow
import com.example.ui.theme.GhostSurfaceContainerLowest
import com.example.ui.theme.GhostTertiaryFixed
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PoseHistoryScreen(
    poseViewModel: PoseViewModel = viewModel()
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val lifecycleOwner = LocalLifecycleOwner.current

    val allPoses by poseViewModel.allPoses.collectAsState()
    val filteredPoses by poseViewModel.filteredPoses.collectAsState()
    val selectedCategory by poseViewModel.selectedCategory.collectAsState()
    val isResolvingUrl by poseViewModel.isResolvingUrl.collectAsState()
    val urlError by poseViewModel.urlError.collectAsState()
    val activeOverlayState by PoseOverlayService.overlayState.collectAsState()

    var selectedBottomTab by remember { mutableIntStateOf(0) } // 0 = Poses, 1 = Camera Tools, 2 = Saved / Profile
    var canDrawOverlays by remember { mutableStateOf(Settings.canDrawOverlays(context)) }

    // Dialog / BottomSheet States
    var showPasteLinkDialog by remember { mutableStateOf(false) }
    var showPermissionDialog by remember { mutableStateOf(false) }
    var pendingOverlayPose by remember { mutableStateOf<PoseEntity?>(null) }
    var editingPose by remember { mutableStateOf<PoseEntity?>(null) }
    var draftSavePoseUri by remember { mutableStateOf<String?>(null) }
    var draftSavePoseName by remember { mutableStateOf("") }

    // Keep overlay permission status fresh on resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                canDrawOverlays = Settings.canDrawOverlays(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    // BackHandler for secondary tabs or Save Pose sheet
    BackHandler(enabled = draftSavePoseUri != null || selectedBottomTab != 0) {
        if (draftSavePoseUri != null) {
            draftSavePoseUri = null
        } else {
            selectedBottomTab = 0
        }
    }

    // Notification permission launcher (Android 13+)
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
        pendingOverlayPose?.let { pose ->
            PoseOverlayService.launchOverlay(context, pose)
            pendingOverlayPose = null
        }
    }

    // System Overlay permission launcher
    val overlaySettingsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        canDrawOverlays = Settings.canDrawOverlays(context)
        val pose = pendingOverlayPose
        if (canDrawOverlays && pose != null) {
            PoseOverlayService.launchOverlay(context, pose)
            Toast.makeText(context, "Overlay active! Open any camera app to shoot.", Toast.LENGTH_LONG).show()
            pendingOverlayPose = null
        }
    }

    // Zero-permission Android Photo Picker for importing gallery pose references
    val photoPickerLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            coroutineScope.launch {
                val cachedUri = ShareReceiverActivity.copySharedUriToInternalStorage(context, uri)
                    ?: uri.toString()
                draftSavePoseName = "Gallery Reference Pose"
                draftSavePoseUri = cachedUri
            }
        }
    }

    fun requestLaunchOverlay(pose: PoseEntity) {
        // Also load into the in-app Viewfinder HUD simulator so user sees immediate state update
        PoseOverlayService.updateSimulatorState {
            it.copy(
                poseId = pose.id,
                poseName = pose.name,
                imageUri = pose.imageUri,
                alpha = pose.alpha,
                isMirrored = pose.isMirrored,
                scale = 1f,
                rotation = 0f,
                offsetX = 0f,
                offsetY = 0f
            )
        }

        if (!Settings.canDrawOverlays(context)) {
            pendingOverlayPose = pose
            showPermissionDialog = true
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            pendingOverlayPose = pose
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }

        PoseOverlayService.launchOverlay(context, pose)
        Toast.makeText(
            context,
            "Launched \"${pose.name}\" overlay!",
            Toast.LENGTH_SHORT
        ).show()
    }

    Scaffold(
        containerColor = GhostSurface,
        topBar = {
            GhostPoseTopHeader(
                currentTab = selectedBottomTab,
                onSwitchToPoses = { selectedBottomTab = 0 },
                onSwitchToProfile = { selectedBottomTab = 2 }
            )
        },
        bottomBar = {
            GhostPoseBottomNavBar(
                selectedTab = selectedBottomTab,
                onSelectTab = { selectedBottomTab = it }
            )
        },
        floatingActionButton = {
            if (selectedBottomTab == 0 && draftSavePoseUri == null) {
                Surface(
                    shape = CircleShape,
                    color = GhostPrimary,
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .testTag("import_pose_fab")
                        .clickable {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 13.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = GhostOnPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Import Pose",
                            style = MaterialTheme.typography.labelLarge,
                            color = GhostOnPrimary
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when (selectedBottomTab) {
                0 -> PoseHubGridContent(
                    allPosesCount = allPoses.size,
                    poses = filteredPoses,
                    selectedCategory = selectedCategory,
                    activeOverlayState = activeOverlayState,
                    onSelectCategory = { poseViewModel.selectCategory(it) },
                    onOpenPasteLinkDialog = { showPasteLinkDialog = true },
                    onOpenCameraHudTab = { selectedBottomTab = 1 },
                    onPickGalleryImage = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    onLaunchOverlay = { pose -> requestLaunchOverlay(pose) },
                    onLaunchDefaultOverlay = {
                        val firstPose = filteredPoses.firstOrNull() ?: allPoses.firstOrNull()
                        if (firstPose != null) {
                            requestLaunchOverlay(firstPose)
                        } else {
                            selectedBottomTab = 1
                        }
                    },
                    onStopOverlay = { PoseOverlayService.stopOverlay(context) },
                    onToggleFavorite = { poseViewModel.toggleFavorite(it) },
                    onOpenPoseTune = { editingPose = it }
                )

                1 -> CameraHudInteractiveStage(
                    activeState = activeOverlayState,
                    canDrawOverlays = canDrawOverlays,
                    onLaunchSystemOverlay = {
                        val pose = PoseEntity(
                            id = activeOverlayState.poseId ?: 0L,
                            name = activeOverlayState.poseName,
                            imageUri = activeOverlayState.imageUri,
                            alpha = activeOverlayState.alpha,
                            isMirrored = activeOverlayState.isMirrored
                        )
                        requestLaunchOverlay(pose)
                    },
                    onStopSystemOverlay = { PoseOverlayService.stopOverlay(context) },
                    onOpenSaveModal = {
                        draftSavePoseName = activeOverlayState.poseName
                        draftSavePoseUri = activeOverlayState.imageUri.ifBlank {
                            allPoses.firstOrNull()?.imageUri.orEmpty()
                        }
                    }
                )

                2 -> SavedProfileTabContent(
                    favoritePoses = allPoses.filter { it.isFavorite },
                    canDrawOverlays = canDrawOverlays,
                    onRequestOverlayPermission = {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:${context.packageName}")
                        )
                        overlaySettingsLauncher.launch(intent)
                    },
                    onLaunchOverlay = { requestLaunchOverlay(it) },
                    onOpenPasteLink = { showPasteLinkDialog = true }
                )
            }

            // Full Save Reference Pose Modal Sheet (Screenshot 3) when importing/saving a pose
            if (draftSavePoseUri != null) {
                val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
                ModalBottomSheet(
                    onDismissRequest = { draftSavePoseUri = null },
                    sheetState = sheetState,
                    containerColor = Color.Transparent,
                    dragHandle = null
                ) {
                    SavePoseModalSheet(
                        initialName = draftSavePoseName,
                        imageUri = draftSavePoseUri.orEmpty(),
                        currentAlpha = activeOverlayState.alpha,
                        isMirrored = activeOverlayState.isMirrored,
                        onSavePose = { name, category, badge, shortcut ->
                            val uriToSave = draftSavePoseUri.orEmpty()
                            draftSavePoseUri = null
                            poseViewModel.insertPose(
                                name = name,
                                imageUri = uriToSave,
                                alpha = activeOverlayState.alpha,
                                category = category,
                                tagBadge = badge,
                                isMirrored = activeOverlayState.isMirrored,
                                quickShortcut = shortcut
                            ) { savedPose ->
                                Toast.makeText(
                                    context,
                                    "Saved \"${savedPose.name}\" to Pose Hub!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        },
                        onDismiss = { draftSavePoseUri = null }
                    )
                }
            }
        }
    }

    // Paste Web / Pinterest Link Dialog
    if (showPasteLinkDialog) {
        PasteUrlDialog(
            isResolving = isResolvingUrl,
            errorMessage = urlError,
            onDismiss = {
                poseViewModel.clearUrlError()
                showPasteLinkDialog = false
            },
            onResolveLink = { urlText ->
                poseViewModel.resolveWebOrPinterestLink(urlText) { resolved ->
                    showPasteLinkDialog = false
                    draftSavePoseName = resolved.suggestedTitle
                    draftSavePoseUri = resolved.imageUrl
                }
            }
        )
    }

    // Edit / Rename / Delete Pose Dialog (triggered from card Tune button)
    editingPose?.let { pose ->
        EditPoseDialog(
            pose = pose,
            onDismiss = { editingPose = null },
            onSave = { newName, newAlpha, newCategory, newBadge ->
                poseViewModel.renameOrUpdatePose(pose, newName, newAlpha, newCategory, newBadge)
                editingPose = null
            },
            onDelete = {
                poseViewModel.deletePose(pose)
                editingPose = null
            },
            onLaunchOverlay = {
                editingPose = null
                requestLaunchOverlay(pose)
            }
        )
    }

    // Graceful System Overlay Permission Prompt Dialog (with Android 13-15 Restricted Settings Guide)
    if (showPermissionDialog) {
        RestrictedOverlayPermissionDialog(
            context = context,
            onOpenAppInfoSettings = {
                overlaySettingsLauncher.launch(PermissionHelper.createAppDetailsSettingsIntent(context))
            },
            onOpenOverlayPermissionSettings = {
                showPermissionDialog = false
                overlaySettingsLauncher.launch(PermissionHelper.createOverlayPermissionIntent(context))
            },
            onDismiss = { showPermissionDialog = false },
            onOpenInAppPreview = {
                showPermissionDialog = false
                selectedBottomTab = 1
            }
        )
    }
}

@Composable
private fun GhostPoseTopHeader(
    currentTab: Int,
    onSwitchToPoses: () -> Unit,
    onSwitchToProfile: () -> Unit
) {
    Surface(
        color = GhostSurface.copy(alpha = 0.92f),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .height(60.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.clickable { onSwitchToPoses() }
            ) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(GhostPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.MotionPhotosOn,
                        contentDescription = null,
                        tint = GhostOnPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "GHOSTPOSE",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = GhostOnSurface
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = when (currentTab) {
                        0 -> "Poses"
                        1 -> "Live HUD"
                        else -> "Library"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = GhostOnSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(GhostPrimary)
                        .clickable { onSwitchToProfile() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = "Profile & Settings",
                        tint = GhostOnPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GhostPoseBottomNavBar(
    selectedTab: Int,
    onSelectTab: (Int) -> Unit
) {
    Surface(
        color = GhostSurface.copy(alpha = 0.94f),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .height(64.dp)
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val items = listOf(
                Triple(0, "Poses", Icons.Default.AccessibilityNew),
                Triple(1, "Camera Tools", Icons.Default.PhotoCamera),
                Triple(2, "Saved / Profile", Icons.Default.Bookmark)
            )
            items.forEach { (index, label, icon) ->
                val selected = selectedTab == index
                Column(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .testTag("bottom_nav_$index")
                        .clickable { onSelectTab(index) }
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (selected) GhostPrimary else GhostOnSurfaceVariant,
                        modifier = Modifier.size(22.dp)
                    )
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        color = if (selected) GhostPrimary else GhostOnSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PoseHubGridContent(
    allPosesCount: Int,
    poses: List<PoseEntity>,
    selectedCategory: String,
    activeOverlayState: OverlayRuntimeState,
    onSelectCategory: (String) -> Unit,
    onOpenPasteLinkDialog: () -> Unit,
    onOpenCameraHudTab: () -> Unit,
    onPickGalleryImage: () -> Unit,
    onLaunchOverlay: (PoseEntity) -> Unit,
    onLaunchDefaultOverlay: () -> Unit,
    onStopOverlay: () -> Unit,
    onToggleFavorite: (PoseEntity) -> Unit,
    onOpenPoseTune: (PoseEntity) -> Unit
) {
    val categories = remember {
        listOf("All Poses", "Favorites", "Portraits", "Full Body", "Aesthetic", "Recent")
    }

    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 92.dp),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        modifier = Modifier
            .fillMaxSize()
            .testTag("pose_history_grid")
    ) {
        // Sub-Header Utility Actions Row (Span 2)
        item(span = { GridItemSpan(2) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Pose Hub",
                        style = MaterialTheme.typography.headlineMedium,
                        color = GhostOnSurface
                    )
                    Surface(
                        shape = CircleShape,
                        color = GhostSurfaceContainerHigh
                    ) {
                        Text(
                            text = "$allPosesCount poses",
                            style = MaterialTheme.typography.labelSmall,
                            color = GhostOnSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = GhostSurfaceContainerLow,
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .testTag("paste_link_button")
                            .clickable { onOpenPasteLinkDialog() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Link,
                                contentDescription = null,
                                tint = GhostOnSurface,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = "Paste Link",
                                style = MaterialTheme.typography.labelMedium,
                                color = GhostOnSurface
                            )
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = GhostSurfaceContainerLow,
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .size(34.dp)
                            .testTag("open_hud_settings_button")
                            .clickable { onOpenCameraHudTab() }
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.Tune,
                                contentDescription = "Camera HUD Tools",
                                tint = GhostOnSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        }

        // Active Overlay Status Pill if running
        if (activeOverlayState.isRunning) {
            item(span = { GridItemSpan(2) }) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = GhostOnTertiaryFixedVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "ACTIVE OVERLAY • ${(activeOverlayState.alpha * 100).roundToInt()}% OPACITY",
                                style = MaterialTheme.typography.labelSmall,
                                color = GhostTertiaryFixed
                            )
                            Text(
                                text = activeOverlayState.poseName,
                                style = MaterialTheme.typography.headlineSmall,
                                color = GhostOnPrimary
                            )
                        }
                        Surface(
                            shape = CircleShape,
                            color = GhostOnPrimary.copy(alpha = 0.15f),
                            modifier = Modifier
                                .testTag("stop_active_overlay_button")
                                .clickable { onStopOverlay() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.StopCircle,
                                    contentDescription = "Stop Overlay",
                                    tint = GhostTertiaryFixed,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Stop",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = GhostOnPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Quick Action Hero Banner ("Ready to shoot?") matching Screenshot 2
        item(span = { GridItemSpan(2) }) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = GhostSurfaceContainerLowest,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = null,
                            tint = GhostSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "LIVE GHOSTING",
                            style = MaterialTheme.typography.labelSmall,
                            color = GhostSecondary
                        )
                    }

                    Text(
                        text = "Ready to shoot?",
                        style = MaterialTheme.typography.headlineLarge,
                        color = GhostOnSurface
                    )

                    Text(
                        text = "Pick any reference pose to overlay with live opacity control directly inside your camera view.",
                        style = MaterialTheme.typography.bodySmall,
                        color = GhostOnSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = GhostPrimary,
                            shadowElevation = 2.dp,
                            modifier = Modifier
                                .height(46.dp)
                                .testTag("hero_launch_overlay_button")
                                .clickable { onLaunchDefaultOverlay() }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 22.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FilterCenterFocus,
                                    contentDescription = null,
                                    tint = GhostOnPrimary,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "Launch Overlay",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = GhostOnPrimary
                                )
                            }
                        }

                        Surface(
                            shape = CircleShape,
                            color = GhostSurfaceContainerLow,
                            modifier = Modifier
                                .size(46.dp)
                                .testTag("hero_upload_photo_button")
                                .clickable { onPickGalleryImage() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AddPhotoAlternate,
                                    contentDescription = "Upload reference photo",
                                    tint = GhostOnSurface,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Category Filter Pills Row (Horizontal Scroll)
        item(span = { GridItemSpan(2) }) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    Surface(
                        shape = CircleShape,
                        color = if (isSelected) GhostPrimary else GhostSurfaceContainerLowest,
                        shadowElevation = 1.dp,
                        modifier = Modifier
                            .testTag("category_chip_${category.lowercase().replace(' ', '_')}")
                            .clickable { onSelectCategory(category) }
                    ) {
                        Text(
                            text = category,
                            style = MaterialTheme.typography.labelMedium,
                            color = if (isSelected) GhostOnPrimary else GhostOnSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp)
                        )
                    }
                }
            }
        }

        // Pose Cards Grid Items
        items(poses, key = { it.id }) { pose ->
            PoseHistoryCard(
                pose = pose,
                onLaunchOverlay = { onLaunchOverlay(pose) },
                onToggleFavorite = { onToggleFavorite(pose) },
                onOpenTune = { onOpenPoseTune(pose) }
            )
        }
    }
}

@Composable
private fun PoseHistoryCard(
    pose: PoseEntity,
    onLaunchOverlay: () -> Unit,
    onToggleFavorite: () -> Unit,
    onOpenTune: () -> Unit
) {
    val context = LocalContext.current
    val thumbnailRequest = remember(pose.imageUri) {
        ImageRequest.Builder(context)
            .data(pose.imageUri)
            .size(420, 560)
            .allowRgb565(true)
            .crossfade(false)
            .build()
    }
    val relativeDate = remember(pose.timestamp) {
        DateUtils.getRelativeTimeSpanString(
            pose.timestamp,
            System.currentTimeMillis(),
            DateUtils.MINUTE_IN_MILLIS,
            DateUtils.FORMAT_ABBREV_RELATIVE
        ).toString()
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = GhostSurfaceContainerLowest,
        shadowElevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pose_card_${pose.id}")
            .clickable { onOpenTune() }
    ) {
        Column {
            // 3:4 Thumbnail Container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.75f)
                    .background(GhostSurfaceContainer)
            ) {
                AsyncImage(
                    model = thumbnailRequest,
                    contentDescription = pose.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Top-Right Bookmark / Favorite Button
                Surface(
                    shape = CircleShape,
                    color = GhostSurfaceContainerLowest.copy(alpha = 0.90f),
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .size(32.dp)
                        .testTag("favorite_pose_button_${pose.id}")
                        .clickable { onToggleFavorite() }
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = if (pose.isFavorite) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                            contentDescription = "Favorite pose",
                            tint = if (pose.isFavorite) GhostPrimary else GhostOnSurfaceVariant,
                            modifier = Modifier.size(17.dp)
                        )
                    }
                }

                // Bottom Overlay Row: Badge Pill + Quick Launch "Overlay" Button
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = GhostInverseSurface.copy(alpha = 0.78f)
                    ) {
                        Text(
                            text = pose.tagBadge,
                            style = MaterialTheme.typography.labelSmall,
                            color = GhostInverseOnSurface,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Surface(
                        shape = CircleShape,
                        color = GhostSurfaceContainerLowest.copy(alpha = 0.95f),
                        shadowElevation = 2.dp,
                        modifier = Modifier
                            .height(28.dp)
                            .testTag("launch_overlay_card_button_${pose.id}")
                            .clickable { onLaunchOverlay() }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MotionPhotosOn,
                                contentDescription = null,
                                tint = GhostOnSurface,
                                modifier = Modifier.size(13.dp)
                            )
                            Text(
                                text = "Overlay",
                                style = MaterialTheme.typography.labelSmall,
                                color = GhostOnSurface
                            )
                        }
                    }
                }
            }

            // Card Footer: Name + Relative Timestamp + Tune Icon
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = pose.name,
                    style = MaterialTheme.typography.headlineSmall,
                    color = GhostOnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = relativeDate,
                        style = MaterialTheme.typography.bodySmall,
                        color = GhostOnSurfaceVariant
                    )
                    Icon(
                        imageVector = Icons.Default.Tune,
                        contentDescription = "Edit or Delete Pose",
                        tint = GhostOutline,
                        modifier = Modifier
                            .size(16.dp)
                            .clickable { onOpenTune() }
                    )
                }
            }
        }
    }
}

/**
 * Interactive Camera Viewfinder Stage matching Screenshot 1 (`ghostpose_active_overlay_hud_10d6ed13.html`).
 * Allows testing dual-window touch passthrough states, pinch/drag/rotate, opacity slider, horizontal flip,
 * and launching the system-wide WindowManager overlay over external camera apps.
 */
@Composable
private fun CameraHudInteractiveStage(
    activeState: OverlayRuntimeState,
    canDrawOverlays: Boolean,
    onLaunchSystemOverlay: () -> Unit,
    onStopSystemOverlay: () -> Unit,
    onOpenSaveModal: () -> Unit
) {
    val studioBackdropUrl =
        "https://lh3.googleusercontent.com/aida-public/AB6AXuCv8RkTT5gttD7iybwfuYjO32UMW8QFa5gafLUU0KfcbIDpoECiaM_IEA1CRVMBGVKqRPCk6LpIeZvPXVFd54Hp4grUDj5Sf05vbL8aNzUEdFA7rP1hEbHURdGWqjJ1Zp_6fHN7hN2ljtZB1UIC7dvsDG92l-RywzTAgxZPQsbbdAflbcM0DuOJvD_Oy1WO_V8lIBTtZjIVkDZ1VMuXfHt9xAGBxbLUoj9b46ZTueQT"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 8.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(28.dp))
            .background(GhostPrimaryContainer)
            .testTag("camera_hud_interactive_stage")
    ) {
        // 1. Simulated Camera Sensor Live Feed Backdrop
        AsyncImage(
            model = studioBackdropUrl,
            contentDescription = "Camera Viewfinder Preview",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // 2. Reference Ghost Pose Image Layer (Supports Pinch-to-Zoom, Drag, Rotate when in Edit Mode)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .then(
                    if (!activeState.isLockedPassthrough) {
                        Modifier.pointerInput(Unit) {
                            detectTransformGestures { _, pan, zoom, rotationChange ->
                                PoseOverlayService.updateSimulatorState { current ->
                                    current.copy(
                                        scale = (current.scale * zoom).coerceIn(0.35f, 4.5f),
                                        rotation = current.rotation + rotationChange,
                                        offsetX = current.offsetX + pan.x,
                                        offsetY = current.offsetY + pan.y
                                    )
                                }
                            }
                        }
                    } else {
                        Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            if (activeState.imageUri.isNotBlank()) {
                AsyncImage(
                    model = activeState.imageUri,
                    contentDescription = activeState.poseName,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(activeState.alpha)
                        .graphicsLayer {
                            val mirrorMultiplier = if (activeState.isMirrored) -1f else 1f
                            scaleX = activeState.scale * mirrorMultiplier
                            scaleY = activeState.scale
                            rotationZ = activeState.rotation
                            translationX = activeState.offsetX
                            translationY = activeState.offsetY
                        }
                )
            }

            // Neon Alignment Silhouette SVG + 3x3 Viewfinder Grid + Horizon Level Guide
            NeonGhostSilhouetteAndGrid(
                showGridAndGuide = activeState.showGridAndGuide,
                alpha = activeState.alpha,
                isMirrored = activeState.isMirrored
            )
        }

        // 3. Top Viewfinder Floating Action Pills Header + Draggable Frosted Controller HUD
        var panelOffsetX by remember { mutableFloatStateOf(0f) }
        var panelOffsetY by remember { mutableFloatStateOf(0f) }

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            FloatingOverlayTopBar(
                isLockedPassthrough = activeState.isLockedPassthrough,
                isHudExpanded = activeState.isHudExpanded,
                onToggleLockMode = {
                    PoseOverlayService.updateSimulatorState {
                        it.copy(isLockedPassthrough = !it.isLockedPassthrough)
                    }
                },
                onToggleExpandHud = {
                    PoseOverlayService.updateSimulatorState {
                        it.copy(isHudExpanded = !it.isHudExpanded, isHudMinimized = false)
                    }
                },
                onCloseOverlay = {
                    if (activeState.isRunning) {
                        onStopSystemOverlay()
                    } else {
                        onLaunchSystemOverlay()
                    }
                }
            )

            if (activeState.isHudExpanded) {
                FloatingOverlayControlsCard(
                    alpha = activeState.alpha,
                    isMirrored = activeState.isMirrored,
                    isLockedPassthrough = activeState.isLockedPassthrough,
                    showGridAndGuide = activeState.showGridAndGuide,
                    isHudMinimized = activeState.isHudMinimized,
                    onAlphaChange = { newAlpha ->
                        PoseOverlayService.updateSimulatorState { it.copy(alpha = newAlpha) }
                    },
                    onToggleMirror = {
                        PoseOverlayService.updateSimulatorState { it.copy(isMirrored = !it.isMirrored) }
                    },
                    onResetTransform = {
                        PoseOverlayService.updateSimulatorState {
                            it.copy(scale = 1f, rotation = 0f, offsetX = 0f, offsetY = 0f)
                        }
                    },
                    onToggleGridAndGuide = {
                        PoseOverlayService.updateSimulatorState {
                            it.copy(showGridAndGuide = !it.showGridAndGuide)
                        }
                    },
                    onToggleLockPassthrough = {
                        PoseOverlayService.updateSimulatorState {
                            it.copy(isLockedPassthrough = !it.isLockedPassthrough)
                        }
                    },
                    onOpenSavePoseModal = onOpenSaveModal,
                    onToggleMinimizeHud = {
                        PoseOverlayService.updateSimulatorState {
                            it.copy(isHudMinimized = !it.isHudMinimized)
                        }
                    },
                    onCloseOverlay = {
                        if (activeState.isRunning) {
                            onStopSystemOverlay()
                        } else {
                            onLaunchSystemOverlay()
                        }
                    },
                    onDragPanel = { dx, dy ->
                        panelOffsetX = (panelOffsetX + dx).coerceIn(-240f, 240f)
                        panelOffsetY = (panelOffsetY + dy).coerceIn(-680f, 120f)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset { IntOffset(panelOffsetX.roundToInt(), panelOffsetY.roundToInt()) }
                )
            }
        }
    }
}

@Composable
private fun SavedProfileTabContent(
    favoritePoses: List<PoseEntity>,
    canDrawOverlays: Boolean,
    onRequestOverlayPermission: () -> Unit,
    onLaunchOverlay: (PoseEntity) -> Unit,
    onOpenPasteLink: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "System Overlay & Share Setup",
            style = MaterialTheme.typography.headlineMedium,
            color = GhostOnSurface
        )

        // Overlay Permission Status Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = GhostSurfaceContainerLowest,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Dual-Window Touch Passthrough",
                        style = MaterialTheme.typography.headlineSmall,
                        color = GhostOnSurface
                    )
                    Surface(
                        shape = CircleShape,
                        color = if (canDrawOverlays) GhostOnTertiaryFixedVariant else GhostSurfaceContainerHigh
                    ) {
                        Text(
                            text = if (canDrawOverlays) "GRANTED" else "REQUIRED",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (canDrawOverlays) GhostTertiaryFixed else GhostOnSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    text = "Allows GhostPose to float over your native Camera app with FLAG_NOT_TOUCHABLE passthrough so taps go directly to your camera shutter.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GhostOnSurfaceVariant
                )

                if (!canDrawOverlays) {
                    Button(
                        onClick = onRequestOverlayPermission,
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = GhostPrimary)
                    ) {
                        Icon(Icons.Default.Launch, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Enable Display Over Other Apps")
                    }
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = GhostOnTertiaryFixedVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Overlay permission is active and ready.",
                            style = MaterialTheme.typography.labelMedium,
                            color = GhostOnTertiaryFixedVariant
                        )
                    }
                }
            }
        }

        // Pinterest / Web Share Sheet Card
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = GhostSurfaceContainerLowest,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = GhostPrimary
                    )
                    Text(
                        text = "Pinterest & Share Sheet Integration",
                        style = MaterialTheme.typography.headlineSmall,
                        color = GhostOnSurface
                    )
                }
                Text(
                    text = "Share any photo or Pinterest link (including pin.it shortlinks) from any browser or social app directly to \"GhostPose Overlay\" via the Android Share Sheet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GhostOnSurfaceVariant
                )
                OutlinedButton(
                    onClick = onOpenPasteLink,
                    shape = CircleShape
                ) {
                    Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test URL / Pinterest Link Resolver")
                }
            }
        }

        Text(
            text = "Quick Overlay Shortcuts (${favoritePoses.size})",
            style = MaterialTheme.typography.headlineSmall,
            color = GhostOnSurface
        )

        favoritePoses.forEach { pose ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = GhostSurfaceContainerLowest,
                shadowElevation = 1.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLaunchOverlay(pose) }
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AsyncImage(
                        model = pose.imageUri,
                        contentDescription = pose.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = pose.name,
                            style = MaterialTheme.typography.headlineSmall,
                            color = GhostOnSurface
                        )
                        Text(
                            text = "${pose.tagBadge} • ${(pose.alpha * 100).roundToInt()}% Opacity",
                            style = MaterialTheme.typography.bodySmall,
                            color = GhostSecondary
                        )
                    }
                    Surface(
                        shape = CircleShape,
                        color = GhostPrimary
                    ) {
                        Text(
                            text = "Overlay",
                            style = MaterialTheme.typography.labelSmall,
                            color = GhostOnPrimary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PasteUrlDialog(
    isResolving: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onResolveLink: (String) -> Unit
) {
    var urlInput by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Import from Pinterest or Web URL",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Paste a Pinterest link (pin.it/... or pinterest.com/pin/...) or any direct image/page URL. GhostPose follows redirects and extracts the high-res image.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GhostOnSurfaceVariant
                )
                OutlinedTextField(
                    value = urlInput,
                    onValueChange = { urlInput = it },
                    placeholder = { Text("https://pin.it/... or image URL") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("paste_url_input")
                )
                if (isResolving) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                        Text(
                            text = "Following redirects & scraping high-res image...",
                            style = MaterialTheme.typography.bodySmall,
                            color = GhostSecondary
                        )
                    }
                }
                if (!errorMessage.isNullOrBlank()) {
                    Text(
                        text = errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (urlInput.isNotBlank()) {
                        onResolveLink(urlInput)
                    }
                },
                enabled = !isResolving && urlInput.isNotBlank(),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = GhostPrimary),
                modifier = Modifier.testTag("confirm_resolve_url_button")
            ) {
                Text("Extract & Preview")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = GhostSecondary)
            }
        }
    )
}

@Composable
private fun EditPoseDialog(
    pose: PoseEntity,
    onDismiss: () -> Unit,
    onSave: (String, Float, String, String) -> Unit,
    onDelete: () -> Unit,
    onLaunchOverlay: () -> Unit
) {
    var name by remember(pose) { mutableStateOf(pose.name) }
    var alpha by remember(pose) { mutableFloatStateOf(pose.alpha) }
    var badge by remember(pose) { mutableStateOf(pose.tagBadge) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Pose Settings", style = MaterialTheme.typography.headlineSmall)
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.testTag("delete_pose_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete Pose",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Pose Title") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("rename_pose_input")
                )
                OutlinedTextField(
                    value = badge,
                    onValueChange = { badge = it },
                    label = { Text("Badge Tag (e.g. 0.5x Wide, Outdoor)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Column {
                    Text(
                        text = "Default Opacity: ${(alpha * 100).roundToInt()}%",
                        style = MaterialTheme.typography.labelMedium
                    )
                    Slider(
                        value = alpha,
                        onValueChange = { alpha = it },
                        valueRange = 0.1f..1.0f
                    )
                }
                OutlinedButton(
                    onClick = onLaunchOverlay,
                    shape = CircleShape,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.MotionPhotosOn, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Launch in Overlay Now")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(name, alpha, pose.category, badge) },
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = GhostPrimary),
                modifier = Modifier.testTag("save_edited_pose_button")
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Save Changes")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = GhostSecondary)
            }
        }
    )
}

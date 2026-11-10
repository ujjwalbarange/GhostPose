package com.example

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.ui.theme.GhostPoseTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class OverlayRuntimeState(
    val isRunning: Boolean = false,
    val poseId: Long? = null,
    val poseName: String = "0.5x Flash Mirror",
    val imageUri: String = "",
    val alpha: Float = 0.40f,
    val scale: Float = 1.0f,
    val rotation: Float = 0.0f,
    val offsetX: Float = 0.0f,
    val offsetY: Float = 0.0f,
    val isMirrored: Boolean = true,
    val isLockedPassthrough: Boolean = true,
    val showGridAndGuide: Boolean = true,
    val isHudExpanded: Boolean = true,
    val isHudMinimized: Boolean = false,
    val showSaveModal: Boolean = false
)

/**
 * Self-contained LifecycleOwner + SavedStateRegistryOwner + ViewModelStoreOwner
 * for hosting Jetpack Compose views inside a WindowManager Service.
 */
private class OverlayWindowLifecycleOwner : LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateController.savedStateRegistry

    fun onCreate() {
        savedStateController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
    }

    fun onDestroy() {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
    }
}

class PoseOverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var poseRepository: PoseRepository
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var alphaSaveJob: Job? = null

    // Multi-Window Touch Passthrough Architecture Views & LayoutParams
    private var poseLayerView: ComposeView? = null
    private var poseLayerParams: WindowManager.LayoutParams? = null
    private var poseLifecycleOwner: OverlayWindowLifecycleOwner? = null
    private var lastAppliedPoseFlags: Int = -1

    private var controllerAnchorView: ComposeView? = null
    private var controllerAnchorParams: WindowManager.LayoutParams? = null
    private var controllerLifecycleOwner: OverlayWindowLifecycleOwner? = null
    private var lastAppliedControllerFlags: Int = -1
    private var anchorDragAccumX: Float = 0f
    private var anchorDragAccumY: Float = 0f

    private var controlsPanelView: ComposeView? = null
    private var controlsPanelParams: WindowManager.LayoutParams? = null
    private var controlsPanelLifecycleOwner: OverlayWindowLifecycleOwner? = null
    private var panelDragAccumX: Float = 0f
    private var panelDragAccumY: Float = 0f

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
        poseRepository = PoseRepository(PoseDatabase.getInstance(applicationContext).poseDao())
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_STOP_OVERLAY -> {
                stopOverlayAndService()
                return START_NOT_STICKY
            }
            ACTION_TOGGLE_LOCK -> {
                toggleLockPassthroughMode()
                return START_STICKY
            }
        }

        if (!Settings.canDrawOverlays(this)) {
            Toast.makeText(
                this,
                getString(R.string.overlay_permission_desc),
                Toast.LENGTH_LONG
            ).show()
            stopSelf()
            return START_NOT_STICKY
        }

        val incomingUri = intent?.getStringExtra(EXTRA_IMAGE_URI)
        val incomingName = intent?.getStringExtra(EXTRA_POSE_NAME)
        val incomingAlpha = intent?.getFloatExtra(EXTRA_ALPHA, 0.40f) ?: 0.40f
        val incomingPoseId = intent?.getLongExtra(EXTRA_POSE_ID, -1L)?.takeIf { it > 0 }
        val incomingMirrored = intent?.getBooleanExtra(EXTRA_IS_MIRRORED, false) ?: false

        _overlayState.update { current ->
            current.copy(
                isRunning = true,
                poseId = incomingPoseId ?: current.poseId,
                poseName = incomingName ?: current.poseName,
                imageUri = incomingUri ?: current.imageUri,
                alpha = incomingAlpha.coerceIn(0.1f, 1.0f),
                isMirrored = incomingMirrored,
                scale = 1.0f,
                rotation = 0.0f,
                offsetX = 0.0f,
                offsetY = 0.0f,
                isLockedPassthrough = true,
                isHudExpanded = true,
                showSaveModal = false
            )
        }

        startOverlayForegroundNotification()
        ensureDualWindowsAttached()
        syncWindowTouchFlags()

        return START_STICKY
    }

    /**
     * Sets up the Multi-Window Touch Passthrough Architecture:
     * 1. Pose Layer (`poseLayerView`): Full-screen image + alignment grid.
     *    - Default ("Locked / Shoot Mode"): `FLAG_NOT_TOUCHABLE or FLAG_NOT_FOCUSABLE`
     *      so touches pass directly through to the native camera shutter/screen.
     *    - "Edit Mode": `FLAG_NOT_TOUCHABLE` is removed so the user can drag, pinch-to-zoom, and rotate.
     * 2. Floating Top Bar Window (`controllerAnchorView`):
     *    - Anchored at top (`FLAG_NOT_FOCUSABLE`), capturing clicks only on the top bar pills.
     * 3. Floating Draggable Overlay Controls Window (`controlsPanelView`):
     *    - Independently positioned and draggable via the minus (`—`) handle in front of "Overlay Controls".
     */
    private fun ensureDualWindowsAttached() {
        val overlayType = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        val density = resources.displayMetrics.density

        // 1. Pose Layer Window (Full-Screen Preview)
        if (poseLayerView == null) {
            val lifecycleOwner = OverlayWindowLifecycleOwner().also { it.onCreate() }
            poseLifecycleOwner = lifecycleOwner

            val initialPoseFlags = WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                overlayType,
                initialPoseFlags,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
            }
            poseLayerParams = params
            lastAppliedPoseFlags = initialPoseFlags

            val composeView = ComposeView(this).apply {
                setViewTreeLifecycleOwner(lifecycleOwner)
                setViewTreeViewModelStoreOwner(lifecycleOwner)
                setViewTreeSavedStateRegistryOwner(lifecycleOwner)
                setContent {
                    GhostPoseTheme {
                        val state by _overlayState.collectAsState()
                        val context = LocalContext.current

                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .then(
                                    if (!state.isLockedPassthrough) {
                                        Modifier.pointerInput(Unit) {
                                            detectTransformGestures { _, pan, zoom, rotationChange ->
                                                _overlayState.update { current ->
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
                            // Transformed Reference Pose Image (Direct GPU graphicsLayer with 0ms latency)
                            if (state.imageUri.isNotBlank()) {
                                val imageRequest = remember(state.imageUri) {
                                    ImageRequest.Builder(context)
                                        .data(state.imageUri)
                                        .size(1280)
                                        .crossfade(false)
                                        .build()
                                }
                                AsyncImage(
                                    model = imageRequest,
                                    contentDescription = state.poseName,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .graphicsLayer {
                                            val mirrorMultiplier = if (state.isMirrored) -1f else 1f
                                            alpha = state.alpha
                                            scaleX = state.scale * mirrorMultiplier
                                            scaleY = state.scale
                                            rotationZ = state.rotation
                                            translationX = state.offsetX
                                            translationY = state.offsetY
                                        }
                                )
                            }

                            // Viewfinder Grid
                            NeonGhostSilhouetteAndGrid(
                                showGridAndGuide = state.showGridAndGuide,
                                alpha = state.alpha,
                                isMirrored = state.isMirrored
                            )
                        }
                    }
                }
            }
            poseLayerView = composeView
            runCatching { windowManager.addView(composeView, params) }
        }

        // 2. Floating Draggable Overlay Controls Panel Window
        if (controlsPanelView == null) {
            val lifecycleOwner = OverlayWindowLifecycleOwner().also { it.onCreate() }
            controlsPanelLifecycleOwner = lifecycleOwner

            val initialPanelY = (76 * density).toInt()
            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                overlayType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 0
                y = initialPanelY
            }
            controlsPanelParams = params

            val panelComposeView = ComposeView(this).apply {
                setViewTreeLifecycleOwner(lifecycleOwner)
                setViewTreeViewModelStoreOwner(lifecycleOwner)
                setViewTreeSavedStateRegistryOwner(lifecycleOwner)
                setContent {
                    GhostPoseTheme {
                        val state by _overlayState.collectAsState()

                        if (state.isHudExpanded && !state.showSaveModal) {
                            FloatingOverlayControlsCard(
                                alpha = state.alpha,
                                isMirrored = state.isMirrored,
                                isLockedPassthrough = state.isLockedPassthrough,
                                showGridAndGuide = state.showGridAndGuide,
                                isHudMinimized = state.isHudMinimized,
                                onAlphaChange = { newAlpha ->
                                    updateAlpha(newAlpha)
                                },
                                onToggleMirror = {
                                    _overlayState.update { it.copy(isMirrored = !it.isMirrored) }
                                },
                                onResetTransform = {
                                    _overlayState.update {
                                        it.copy(
                                            scale = 1.0f,
                                            rotation = 0.0f,
                                            offsetX = 0.0f,
                                            offsetY = 0.0f
                                        )
                                    }
                                },
                                onToggleGridAndGuide = {
                                    _overlayState.update { it.copy(showGridAndGuide = !it.showGridAndGuide) }
                                },
                                onToggleLockPassthrough = {
                                    toggleLockPassthroughMode()
                                },
                                onOpenSavePoseModal = {
                                    _overlayState.update { it.copy(showSaveModal = true) }
                                    syncWindowTouchFlags()
                                },
                                onToggleMinimizeHud = {
                                    _overlayState.update { it.copy(isHudMinimized = !it.isHudMinimized) }
                                },
                                onCloseOverlay = {
                                    stopOverlayAndService()
                                },
                                onDragPanel = { dx, dy ->
                                    val p = controlsPanelParams ?: return@FloatingOverlayControlsCard
                                    val v = controlsPanelView ?: return@FloatingOverlayControlsCard
                                    panelDragAccumX += dx
                                    panelDragAccumY += dy
                                    val deltaX = panelDragAccumX.toInt()
                                    val deltaY = panelDragAccumY.toInt()
                                    if (deltaX == 0 && deltaY == 0) return@FloatingOverlayControlsCard
                                    panelDragAccumX -= deltaX
                                    panelDragAccumY -= deltaY

                                    val screenHeight = resources.displayMetrics.heightPixels
                                    val screenWidth = resources.displayMetrics.widthPixels
                                    val maxX = (screenWidth * 0.35f).toInt()
                                    val minY = (56 * density).toInt()
                                    val maxY = (screenHeight - (140 * density)).toInt().coerceAtLeast(minY)
                                    val nextX = (p.x + deltaX).coerceIn(-maxX, maxX)
                                    val nextY = (p.y + deltaY).coerceIn(minY, maxY)
                                    if (nextX != p.x || nextY != p.y) {
                                        p.x = nextX
                                        p.y = nextY
                                        runCatching { windowManager.updateViewLayout(v, p) }
                                    }
                                }
                            )
                        }
                    }
                }
            }
            controlsPanelView = panelComposeView
            runCatching { windowManager.addView(panelComposeView, params) }
        }

        // 3. Floating Top Bar Anchor & Save Modal Window
        if (controllerAnchorView == null) {
            val lifecycleOwner = OverlayWindowLifecycleOwner().also { it.onCreate() }
            controllerLifecycleOwner = lifecycleOwner

            val initialControllerFlags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                overlayType,
                initialControllerFlags,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
                x = 0
                y = 0
            }
            controllerAnchorParams = params
            lastAppliedControllerFlags = initialControllerFlags

            val composeView = ComposeView(this).apply {
                setViewTreeLifecycleOwner(lifecycleOwner)
                setViewTreeViewModelStoreOwner(lifecycleOwner)
                setViewTreeSavedStateRegistryOwner(lifecycleOwner)
                setContent {
                    GhostPoseTheme {
                        val state by _overlayState.collectAsState()

                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            // Top Floating Anchor Bar
                            FloatingOverlayTopBar(
                                isLockedPassthrough = state.isLockedPassthrough,
                                isHudExpanded = state.isHudExpanded,
                                onToggleLockMode = { toggleLockPassthroughMode() },
                                onToggleExpandHud = {
                                    _overlayState.update {
                                        it.copy(
                                            isHudExpanded = !it.isHudExpanded,
                                            isHudMinimized = false
                                        )
                                    }
                                    syncControlsPanelVisibility()
                                },
                                onCloseOverlay = { stopOverlayAndService() },
                                onDragAnchor = { dx, dy ->
                                    val p = controllerAnchorParams ?: return@FloatingOverlayTopBar
                                    val v = controllerAnchorView ?: return@FloatingOverlayTopBar
                                    anchorDragAccumX += dx
                                    anchorDragAccumY += dy
                                    val deltaX = anchorDragAccumX.toInt()
                                    val deltaY = anchorDragAccumY.toInt()
                                    if (deltaX == 0 && deltaY == 0) return@FloatingOverlayTopBar
                                    anchorDragAccumX -= deltaX
                                    anchorDragAccumY -= deltaY
                                    val nextX = p.x + deltaX
                                    val nextY = (p.y + deltaY).coerceAtLeast(0)
                                    if (nextX != p.x || nextY != p.y) {
                                        p.x = nextX
                                        p.y = nextY
                                        runCatching { windowManager.updateViewLayout(v, p) }
                                    }
                                }
                            )

                            // Save Pose Modal Sheet inside Overlay
                            if (state.showSaveModal) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color.Black.copy(alpha = 0.45f)),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    SavePoseModalSheet(
                                        initialName = state.poseName,
                                        imageUri = state.imageUri,
                                        currentAlpha = state.alpha,
                                        isMirrored = state.isMirrored,
                                        onSavePose = { name, category, badge, shortcut ->
                                            saveCurrentPoseToDatabase(name, category, badge, shortcut)
                                        },
                                        onDismiss = {
                                            _overlayState.update { it.copy(showSaveModal = false) }
                                            syncWindowTouchFlags()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
            controllerAnchorView = composeView
            runCatching { windowManager.addView(composeView, params) }
        }
    }

    private fun syncControlsPanelVisibility() {
        val state = _overlayState.value
        val shouldShow = state.isHudExpanded && !state.showSaveModal
        controlsPanelView?.visibility = if (shouldShow) View.VISIBLE else View.GONE
    }

    /**
     * Dynamically updates WindowManager flags only when they actually change,
     * avoiding redundant WindowManager IPC calls.
     */
    private fun syncWindowTouchFlags() {
        val state = _overlayState.value
        syncControlsPanelVisibility()

        poseLayerParams?.let { params ->
            val view = poseLayerView ?: return@let
            val desiredPoseFlags = if (state.isLockedPassthrough) {
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            } else {
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            }
            if (lastAppliedPoseFlags != desiredPoseFlags) {
                params.flags = desiredPoseFlags
                lastAppliedPoseFlags = desiredPoseFlags
                runCatching { windowManager.updateViewLayout(view, params) }
            }
        }

        controllerAnchorParams?.let { params ->
            val view = controllerAnchorView ?: return@let
            val desiredControllerFlags = if (state.showSaveModal) {
                // Allow soft keyboard input when entering custom pose name
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            } else {
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH or
                    WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
            }
            if (lastAppliedControllerFlags != desiredControllerFlags) {
                params.flags = desiredControllerFlags
                params.width = WindowManager.LayoutParams.MATCH_PARENT
                lastAppliedControllerFlags = desiredControllerFlags
                runCatching { windowManager.updateViewLayout(view, params) }
            }
        }

        updateForegroundNotification()
    }

    private fun toggleLockPassthroughMode() {
        _overlayState.update { current ->
            current.copy(isLockedPassthrough = !current.isLockedPassthrough)
        }
        syncWindowTouchFlags()
    }

    private fun updateAlpha(newAlpha: Float) {
        val clamped = newAlpha.coerceIn(0.1f, 1.0f)
        _overlayState.update { it.copy(alpha = clamped) }
        val currentId = _overlayState.value.poseId
        if (currentId != null) {
            alphaSaveJob?.cancel()
            alphaSaveJob = serviceScope.launch(Dispatchers.IO) {
                delay(500)
                poseRepository.getPoseById(currentId)?.let { existing ->
                    poseRepository.updatePose(existing.copy(alpha = clamped))
                }
            }
        }
    }

    private fun saveCurrentPoseToDatabase(
        customName: String,
        category: String,
        tagBadge: String,
        quickShortcut: Boolean
    ) {
        val snapshot = _overlayState.value
        serviceScope.launch(Dispatchers.IO) {
            val entity = PoseEntity(
                id = snapshot.poseId ?: 0L,
                name = customName,
                imageUri = snapshot.imageUri,
                alpha = snapshot.alpha,
                timestamp = System.currentTimeMillis(),
                isFavorite = true,
                category = category,
                tagBadge = tagBadge,
                isMirrored = snapshot.isMirrored,
                quickShortcut = quickShortcut
            )
            val savedId = poseRepository.insertPose(entity)
            _overlayState.update {
                it.copy(
                    poseId = savedId,
                    poseName = customName,
                    showSaveModal = false
                )
            }
            launch(Dispatchers.Main) {
                syncWindowTouchFlags()
                Toast.makeText(
                    this@PoseOverlayService,
                    "Saved \"$customName\" to Pose Hub",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun startOverlayForegroundNotification() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            startForeground(
                NOTIFICATION_ID,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
            )
        } else {
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun updateForegroundNotification() {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID, buildNotification())
    }

    private fun buildNotification(): Notification {
        val state = _overlayState.value
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openPendingIntent = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val toggleLockIntent = Intent(this, PoseOverlayService::class.java).apply {
            action = ACTION_TOGGLE_LOCK
        }
        val toggleLockPendingIntent = PendingIntent.getService(
            this,
            1,
            toggleLockIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, PoseOverlayService::class.java).apply {
            action = ACTION_STOP_OVERLAY
        }
        val stopPendingIntent = PendingIntent.getService(
            this,
            2,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val statusText = if (state.isLockedPassthrough) {
            getString(R.string.notification_text_locked)
        } else {
            getString(R.string.notification_text_edit)
        }

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("${getString(R.string.notification_title)}: ${state.poseName}")
            .setContentText(statusText)
            .setOngoing(true)
            .setContentIntent(openPendingIntent)
            .addAction(
                0,
                if (state.isLockedPassthrough) "Unlock / Edit" else "Lock (Passthrough)",
                toggleLockPendingIntent
            )
            .addAction(0, "Close Overlay", stopPendingIntent)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                getString(R.string.notification_channel_name),
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Persistent controls for GhostPose active camera overlay"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    private fun removeDualWindows() {
        poseLayerView?.let { view ->
            runCatching { windowManager.removeView(view) }
        }
        poseLayerView = null
        poseLayerParams = null
        poseLifecycleOwner?.onDestroy()
        poseLifecycleOwner = null
        lastAppliedPoseFlags = -1

        controlsPanelView?.let { view ->
            runCatching { windowManager.removeView(view) }
        }
        controlsPanelView = null
        controlsPanelParams = null
        controlsPanelLifecycleOwner?.onDestroy()
        controlsPanelLifecycleOwner = null

        controllerAnchorView?.let { view ->
            runCatching { windowManager.removeView(view) }
        }
        controllerAnchorView = null
        controllerAnchorParams = null
        controllerLifecycleOwner?.onDestroy()
        controllerLifecycleOwner = null
        lastAppliedControllerFlags = -1
    }

    private fun stopOverlayAndService() {
        _overlayState.update { it.copy(isRunning = false, showSaveModal = false) }
        removeDualWindows()
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        _overlayState.update { it.copy(isRunning = false, showSaveModal = false) }
        removeDualWindows()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val CHANNEL_ID = "ghostpose_overlay_channel"
        const val NOTIFICATION_ID = 4091

        const val ACTION_START_OVERLAY = "com.example.action.START_OVERLAY"
        const val ACTION_STOP_OVERLAY = "com.example.action.STOP_OVERLAY"
        const val ACTION_TOGGLE_LOCK = "com.example.action.TOGGLE_LOCK"

        const val EXTRA_POSE_ID = "extra_pose_id"
        const val EXTRA_POSE_NAME = "extra_pose_name"
        const val EXTRA_IMAGE_URI = "extra_image_uri"
        const val EXTRA_ALPHA = "extra_alpha"
        const val EXTRA_IS_MIRRORED = "extra_is_mirrored"

        private val _overlayState = MutableStateFlow(OverlayRuntimeState())
        val overlayState: StateFlow<OverlayRuntimeState> = _overlayState.asStateFlow()

        fun updateSimulatorState(transform: (OverlayRuntimeState) -> OverlayRuntimeState) {
            _overlayState.update(transform)
        }

        fun launchOverlay(
            context: Context,
            pose: PoseEntity
        ) {
            val intent = Intent(context, PoseOverlayService::class.java).apply {
                action = ACTION_START_OVERLAY
                putExtra(EXTRA_POSE_ID, pose.id)
                putExtra(EXTRA_POSE_NAME, pose.name)
                putExtra(EXTRA_IMAGE_URI, pose.imageUri)
                putExtra(EXTRA_ALPHA, pose.alpha)
                putExtra(EXTRA_IS_MIRRORED, pose.isMirrored)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stopOverlay(context: Context) {
            val intent = Intent(context, PoseOverlayService::class.java).apply {
                action = ACTION_STOP_OVERLAY
            }
            context.startService(intent)
        }
    }
}

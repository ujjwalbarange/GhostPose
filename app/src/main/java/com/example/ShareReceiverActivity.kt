package com.example

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CropFree
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import coil.compose.AsyncImage
import com.example.ui.theme.GhostInverseSurface
import com.example.ui.theme.GhostOnPrimary
import com.example.ui.theme.GhostOnSurface
import com.example.ui.theme.GhostPoseTheme
import com.example.ui.theme.GhostPrimary
import com.example.ui.theme.GhostSecondary
import com.example.ui.theme.GhostSurface
import com.example.ui.theme.GhostSurfaceContainerLowest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

sealed class ShareResolutionState {
    data object Loading : ShareResolutionState()
    data class Resolved(
        val imageUri: String,
        val suggestedName: String,
        val sourceLabel: String
    ) : ShareResolutionState()
    data class Error(val message: String) : ShareResolutionState()
}

class ShareReceiverActivity : ComponentActivity() {

    private lateinit var poseRepository: PoseRepository
    private val resolutionState = MutableStateFlow<ShareResolutionState>(ShareResolutionState.Loading)
    private var pendingPoseForOverlay: PoseEntity? = null

    private val overlayPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) {
        val pending = pendingPoseForOverlay
        if (Settings.canDrawOverlays(this) && pending != null) {
            PoseOverlayService.launchOverlay(this, pending)
            Toast.makeText(this, "GhostPose overlay launched", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        poseRepository = PoseRepository(PoseDatabase.getInstance(applicationContext).poseDao())

        handleIncomingShareIntent(intent)

        setContent {
            GhostPoseTheme {
                val state by resolutionState.collectAsState()
                Scaffold(
                    containerColor = GhostSurface,
                    topBar = {
                        Surface(
                            color = GhostSurface.copy(alpha = 0.9f),
                            shadowElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 36.dp, bottom = 10.dp, start = 12.dp, end = 16.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = "Go back",
                                        tint = GhostOnSurface
                                    )
                                }
                                Text(
                                    text = "Pose Details",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = GhostOnSurface
                                )
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(GhostPrimary),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = null,
                                        tint = GhostOnPrimary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                ) { innerPadding ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Dimmed Blurred Viewport Simulation Header matching Screenshot 3
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(176.dp)
                                .background(GhostInverseSurface),
                            contentAlignment = Alignment.Center
                        ) {
                            val previewUri = (state as? ShareResolutionState.Resolved)?.imageUri
                                ?: "https://lh3.googleusercontent.com/aida-public/AB6AXuC3xNTai2jeoSfThNwdgJoc8Z9XrBVFfggF5OcFHi_ecFLRL-7TfVLq5OwYlBRyL7aA-F2Rv6M91Iey5921IV5Az2KmCXqHYk3gRlTAiFpg7TBc4vXzu6AneWEC1i2N57aLTyFbBESV-2mgDOuq7_OV_qyFyrFxkEmOnWxZscIdw6NrDV4xiclKAXwAFOMw9rzvzim6gVC6xA8Rocj7X03KgCEbiU_wbSIrU6bqpmVu"
                            AsyncImage(
                                model = previewUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(0.40f)
                            )
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CropFree,
                                    contentDescription = null,
                                    tint = GhostSurfaceContainerLowest.copy(alpha = 0.7f),
                                    modifier = Modifier.size(28.dp)
                                )
                                Surface(
                                    shape = CircleShape,
                                    color = GhostInverseSurface.copy(alpha = 0.7f)
                                ) {
                                    Text(
                                        text = "OVERLAY PAUSED",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = GhostSurfaceContainerLowest.copy(alpha = 0.9f),
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }

                        when (val current = state) {
                            is ShareResolutionState.Loading -> {
                                Surface(
                                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                                    color = GhostSurfaceContainerLowest,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(36.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(16.dp)
                                    ) {
                                        CircularProgressIndicator(color = GhostPrimary)
                                        Text(
                                            text = "Resolving high-res pose reference...",
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = GhostOnSurface
                                        )
                                        Text(
                                            text = "Following redirects and extracting target image stream.",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = GhostSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }

                            is ShareResolutionState.Error -> {
                                Surface(
                                    shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                                    color = GhostSurfaceContainerLowest,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(32.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.ErrorOutline,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(40.dp)
                                        )
                                        Text(
                                            text = "Unable to Resolve Shared Pose",
                                            style = MaterialTheme.typography.headlineSmall,
                                            color = GhostOnSurface
                                        )
                                        Text(
                                            text = current.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = GhostSecondary,
                                            textAlign = TextAlign.Center
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Button(
                                            onClick = { finish() },
                                            shape = CircleShape,
                                            colors = ButtonDefaults.buttonColors(containerColor = GhostPrimary)
                                        ) {
                                            Text("Return to App")
                                        }
                                    }
                                }
                            }

                            is ShareResolutionState.Resolved -> {
                                SavePoseModalSheet(
                                    initialName = current.suggestedName,
                                    imageUri = current.imageUri,
                                    currentAlpha = 0.40f,
                                    isMirrored = false,
                                    onSavePose = { name, category, badge, shortcut ->
                                        saveAndLaunchSharedPose(
                                            name = name,
                                            imageUri = current.imageUri,
                                            category = category,
                                            tagBadge = badge,
                                            quickShortcut = shortcut
                                        )
                                    },
                                    onDismiss = { finish() }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    private fun handleIncomingShareIntent(incomingIntent: Intent?) {
        if (incomingIntent?.action != Intent.ACTION_SEND) {
            resolutionState.value = ShareResolutionState.Error("Unsupported intent action.")
            return
        }

        val mimeType = incomingIntent.type.orEmpty()
        lifecycleScope.launch {
            // 1. Check direct EXTRA_STREAM or ClipData content:// image URI first (used by Google Images / Chrome / Gallery shares)
            val streamUri: Uri? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                incomingIntent.getParcelableExtra(Intent.EXTRA_STREAM, Uri::class.java)
            } else {
                @Suppress("DEPRECATION")
                incomingIntent.getParcelableExtra(Intent.EXTRA_STREAM)
            } ?: incomingIntent.clipData?.let { clip ->
                if (clip.itemCount > 0) clip.getItemAt(0).uri else null
            }

            if (streamUri != null && (mimeType.startsWith("image/") || streamUri.scheme == "content" || streamUri.scheme == "file")) {
                val localUri = copySharedUriToInternalStorage(this@ShareReceiverActivity, streamUri)
                if (localUri != null) {
                    val sharedTitle = incomingIntent.getStringExtra(Intent.EXTRA_SUBJECT)
                        ?.takeIf { it.isNotBlank() }
                        ?: "Shared Gallery Pose"
                    resolutionState.value = ShareResolutionState.Resolved(
                        imageUri = localUri,
                        suggestedName = sharedTitle,
                        sourceLabel = "Local Image"
                    )
                    return@launch
                }
            }

            when {
                mimeType.startsWith("image/") -> {
                    resolutionState.value = ShareResolutionState.Error("Could not read shared image stream.")
                }

                mimeType == "text/plain" || incomingIntent.hasExtra(Intent.EXTRA_TEXT) -> {
                    val sharedText = incomingIntent.getStringExtra(Intent.EXTRA_TEXT)
                        ?: incomingIntent.clipData?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.text?.toString()
                        .orEmpty()
                    if (sharedText.isBlank()) {
                        resolutionState.value = ShareResolutionState.Error("Shared link text is empty.")
                        return@launch
                    }

                    val result = LinkResolver.resolveSharedUrl(sharedText, this@ShareReceiverActivity)
                    result.fold(
                        onSuccess = { media ->
                            resolutionState.value = ShareResolutionState.Resolved(
                                imageUri = media.imageUrl,
                                suggestedName = media.suggestedTitle,
                                sourceLabel = media.sourceHost
                            )
                        },
                        onFailure = { err ->
                            resolutionState.value = ShareResolutionState.Error(
                                err.localizedMessage ?: "Could not extract image from shared link."
                            )
                        }
                    )
                }

                else -> {
                    resolutionState.value = ShareResolutionState.Error("Unsupported shared media type: $mimeType")
                }
            }
        }
    }

    private fun saveAndLaunchSharedPose(
        name: String,
        imageUri: String,
        category: String,
        tagBadge: String,
        quickShortcut: Boolean
    ) {
        lifecycleScope.launch {
            val entity = PoseEntity(
                name = name,
                imageUri = imageUri,
                alpha = 0.40f,
                timestamp = System.currentTimeMillis(),
                isFavorite = true,
                category = category,
                tagBadge = tagBadge,
                isMirrored = false,
                quickShortcut = quickShortcut
            )
            val newId = withContext(Dispatchers.IO) {
                poseRepository.insertPose(entity)
            }
            val savedEntity = entity.copy(id = newId)

            if (PermissionHelper.canDrawOverlays(this@ShareReceiverActivity)) {
                PoseOverlayService.launchOverlay(this@ShareReceiverActivity, savedEntity)
                Toast.makeText(
                    this@ShareReceiverActivity,
                    "Saved & launched \"$name\" overlay",
                    Toast.LENGTH_SHORT
                ).show()
                finish()
            } else {
                pendingPoseForOverlay = savedEntity
                Toast.makeText(
                    this@ShareReceiverActivity,
                    "Saved to Pose Hub! Grant overlay permission to project over camera.",
                    Toast.LENGTH_LONG
                ).show()
                overlayPermissionLauncher.launch(
                    PermissionHelper.createOverlayPermissionIntent(this@ShareReceiverActivity)
                )
            }
        }
    }

    companion object {
        suspend fun copySharedUriToInternalStorage(context: Context, sourceUri: Uri): String? =
            withContext(Dispatchers.IO) {
                runCatching {
                    val dir = File(context.filesDir, "shared_poses").apply { mkdirs() }
                    val destFile = File(dir, "pose_${System.currentTimeMillis()}.jpg")

                    // First pass: read image bounds without allocating pixel memory
                    val boundsOptions = android.graphics.BitmapFactory.Options().apply {
                        inJustDecodeBounds = true
                    }
                    context.contentResolver.openInputStream(sourceUri)?.use { input ->
                        android.graphics.BitmapFactory.decodeStream(input, null, boundsOptions)
                    }

                    val width = boundsOptions.outWidth
                    val height = boundsOptions.outHeight
                    if (width > 0 && height > 0) {
                        var sampleSize = 1
                        val maxDimension = 1280
                        while (width / sampleSize > maxDimension || height / sampleSize > maxDimension) {
                            sampleSize *= 2
                        }
                        val decodeOptions = android.graphics.BitmapFactory.Options().apply {
                            inSampleSize = sampleSize
                            inPreferredConfig = android.graphics.Bitmap.Config.ARGB_8888
                        }
                        val decoded = context.contentResolver.openInputStream(sourceUri)?.use { input ->
                            android.graphics.BitmapFactory.decodeStream(input, null, decodeOptions)
                        }
                        if (decoded != null) {
                            FileOutputStream(destFile).use { output ->
                                decoded.compress(android.graphics.Bitmap.CompressFormat.JPEG, 84, output)
                            }
                            decoded.recycle()
                            return@runCatching Uri.fromFile(destFile).toString()
                        }
                    }

                    // Fallback stream copy
                    context.contentResolver.openInputStream(sourceUri)?.use { input ->
                        FileOutputStream(destFile).use { output ->
                            input.copyTo(output)
                        }
                    } ?: return@runCatching null
                    Uri.fromFile(destFile).toString()
                }.getOrNull()
            }
    }
}

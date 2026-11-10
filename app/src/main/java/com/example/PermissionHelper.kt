package com.example

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Process
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Launch
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.GhostOnPrimary
import com.example.ui.theme.GhostOnSurface
import com.example.ui.theme.GhostOnSurfaceVariant
import com.example.ui.theme.GhostOnTertiaryFixedVariant
import com.example.ui.theme.GhostPrimary
import com.example.ui.theme.GhostSecondary
import com.example.ui.theme.GhostSurfaceContainerHigh
import com.example.ui.theme.GhostSurfaceContainerLow
import com.example.ui.theme.GhostSurfaceContainerLowest
import com.example.ui.theme.GhostTertiaryFixed

/**
 * Utility for checking SYSTEM_ALERT_WINDOW permission and guiding users through
 * Android 13, 14, and 15 (API 33+) "Restricted Settings" sideloading protections.
 */
object PermissionHelper {

    /**
     * Checks if `SYSTEM_ALERT_WINDOW` is granted using both `Settings.canDrawOverlays`
     * and `AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW` for immediate accuracy when returning from Settings.
     */
    fun canDrawOverlays(context: Context): Boolean {
        if (Settings.canDrawOverlays(context)) return true
        return runCatching {
            val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
            val mode = appOps.unsafeCheckOpNoThrow(
                AppOpsManager.OPSTR_SYSTEM_ALERT_WINDOW,
                Process.myUid(),
                context.packageName
            )
            mode == AppOpsManager.MODE_ALLOWED
        }.getOrDefault(false)
    }

    /**
     * Returns true if the device runs Android 13+ (API 33+) and the APK was sideloaded
     * (e.g., via browser, file manager, or ADB/package installer rather than Google Play Store),
     * which triggers Android's "Restricted setting" lock on SYSTEM_ALERT_WINDOW.
     */
    fun isRestrictedSettingsLikely(context: Context): Boolean {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return false
        if (canDrawOverlays(context)) return false

        return runCatching {
            val sourceInfo = context.packageManager.getInstallSourceInfo(context.packageName)
            val installingPkg = sourceInfo.installingPackageName.orEmpty()
            // Play Store package name is com.android.vending; any other installer (Chrome, Files, PackageInstaller)
            // on Android 13, 14, and 15 subjects SYSTEM_ALERT_WINDOW to Restricted Settings until unlocked in App Info.
            installingPkg != "com.android.vending"
        }.getOrDefault(true)
    }

    /**
     * Builds the Intent to open `Settings.ACTION_MANAGE_OVERLAY_PERMISSION` directly for this package.
     */
    fun createOverlayPermissionIntent(context: Context): Intent {
        return Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:${context.packageName}")
        )
    }

    /**
     * Builds the Intent to open App Info (`Settings.ACTION_APPLICATION_DETAILS_SETTINGS`)
     * where Android 13/14/15 users tap the top-right 3-dots menu -> "Allow restricted settings".
     */
    fun createAppDetailsSettingsIntent(context: Context): Intent {
        return Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:${context.packageName}")
        )
    }
}

/**
 * Pre-permission explanatory dialog that detects Android 13/14/15 "Restricted Settings"
 * on sideloaded installs and walks the user through unlocking App Info -> 3 Dots -> Allow restricted settings
 * before launching `ACTION_MANAGE_OVERLAY_PERMISSION`.
 */
@Composable
fun RestrictedOverlayPermissionDialog(
    context: Context,
    onOpenAppInfoSettings: () -> Unit,
    onOpenOverlayPermissionSettings: () -> Unit,
    onDismiss: () -> Unit,
    onOpenInAppPreview: (() -> Unit)? = null
) {
    val isAndroid13PlusRestricted = PermissionHelper.isRestrictedSettingsLikely(context)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = GhostSurfaceContainerLowest,
        shape = RoundedCornerShape(28.dp),
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(GhostPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = GhostTertiaryFixed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Enable Camera Overlay",
                        style = MaterialTheme.typography.headlineSmall,
                        color = GhostOnSurface
                    )
                    if (isAndroid13PlusRestricted) {
                        Text(
                            text = "Android 13–15 Sideload Guide",
                            style = MaterialTheme.typography.labelSmall,
                            color = GhostOnTertiaryFixedVariant
                        )
                    }
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "GhostPose uses Display over other apps (SYSTEM_ALERT_WINDOW) to project reference poses over your camera with touch passthrough.",
                    style = MaterialTheme.typography.bodySmall,
                    color = GhostOnSurfaceVariant
                )

                if (isAndroid13PlusRestricted) {
                    Surface(
                        shape = RoundedCornerShape(18.dp),
                        color = GhostSurfaceContainerLow,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AdminPanelSettings,
                                    contentDescription = null,
                                    tint = GhostPrimary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Seeing \"Restricted setting\"?",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = GhostOnSurface
                                )
                            }

                            Text(
                                text = "On Android 13, 14, and 15, sideloaded APKs require a one-time unlock if the overlay toggle is grayed out:",
                                style = MaterialTheme.typography.bodySmall,
                                color = GhostOnSurfaceVariant
                            )

                            // Step-by-step instructions
                            Row(
                                verticalAlignment = Alignment.Top,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = null,
                                    tint = GhostPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Go to App Info > 3 Dots (⋮) > Allow restricted settings, then verify with your PIN/biometrics.",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = GhostOnSurface
                                )
                            }

                            OutlinedButton(
                                onClick = onOpenAppInfoSettings,
                                shape = CircleShape,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("open_app_info_restricted_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Step 1: Open App Info Settings")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onOpenOverlayPermissionSettings,
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(containerColor = GhostPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("grant_overlay_permission_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Launch,
                    contentDescription = null,
                    tint = GhostOnPrimary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isAndroid13PlusRestricted) {
                        "Step 2: Grant Overlay Permission"
                    } else {
                        "Grant Overlay Permission"
                    },
                    color = GhostOnPrimary
                )
            }
        },
        dismissButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (onOpenInAppPreview != null) {
                    TextButton(onClick = onOpenInAppPreview) {
                        Text("In-App HUD Preview", color = GhostOnSurface)
                    }
                }
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = GhostSecondary)
                }
            }
        }
    )
}

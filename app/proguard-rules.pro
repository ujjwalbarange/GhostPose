# ==============================================================================
# GhostPose Production R8 / ProGuard Configuration
# Prevents aggressive stripping of Room, Coil, OkHttp, Coroutines & Compose Views
# ==============================================================================

# 1. General Android & Kotlin Metadata Preservation
-keepattributes *Annotation*,InnerClasses,EnclosingMethod,Signature,Exceptions
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep Application, Activities, Services, and BroadcastReceivers referenced in AndroidManifest.xml
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class com.example.MainActivity
-keep public class com.example.ShareReceiverActivity
-keep public class com.example.PoseOverlayService

# 2. AndroidX Room Database (@Entity, @Dao, RoomDatabase)
-keep class * extends androidx.room.RoomDatabase {
    <init>();
}
-keep @androidx.room.Entity class * {
    *;
}
-keep @androidx.room.Dao interface * {
    *;
}
-keep class com.example.PoseEntity { *; }
-keep class com.example.PoseDao_Impl { *; }
-keep class com.example.PoseDatabase_Impl { *; }
-dontwarn androidx.room.paging.**

# 3. Kotlinx Coroutines & Flow
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}
-dontwarn kotlinx.coroutines.flow.**

# 4. OkHttp3, Okio & LinkResolver Redirect / TLS Handling
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# 5. Coil Image Loading
-keep class coil.** { *; }
-dontwarn coil.**

# 6. Custom WindowManager Views & Lifecycle Owners used in PoseOverlayService
-keep class androidx.compose.ui.platform.ComposeView { *; }
-keep class * implements androidx.lifecycle.LifecycleOwner { *; }
-keep class * implements androidx.savedstate.SavedStateRegistryOwner { *; }
-keep class * implements androidx.lifecycle.ViewModelStoreOwner { *; }
-keepclassmembers class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

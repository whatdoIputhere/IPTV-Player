# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# --- Compose / Kotlin ---
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-dontwarn kotlinx.**
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# --- Media3 / ExoPlayer ---
-dontwarn com.google.common.**
-dontwarn org.checkerframework.**
-dontwarn androidx.media3.**
-keep class androidx.media3.** { *; }

# --- OkHttp/Retrofit/Gson ---
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn retrofit2.**
-dontwarn com.google.gson.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }
-keep class retrofit2.** { *; }
-keepattributes Signature
-keepattributes *Annotation*

# Keep model classes used by Gson (fields may be accessed via reflection)
-keep class com.whatdoiputhere.iptvplayer.model.** { *; }

# --- Room ---
-dontwarn androidx.room.**
-keep class androidx.room.** { *; }
-keep @androidx.room.* class * { *; }
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.RoomDatabase { *; }

# --- AndroidX Lifecycle / ViewModel ---
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

# Keep Parcelable implementations
-keepclassmembers class * implements android.os.Parcelable {
	static ** CREATOR;
}

# Keep generated R classes
-keep class **.R$* { *; }

# Keep annotation default values (Compose, Room)
-keepattributes RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeInvisibleParameterAnnotations,AnnotationDefault
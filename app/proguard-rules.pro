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

# Preserve generic type signatures and annotations (used by Retrofit/Gson/Kotlin)
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes Exceptions,InnerClasses,EnclosingMethod,AnnotationDefault,RuntimeVisibleAnnotations,RuntimeInvisibleAnnotations,RuntimeVisibleParameterAnnotations,RuntimeInvisibleParameterAnnotations,SourceFile,LineNumberTable

# Keep Kotlin metadata (used by reflection/serialization sometimes)
-keep class kotlin.Metadata { *; }

# --- Gson ---
# Keep Gson and its streaming classes
-keep class com.google.gson.** { *; }
-keep class com.google.gson.stream.** { *; }

# Keep our model classes so field names are not obfuscated (Gson uses them)
-keep class com.whatdoiputhere.iptvplayer.model.** { *; }

# In case of Kotlin data classes, keep the default constructors
-keepclasseswithmembers class com.whatdoiputhere.iptvplayer.model.** {
	<init>(...);
}

# --- Retrofit / OkHttp ---
-keep class retrofit2.** { *; }
-keep interface retrofit2.** { *; }
-keep interface com.whatdoiputhere.iptvplayer.api.** { *; }

# Keep Retrofit service methods with annotations
-keepclassmembers interface com.whatdoiputhere.iptvplayer.api.** {
	@retrofit2.http.* <methods>;
}

# Keep our networking/repository/parser code paths intact
-keep class com.whatdoiputhere.iptvplayer.api.** { *; }
-keep class com.whatdoiputhere.iptvplayer.repository.** { *; }
-keep class com.whatdoiputhere.iptvplayer.parser.** { *; }

# Keep Retrofit method parameter annotations (used to build requests)
-keepclasseswithmembers class * {
	@retrofit2.http.* <methods>;
}

# Silence warnings from optional dependencies
-dontwarn javax.annotation.**
-dontwarn okio.**
-dontwarn okhttp3.**

# Keep OkHttp internal to avoid method stripping issues
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# --- Coroutines (usually safe, but silence warnings just in case) ---
-dontwarn kotlinx.coroutines.**
-keep class kotlin.coroutines.** { *; }
-keep class kotlinx.coroutines.** { *; }

# --- Room (not heavily used here, but keep annotations/warnings quiet) ---
-dontwarn androidx.room.**

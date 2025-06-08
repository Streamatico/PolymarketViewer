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

# Do not use ProGuard to obfuscate the code (can help with reproducibility F-Droid builds)
-dontobfuscate

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
-renamesourcefileattribute SourceFile

#-keep @kotlinx.parcelize.Parcelize public class *
-keepnames class * implements java.lang.Exception

# Rules for Kotlin Coroutines (often needed with Ktor/Serialization)
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.android.AndroidDispatcherFactory {}
-keepnames class kotlinx.coroutines.android.AndroidExceptionPreHandler {}
-keepclassmembernames class kotlinx.** { volatile <fields>; }
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlinx.coroutines.flow.** { *; }
-keep class kotlinx.coroutines.flow.**

# Rules for Kotlinx Serialization (usually minimal needed due to plugin)
# -keepclassmembers class ** { @kotlinx.serialization.Serializable <methods>; }
# -if class ** { @kotlinx.serialization.Serializable *; } \
# -keepclassmembers class <1> { *** <init>(...); }
# Ensure data classes used with serialization are kept
-keep @kotlinx.serialization.Serializable class * { <fields>; }
-keepclassmembers,allowoptimization enum * { @kotlinx.serialization.SerialName <fields>; }

# Rules from F-Droid documentation for R8 non-deterministic behavior
# Ref: https://f-droid.org/docs/Reproducible_Builds/
-keep class kotlinx.coroutines.CoroutineExceptionHandler
-keep class kotlinx.coroutines.internal.MainDispatcherFactory

# Prevent R8 from devirtualizing the kotlinx.coroutines.Job interface
# by explicitly keeping it. This should stabilize the output bytecode and fix reproducible builds.
-keep interface kotlinx.coroutines.Job { *; }

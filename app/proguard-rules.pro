# Keep TDLib
-keep class org.drinkless.tdlib.** { *; }

# Keep model classes
-keep class com.letovpn.proxychecker.model.** { *; }

# OkHttp
-dontwarn okhttp3.**
-dontwarn okio.**

# General
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends android.app.Activity

-keepattributes Signature

# Gson specific
-keep class sun.misc.Unsafe { *; }
-keepclassmembers,allowobfuscation class * { @com.google.gson.annotations.SerializedName <fields>; }

# Models
-keep class com.apkupdater.model.** { *; }
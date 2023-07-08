-keepattributes Signature

# Gson
-keep class sun.misc.Unsafe { *; }
-keepclassmembers,allowobfuscation class * { @com.google.gson.annotations.SerializedName <fields>; }

# Models
-keep class com.apkupdater.data.** { *; }

# OkHttp
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# Jsoup
-keepnames class org.jsoup.nodes.Entities
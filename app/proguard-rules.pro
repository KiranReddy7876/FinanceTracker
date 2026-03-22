# Add project specific ProGuard rules here.

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *

# Google Drive / API client
-keep class com.google.api.** { *; }
-keep class com.google.api.client.** { *; }
-dontwarn com.google.api.client.**

# Gson
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Data model classes (for Gson serialization)
-keep class com.financetracker.data.db.entity.** { *; }

# MPAndroidChart
-keep class com.github.mikephil.charting.** { *; }

# WorkManager
-keep class androidx.work.** { *; }
-keep class com.financetracker.service.drive.DriveSyncWorker { *; }

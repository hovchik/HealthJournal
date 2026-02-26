# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.healthjournal.data.remote.dto.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.healthjournal.**$$serializer { *; }
-keepclassmembers class com.healthjournal.** { *** Companion; }
-keepclasseswithmembers class com.healthjournal.** { kotlinx.serialization.KSerializer serializer(...); }

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

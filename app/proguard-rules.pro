# Retrofit
-keepattributes Signature
-keepattributes *Annotation*
-keep class com.hovchik.healthjournal.data.remote.dto.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class kotlinx.serialization.json.** { kotlinx.serialization.KSerializer serializer(...); }
-keep,includedescriptorclasses class com.hovchik.healthjournal.**$$serializer { *; }
-keepclassmembers class com.hovchik.healthjournal.** { *** Companion; }
-keepclasseswithmembers class com.hovchik.healthjournal.** { kotlinx.serialization.KSerializer serializer(...); }

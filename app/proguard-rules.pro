# NoteFlow ProGuard Configuration Rules

# Keep Kotlinx Serialization polymorphic classes and models in domain
-keepattributes *Annotation*,Signature,InnerClasses,EnclosingMethod

-keepclassmembers class * {
    @kotlinx.serialization.Serializable *;
}

# Keep Room entities, DAOs and pure Domain Models
-keep class com.example.data.local.** { *; }
-keep class com.example.domain.model.** { *; }

# Keep androidx.biometric classes
-keep class androidx.biometric.** { *; }

# Project-specific ProGuard rules.

# Keep annotation metadata used by reflection and tooling.
-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod

# Keep Kotlin metadata (safe default for reflection and tooling).
-keep class kotlin.Metadata { *; }

# Add additional keep rules below if reflection or code generation requires it.

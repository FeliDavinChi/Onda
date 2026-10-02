# Library consumer rules handle their own serialization and generated models.

# NewPipeExtractor uses Rhino for provider JavaScript evaluation.
# Upstream: TeamNewPipe/NewPipeExtractor v0.26.5 README.
-keep class org.mozilla.javascript.** { *; }
-keep class org.mozilla.classfile.ClassFileWriter
-dontwarn org.mozilla.javascript.tools.**

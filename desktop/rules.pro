# References to classes only used if needed
-dontwarn ch.qos.logback.**
-dontwarn io.ktor.network.sockets.**
-dontwarn nl.adaptivity.xmlutil.jdk.**

-keep,allowshrinking,allowobfuscation class androidx.compose.runtime.* { *; }

# ktor
-keep class io.ktor.client.HttpClientEngineContainer
-keep class io.ktor.serialization.kotlinx.json.KotlinxSerializationJsonExtensionProvider

# File dialogs
-dontwarn eu.bsinfo.native_helper.**

# Kotlin serialization looks up the generated serializer classes through a function on companion
# objects. The companions are looked up reflectively so we need to explicitly keep these functions.
-keepclasseswithmembers class **.*$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}
# If a companion has the serializer function, keep the companion field on the original type so that
# the reflective lookup succeeds.
-if class **.*$Companion {
  kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class <1>.<2> {
  <1>.<2>$Companion Companion;
}

# Logback
-keep class org.slf4j.spi.SLF4JServiceProvider
-keep class ch.qos.logback.classic.spi.LogbackServiceProvider

-optimizations !method/specialization/parametertype,!class/unboxing/enum

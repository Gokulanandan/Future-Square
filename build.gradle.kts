// Load the Android and Kotlin plugins once, in the same classloader, for all modules.
// Versions live in settings.gradle.kts.
plugins {
    id("com.android.application") apply false
    id("org.jetbrains.kotlin.jvm") apply false
    id("org.jetbrains.kotlin.android") apply false
    id("org.jetbrains.kotlin.plugin.compose") apply false
}

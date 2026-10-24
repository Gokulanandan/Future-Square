// Load the Kotlin plugin once for all modules; versions live in settings.gradle.kts.
plugins {
    id("org.jetbrains.kotlin.jvm") apply false
    id("org.jetbrains.kotlin.android") apply false
    id("org.jetbrains.kotlin.plugin.compose") apply false
}

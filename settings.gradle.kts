rootProject.name = "delta"
enableFeaturePreview("GROOVY_COMPILATION_AVOIDANCE")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://cache-redirector.jetbrains.com/kotlin.bintray.com/kotlin-plugin")
    }
}

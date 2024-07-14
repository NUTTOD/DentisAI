buildscript {
    repositories {
        google()       // Google's Maven repository
        mavenCentral() // Maven Central repository
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.5.0")          // Android Gradle Plugin
        classpath("com.google.gms:google-services:4.3.15")         // Google Services plugin
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0") // Kotlin Gradle Plugin
    }
}
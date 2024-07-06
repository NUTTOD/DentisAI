import org.gradle.kotlin.dsl.accessors.runtime.extensionOf

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")
}
repositories {
    google()
    mavenCentral()
}
android {
    namespace = "com.example.dentistver1"
    compileSdk = 34
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        applicationId = "com.example.dentistver1"
        minSdk = 23
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility(JavaVersion.VERSION_17)
        targetCompatibility(JavaVersion.VERSION_17)
    }
}
repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation(libs.androidx.core.ktx.v180)
    implementation(libs.androidx.appcompat.v142)
    implementation(libs.material.v160)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.analytics.ktx)
    implementation(libs.firebase.auth)
    implementation(libs.androidx.activity)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit.v113)
    androidTestImplementation(libs.androidx.espresso.core.v340)
    implementation(libs.facebook.login)
    implementation(libs.kotlin.stdlib)
    implementation(libs.play.services.auth)
    //implementation(libs.androidx.appcompat.v140)
    implementation(libs.androidx.activity.ktx)
    //implementation("com.google.android.gms:play-services-auth:21.2.0")
}

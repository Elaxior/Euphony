plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
}

android {
    namespace = "com.example.euphony"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.euphony"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
}

dependencies {
    // Room Database
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    testImplementation("junit:junit:4.13.2")
    implementation("androidx.media:media:1.7.0")

    // Guava for ListenableFuture (required for Media3 callbacks)
    implementation("com.google.guava:guava:33.0.0-android")

    // Compose BOM
    implementation(platform("androidx.compose:compose-bom:2024.10.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")

    // Core Android
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.lifecycle:lifecycle-service:2.8.7")

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Material Icons Extended
    implementation("androidx.compose.material:material-icons-extended:1.7.6")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // NewPipeExtractor - Try dev snapshot with latest YouTube fixes
    // If this fails, the extractor library itself is incompatible with current YouTube API
    implementation("com.github.teamnewpipe:NewPipeExtractor:dev-SNAPSHOT")

    // OkHttp
    implementation("com.squareup.okhttp3:okhttp:4.12.0")

    // ExoPlayer (Media3)
    val media3Version = "1.9.2"
    implementation("androidx.media3:media3-exoplayer:$media3Version")
    implementation("androidx.media3:media3-common:$media3Version")
    implementation("androidx.media3:media3-session:$media3Version")
    implementation("androidx.media3:media3-ui:$media3Version")

    // Coil for image loading
    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("io.coil-kt:coil:2.7.0")

    // Core JUnit 4 library
    testImplementation("junit:junit:4.13.2")

    // AndroidX Test Extensions (Fixes AndroidJUnit4)
    androidTestImplementation("androidx.test.ext:junit:1.2.1")

    // Espresso (Standard for UI testing, highly recommended)
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")

    // Desugaring
    coreLibraryDesugaring("com.android.tools:desugar_jdk_libs_nio:2.0.4")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

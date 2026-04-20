plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("org.jetbrains.kotlin.plugin.serialization")
    id("com.google.devtools.ksp")
}

android {
    namespace = "com.healthjournal"
    compileSdk = 35
    buildToolsVersion = "35.0.0"

    defaultConfig {
        applicationId = "com.hovchik.healthjournal"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        ndk {
            abiFilters += listOf("arm64-v8a")
        }

        val claudeApiKey: String = project.findProperty("CLAUDE_API_KEY") as? String ?: ""
        buildConfigField("String", "CLAUDE_API_KEY", "\"$claudeApiKey\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
        // LiteRT 2.1.4 is built with Kotlin 2.3.0 metadata; we're on 2.1.0.
        // Metadata stays ABI-compatible across minor versions, so skip the
        // strict version check rather than doing a large Kotlin upgrade.
        freeCompilerArgs += "-Xskip-metadata-version-check"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        jniLibs {
            // LiteRT 2.1.x ships libLiteRtOpenClAccelerator.so with a LOAD segment
            // misaligned for 16 KB pages (google-ai-edge/LiteRT#6299). We don't use
            // GPU/OpenCL acceleration, so drop it to stay Play Store compliant.
            excludes += listOf("**/libLiteRtOpenClAccelerator.so")
        }
    }

}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2024.12.01")
    implementation(composeBom)

    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    // Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    // Navigation
    implementation("androidx.navigation:navigation-compose:2.8.5")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // Retrofit + OkHttp
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

    // Kotlinx Serialization
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.7.3")
    implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")

    // DataStore
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // Room (local AI model management)
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // LiteRT (successor to TensorFlow Lite; drop-in Interpreter API).
    // 2.1.x ships 16 KB-aligned libLiteRt.so so the app meets Google Play's
    // Android 15+ page-size requirement enforced Nov 1, 2025.
    implementation("com.google.ai.edge.litert:litert:2.1.4")

// Coil (image loading)
    implementation("io.coil-kt:coil-compose:2.7.0")

    // Google Play Billing (subscriptions)
    implementation("com.android.billingclient:billing-ktx:7.1.1")

    // WorkManager (reminders & notifications)
    implementation("androidx.work:work-runtime-ktx:2.10.0")

    // Biometric (security)
    implementation("androidx.biometric:biometric:1.1.0")

    // Health Connect (wearable integration)
    implementation("androidx.health.connect:connect-client:1.1.0-alpha10")

    // Test
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")

    // Debug
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}

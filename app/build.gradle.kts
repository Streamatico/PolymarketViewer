plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("kotlin-parcelize")
    alias(libs.plugins.kotlin.serialization)

    alias(libs.plugins.google.ksp)

    // DI support
    alias(libs.plugins.hilt.android)
}

android {
    namespace = "com.streamatico.polymarketviewer"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.streamatico.polymarketviewer"
        minSdk = 26
        targetSdk = 35
        versionCode = 3
        versionName = "1.12"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
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
        freeCompilerArgs += "-Xjavac-arguments=[\"-Xlint:deprecation\"]"
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    
    // Settings for reproducible builds
    dependenciesInfo {
        includeInApk = false
        // includeInBundle = false  // Uncomment if building AAB files
    }
}

// Add Java compile options for deprecation details
tasks.withType<JavaCompile>().configureEach {
    options.compilerArgs.add("-Xlint:deprecation")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.compose.material.icons.extended)

    // Window Size Class for adaptive layouts
    implementation(libs.androidx.compose.adaptive)
    implementation(libs.androidx.compose.window.size)

    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation(libs.androidx.navigation.compose)

    // Ktor client
    implementation(libs.bundles.ktor.client)
    implementation(libs.kotlinx.serialization.json)

    // DI: Hilt dependencies
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)

    implementation(libs.androidx.hilt.navigation.compose)

    // Coil for Image Loading
    implementation(libs.bundles.coil)

    // Vico Chart Library (Compose)
    implementation(libs.bundles.vico)

    implementation(libs.kotlinx.coroutines.android)
    //implementation(libs.androidx.savedstate.ktx)

    // User Preferences storage
    implementation(libs.androidx.data.store)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}

// Reproducible builds configuration
tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}
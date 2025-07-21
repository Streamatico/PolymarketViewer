import org.jetbrains.kotlin.gradle.dsl.JvmTarget

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

kotlin {
    jvmToolchain(17)
    compilerOptions {
        jvmTarget = JvmTarget.JVM_17

        // See https://youtrack.jetbrains.com/issue/KT-73255
        freeCompilerArgs.add("-Xannotation-default-target=param-property")
    }
}

android {
    namespace = "com.streamatico.polymarketviewer"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.streamatico.polymarketviewer"
        minSdk = 27
        //noinspection OldTargetApi
        targetSdk = 36
        versionCode = 20
        versionName = "1.20"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = false
        }
    }

    buildTypes {
        debug {
            versionNameSuffix = "-DEBUG"
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

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    // By default, Android generates dependency metadata (a file containing information
    // about all the dependencies used in the project) and includes it in both APKs and app bundles.
    // This metadata is particularly useful for the Google Play Store, as it provides actionable
    // feedback on potential issues with project dependencies. However, other platforms cannot
    // utilize this metadata. For example, platforms like IzzyOnDroid, GitHub, and our website do not
    // require or utilize the metadata.
    // Since we only upload app bundles to the Play Store for kiwix app, dependency metadata
    // is enabled for app bundles to leverage Google Play Store's analysis
    // and feedback. For APKs distributed outside the Play Store, we exclude this metadata
    // as they do not require this.
    dependenciesInfo {
        // Disables dependency metadata when building APKs.
        // This is for the signed APKs posted on F-Droid and Github,
        // where dependency metadata is not required or utilized.
        includeInApk = false
        // Enables dependency metadata when building Android App Bundles.
        // This is specifically for the Google Play Store, where dependency metadata
        // is analyzed to provide actionable feedback on potential issues with dependencies.
        includeInBundle = true
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
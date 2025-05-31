// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.compose) apply false

    // KSP
    alias(libs.plugins.google.ksp) apply false

    // DI support
    alias(libs.plugins.hilt.android) apply false
}
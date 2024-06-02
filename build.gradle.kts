// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    id("com.google.gms.google-services") version "4.4.1" apply false
    id("com.google.devtools.ksp") version "2.0.0-1.0.21"
    alias(libs.plugins.compose.compiler) apply false
}
apply{
    plugin("kotlin-kapt")
}
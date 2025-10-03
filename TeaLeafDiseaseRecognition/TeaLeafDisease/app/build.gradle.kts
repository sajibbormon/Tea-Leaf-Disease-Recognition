plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.tealeafdisease.tealeafdisease"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tealeafdisease.tealeafdisease"
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
// LiteRT runtime only (provides org.tensorflow.lite classes)
//    implementation("com.google.ai.edge.litert:litert-api:1.0.1")
//    implementation("com.google.ai.edge.litert:litert:1.0.1")


    // LiteRT dependencies for Google Play services
//    implementation("com.google.android.gms:play-services-tflite-java:16.1.0")
//// Optional: include LiteRT Support Library
//    implementation("com.google.android.gms:play-services-tflite-support:16.1.0")

    implementation("com.google.ai.edge.litert:litert:1.0.1")


    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")

    // Kotlin users use kapt instead of annotationProcessor
    // implementation "androidx.room:room-ktx:2.6.1"

    // MPAndroidChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // MPAndroidChart
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    implementation("com.google.android.material:material:1.12.0")

}
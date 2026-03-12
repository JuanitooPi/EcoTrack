plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.ecotrack"
    compileSdk = 36  // Cambiado a 36 para cumplir con las dependencias

    defaultConfig {
        applicationId = "com.example.ecotrack"
        minSdk = 24
        targetSdk = 36  // Cambiado a 36 también
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Dependencia para gráficos
    implementation("com.github.PhilJay:MPAndroidChart:v3.1.0")

    // Dependencias para PDF
    implementation("com.itextpdf:itext7-core:7.1.16")

    // Dependencia para CSV
    implementation("com.opencsv:opencsv:5.8")

    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}
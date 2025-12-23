//import androidx.glance.appwidget.compose

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.23"
    id("com.google.devtools.ksp") version "2.0.0-1.0.24"
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 24
        targetSdk = 35
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
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    dependencies {
        implementation("androidx.room:room-runtime:2.6.1")
        implementation("androidx.room:room-ktx:2.6.1")
        ksp("androidx.room:room-compiler:2.6.1")
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
        implementation("androidx.navigation:navigation-compose:2.8.0")
        implementation("io.coil-kt:coil-compose:2.6.0")
        implementation("androidx.compose.material3:material3:1.3.0")
        implementation(libs.androidx.core.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        implementation(libs.androidx.activity.compose)

        // Compose
        implementation(platform(libs.androidx.compose.bom))
        implementation(libs.androidx.ui)
        implementation(libs.androidx.ui.graphics)
        implementation(libs.androidx.ui.tooling.preview)
        implementation(libs.androidx.material3)
        implementation("io.coil-kt:coil-compose:2.4.0") // Coil resim yükleme için

        // ViewModel ve Navigation (Doğru versiyonlar)
        implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.1")
        implementation("androidx.navigation:navigation-compose:2.8.1")

        // Retrofit (Ağ İstekleri için)
        implementation("com.squareup.retrofit2:retrofit:2.9.0")
        implementation("com.jakewharton.retrofit:retrofit2-kotlinx-serialization-converter:1.0.0")
        implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")

        // Test Kütüphaneleri
        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        androidTestImplementation(platform(libs.androidx.compose.bom))
        androidTestImplementation(libs.androidx.ui.test.junit4)
        debugImplementation(libs.androidx.ui.tooling)
        debugImplementation(libs.androidx.ui.test.manifest)

        // --- KALDIRILANLAR ---
        // implementation("com.squareup.retrofit2:converter-gson:2.9.0") // kotlinx.serialization kullandığımız için GSON'a gerek yok.
        // implementation("androidx.recyclerview:recyclerview:1.3.1") // Compose projesinde RecyclerView'a gerek yok.
        // implementation(libs.androidx.navigation.compose.jvmstubs) // Çakışmaya neden olan hatalı satır.
    }
}
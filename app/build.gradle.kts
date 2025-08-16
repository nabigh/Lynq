plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    kotlin("kapt")
}

android {
    namespace = "com.example.simplechatapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.simplechatapp"
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

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        viewBinding = true
    }
}

dependencies {
    val room_version = "2.6.1"

    implementation("androidx.room:room-runtime:$room_version")
    kapt("androidx.room:room-compiler:$room_version")
    implementation("androidx.room:room-ktx:$room_version")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation("com.google.android.material:material:1.14.0-alpha03") // Or the latest stable version

    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.recyclerview:recyclerview:1.4.0")
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    implementation("com.github.chrisbanes:PhotoView:2.3.0") // JitPack

    implementation("androidx.emoji2:emoji2:1.5.0")
// Or the latest version
    implementation("androidx.emoji2:emoji2-views-helper:1.5.0")
// For older non-Appcompat views
// For the EmojiPickerView
    implementation("androidx.emoji2:emoji2-emojipicker:1.0.0-alpha03")
// Or the latest alpha/beta/stable
// ... other dependencies

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    // Core Android & UI
    implementation("androidx.core:core-ktx:1.10.1")
// Or your current version
    implementation("androidx.appcompat:appcompat:1.7.1")
// Or your current version
    implementation("com.google.android.material:material:1.14.0-alpha03")
// Or your current version
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
// Or your current version
// Lifecycle (for lifecycleScope, ViewModel, LiveData etc.)
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
// Or your current version
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.6.2")
// Activity KTX for registerForActivityResult
    implementation("androidx.activity:activity-ktx:1.8.0")
// Or your current version
    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // Gson (for JSON serialization of canvas content)
    implementation("com.google.code.gson:gson:2.10.1")

    // ViewBinding (if you haven't enabled it globally)
    // buildFeatures { viewBinding true } // Add this to android {} block in build.gradle

    // RecyclerView (used in NotesListActivity, not directly in NoteEditorActivity but part of the feature)
    implementation ("androidx.recyclerview:recyclerview:1.4.0")

    // Test dependencies (example)
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
}

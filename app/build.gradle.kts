plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
}

android {
    namespace = "com.example.cleantrack"

    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.cleantrack"
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
    kotlinOptions { jvmTarget = "11" }

    buildFeatures { compose = true }
}

dependencies {

    // AndroidX
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)

    //Text Editor dependencies
    dependencies {
        implementation("com.mohamedrejeb.richeditor:richeditor-compose:1.0.0-rc05")
        implementation("androidx.compose.material3:material3")
        implementation("androidx.compose.material:material-icons-extended")
    }

    // Needed for FlowRow and other foundation APIs
    implementation("androidx.compose.foundation:foundation")

    // LiveData with Compose
    implementation("androidx.compose.runtime:runtime-livedata")

    // OPTIONAL icons (choose ONE option below)
    // Option A: if you have version catalog entry, keep yours:
    // implementation(libs.androidx.compose.icons)

    // Option B: if libs.androidx.compose.icons is NOT defined, use this:
    implementation("androidx.compose.material:material-icons-extended")

    // Firebase (ONE BOM only)
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-database-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-config-ktx:21.6.0")

    // Google Vertex AI for Firebase Library
    implementation("com.google.firebase:firebase-vertexai")

    // QR
    implementation("com.journeyapps:zxing-android-embedded:4.3.0")

    // Google Sign-In
    implementation("com.google.android.gms:play-services-auth:21.0.0")

    // Maps + Location + Network
    implementation("org.maplibre.gl:android-sdk:11.12.1")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // IMPORTANT if you use Tasks.await() anywhere
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.ui)

    // Tests
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    implementation("androidx.compose.runtime:runtime-livedata:<compose_version>")

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    //    Cloudinary to store image and picasso to fetch image

    implementation("com.cloudinary:cloudinary-android:2.1.0")
    implementation("com.squareup.picasso:picasso:2.8")
    //Coil instaed of picasso
    // Change from 3.3.0 to 3.2.0 or 3.0.0
    // Coil 2.x includes the network engine by default
    implementation("io.coil-kt:coil-compose:2.6.0")

    implementation("com.google.ai.client.generativeai:generativeai:0.9.0")

    implementation ("com.google.code.gson:gson:2.10.1")

    // Stripe
    implementation("com.stripe:stripe-android:20.50.0")

    //Gson
    implementation("com.google.code.gson:gson:2.10.1")

//    Mockito for uniT testing
    testImplementation("org.mockito:mockito-core:5.11.0")
    testImplementation("org.mockito.kotlin:mockito-kotlin:5.2.1")
    testImplementation("androidx.arch.core:core-testing:2.2.0")

    //Espresso
    androidTestImplementation("androidx.test:runner:1.2.0")
    androidTestImplementation("androidx.test:rules:1.2.0")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")

}

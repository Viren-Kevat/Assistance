plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android") version "1.9.24"
}

android {
    namespace = "com.example.assistance"
    compileSdk = 34
    buildFeatures {
        buildConfig = true
    }
    defaultConfig {
        applicationId = "com.example.assistance"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        buildConfigField(
            "String",
            "GEMINI_API_KEY",
            "\"${project.findProperty("GEMINI_API_KEY")}\""
        )
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {


    testImplementation("junit:junit:4.13.2") // ✅ Add this
    androidTestImplementation("androidx.test.ext:junit:1.1.5") // ✅ Add this
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1") // ✅ Add this
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("com.squareup.okhttp3:okhttp:4.12.0") // ✅ Add this line
    implementation ("com.google.android.material:material:1.11.0")
    implementation ("com.airbnb.android:lottie:6.4.0")

}

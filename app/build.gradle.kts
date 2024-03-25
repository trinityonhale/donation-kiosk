plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")

    // hilt
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "ca.trinityonhale.donationkiosk"
    compileSdk = 34

    defaultConfig {
        applicationId = "ca.trinityonhale.donationkiosk"
        minSdk = 29
        targetSdk = 34
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
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
        buildConfig = true
    }
}

kapt {
    correctErrorTypes = true
}

hilt {
    enableAggregatingTask = true
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.preference:preference-ktx:1.2.1")

    // lifecycle
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.4.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.4.0")

    // start of stripe
    implementation("com.stripe:stripeterminal:3.4.0")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.5.0")
    implementation("com.stripe:stripe-java:24.19.0")
    // end of stripe

    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:retrofit:2.10.0")
    implementation("com.squareup.retrofit2:converter-gson:2.10.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.1")

    // encryption
    implementation("androidx.security:security-crypto:1.0.0")

    // hilt
    implementation("com.google.dagger:hilt-android:2.51")
    implementation("com.google.firebase:firebase-database-ktx:20.3.1")
    implementation("androidx.legacy:legacy-support-v4:1.0.0")
    kapt("com.google.dagger:hilt-compiler:2.51")

    // hilt (for instrumentation tests)
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.51")
    kaptAndroidTest("com.google.dagger:hilt-compiler:2.51")

    // hilt (for local unit tests)
    testImplementation("com.google.dagger:hilt-android-testing:2.51")
    kaptTest("com.google.dagger:hilt-compiler:2.51")
    // end of hilt

    // picasso
    implementation("com.squareup.picasso:picasso:2.8")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
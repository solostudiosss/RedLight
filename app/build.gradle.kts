@file:Suppress("NewApiVersionAvailable")

plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.solostudios.redlight"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.solostudios.redlight"
        minSdk = 31
        targetSdk = 36
        versionCode = 11000
        versionName = "0.11.0"
    }

    signingConfigs {
        create("release") {
            storeFile = file("release-key.jks")
            storePassword = "YOUR_KEYSTORE_PASSWORD"
            keyAlias = "redlight-key"
            keyPassword = "YOUR_KEY_PASSWORD"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }


    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    kotlinOptions {
        jvmTarget = "21"
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    @Suppress("UseTomlInstead")
    implementation("androidx.appcompat:appcompat:1.7.1")
    @Suppress("UseTomlInstead")
    implementation("androidx.core:core-ktx:1.16.0")
}

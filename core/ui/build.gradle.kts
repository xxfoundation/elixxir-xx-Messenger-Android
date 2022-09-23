plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
    kotlin("kapt")
    id("androidx.navigation.safeargs")
}

android {
    compileSdk = Versions.COMPILE_SDK

    defaultConfig {
        minSdk = Versions.MIN_SDK
        targetSdk = Versions.TARGET_SDK

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":depconstraints"))
    implementation(project(":core:logging"))

    implementation(Libs.Core.ANDROIDX_APPCOMPAT)
    implementation(Libs.Core.CORE_KTX)

    implementation(Libs.Ui.MATERIAL)
    implementation(Libs.Ui.CONSTRAINT_LAYOUT)
    implementation(Libs.Ui.BIOMETRIC)
    implementation(Libs.Ui.NAVIGATION)
    implementation(Libs.Ui.NAVIGATION_UI)
}
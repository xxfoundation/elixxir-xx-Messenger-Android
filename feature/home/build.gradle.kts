plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    implementation(project(":depconstraints"))
    implementation(project(":core:preferences"))
    implementation(project(":core:ui"))
    implementation(project(":core:logging"))

    implementation(Libs.Core.ANDROIDX_APPCOMPAT)
    implementation(Libs.Core.CORE_KTX)
    implementation(Libs.Core.CORE_COROUTINES)

    implementation(Libs.Ui.MATERIAL)
    implementation(Libs.Ui.RECYCLERVIEW)
    implementation(Libs.Ui.CONSTRAINT_LAYOUT)
    implementation(Libs.Ui.NAVIGATION)
    implementation(Libs.Ui.NAVIGATION_UI)

    testImplementation(Libs.Testing.CORE_TEST)
    testImplementation(Libs.Testing.TRUTH)
    testImplementation(Libs.Testing.JUNIT)
    androidTestImplementation(Libs.Testing.EXT_JUNIT)
}
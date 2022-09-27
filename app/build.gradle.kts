plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("androidx.navigation.safeargs")
    id("dagger.hilt.android.plugin")
}

android {
    compileSdk = Versions.COMPILE_SDK

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    buildFeatures {
        dataBinding = true
        viewBinding = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
    }

    defaultConfig {
        applicationId = "io.xxlabs.messenger"
        versionCode = Versions.VERSION_CODE
        versionName = Versions.VERSION_NAME
        minSdk = Versions.MIN_SDK
        targetSdk = Versions.TARGET_SDK
        testInstrumentationRunner = "io.xxlabs.messenger.CustomTestRunner"
        multiDexEnabled = true
        vectorDrawables.useSupportLibrary = true

        javaCompileOptions {
            annotationProcessorOptions {
                arguments["room.incremental"] = "true"
            }
        }

        ndk.abiFilters.addAll(
            listOf(
                "armeabi-v7a",
                "arm64-v8a",
                "x86",
                "x86_64"
            )
        )
    }

    buildTypes {
        all {
            multiDexEnabled = true

            setProperty(
                "archivesBaseName",
                "${defaultConfig.applicationId}-v${defaultConfig.versionName}-build-${defaultConfig.versionCode}"
            )

            signingConfig = signingConfigs.getByName("debug")
        }

        getByName("debug") {
            versionNameSuffix = "-internal"
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
        }

        getByName("release") {
            versionNameSuffix = "-release"
            isMinifyEnabled = true
            isDebuggable = false
            isShrinkResources = true

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    android.buildTypes.all { type ->
        val name = type.name.replace("-", "_")
        type.manifestPlaceholders["appName"] = "@string/xx_app_name_${name}"
        true
    }

    lint {
        lintConfig = file("lint_config.xml")
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }
}

kapt {
    correctErrorTypes = true
}

dependencies {
    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))
    implementation(project(":depconstraints"))
    implementation(project(":core:preferences"))
    implementation(project(":core:ui"))
    implementation(project(":core:logging"))
    implementation(project(":feature:home"))
    implementation(project(":feature:registration"))
    implementation(project(":feature:splash"))

    implementation(Libs.Core.ANDROIDX_APPCOMPAT)
    implementation(Libs.Core.CORE_KTX)
    implementation(Libs.Core.CORE_COROUTINES)

    implementation(Libs.Ui.MATERIAL)
    implementation(Libs.Ui.BIOMETRIC)
    implementation(Libs.Ui.RECYCLERVIEW)
    implementation(Libs.Ui.CONSTRAINT_LAYOUT)
    implementation(Libs.Ui.NAVIGATION)
    implementation(Libs.Ui.NAVIGATION_UI)

    implementation(Libs.Data.PHONE_NUMBER)
    implementation(Libs.Data.PROTOBUF)

    implementation(Libs.DI.HILT)
    kapt(Libs.DI.HILT_KAPT)
    
    testImplementation(Libs.Testing.CORE_TEST)
    testImplementation(Libs.Testing.TRUTH)
    testImplementation(Libs.Testing.JUNIT)
    androidTestImplementation(Libs.Testing.EXT_JUNIT)
}
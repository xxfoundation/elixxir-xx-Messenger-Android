plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("androidx.navigation.safeargs")
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

        buildConfigField(
            "double",
            "APP_VERSION",
            android.defaultConfig.versionName ?: "1.0"
        )
    }

    buildTypes {
        all {
            multiDexEnabled = true

            //Shared Keys
            val localNdf = getNdf()

            buildConfigField("String", "NDF", localNdf)
            setProperty(
                "archivesBaseName",
                "${defaultConfig.applicationId}-v${defaultConfig.versionName}-build-${defaultConfig.versionCode}"
            )

            signingConfig = signingConfigs.getByName("debug")
        }

        getByName("debug") {
            isDebuggable = true
            isMinifyEnabled = false
            isShrinkResources = false
        }

        create("mock") {
            initWith(getByName("debug"))
            versionNameSuffix = "-Mock"
            matchingFallbacks += "debug"

            buildConfigField(
                "io.xxlabs.messenger.config.Environment",
                "ENVIRONMENT",
                "io.xxlabs.messenger.config.Environment.MOCK"
            )
        }

        create("releaseNetDebug") {
            initWith(getByName("debug"))
            versionNameSuffix = "-ReleaseNetDebug"
            ndk.debugSymbolLevel = "FULL"
            matchingFallbacks += "debug"

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField(
                "io.xxlabs.messenger.config.Environment",
                "ENVIRONMENT",
                "io.xxlabs.messenger.config.Environment.RELEASE_NET"
            )
        }

        create("mainNetDebug") {
            initWith(getByName("debug"))
            versionNameSuffix = "-MainNetDebug"
            matchingFallbacks += "debug"

            buildConfigField(
                "io.xxlabs.messenger.config.Environment",
                "ENVIRONMENT",
                "io.xxlabs.messenger.config.Environment.MAIN_NET"
            )
        }

        create("mainNet") {
            isMinifyEnabled = false
            isDebuggable = false
            isShrinkResources = false
            matchingFallbacks += "release"

            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )

            buildConfigField(
                "io.xxlabs.messenger.config.Environment",
                "ENVIRONMENT",
                "io.xxlabs.messenger.config.Environment.MAIN_NET"
            )
        }
    }

    android.variantFilter {
        if(buildType.name == "release" || buildType.name == "debug") {
            ignore = true
        }
    }

    android.buildTypes.all { type ->
        val name = type.name.replace("-", "_")
        type.manifestPlaceholders["appName"] = "@string/xx_app_name_${name}"
        true
    }

    lint {
        lintConfig = file("lint_config.xml")
//        disable("MissingTranslation")
    }

    packagingOptions.excludes.addAll(
        listOf(
            "META-INF/atomicfu.kotlin_module",
            "META-INF/reflect.kotlin_builtins",
            "**/attach_hotspot_windows.dll",
            "META-INF/licenses/**",
            "META-INF/AL2.0",
            "META-INF/LGPL2.1",
            "META-INF/DEPENDENCIES"
        )
    )

    kotlinOptions {
        jvmTarget = "1.8"
    }

    configurations.all {
        resolutionStrategy.force("com.google.code.findbugs:jsr305:2.0.1")
    }
}

kapt {
    useBuildCache = true
}

dependencies {
    implementation(fileTree(mapOf("include" to listOf("*.jar"), "dir" to "libs")))
    implementation(project(":xxclient"))
    implementation(project(":depconstraints"))

    implementation(Libs.Core.ANDROIDX_APPCOMPAT)
    implementation(Libs.Core.CORE_KTX)
    implementation(Libs.Core.CORE_COROUTINES)

    implementation(Libs.Ui.MATERIAL)
    implementation(Libs.Ui.BIOMETRIC)
    implementation(Libs.Ui.RECYCLERVIEW)
    implementation(Libs.Ui.CONSTRAINT_LAYOUT)
    implementation(Libs.Ui.NAVIGATION)
    implementation(Libs.Ui.NAVIGATION_UI)

    implementation(Libs.Data.PREFERENCE)
    implementation(Libs.Data.PHONE_NUMBER)
    implementation(Libs.Data.GSON)
    implementation(Libs.Data.PROTOBUF)

    implementation(Libs.Media.GLIDE)
    kapt(Libs.Media.GLIDE_KAPT)

    implementation(platform(Libs.Logging.FIREBASE_BOM))
    implementation(Libs.Logging.CRASHLYTICS)
    implementation(Libs.Logging.TIMBER)

    implementation(Libs.Testing.CORE_TEST)
    implementation(Libs.Testing.JUNIT)
    implementation(Libs.Testing.EXT_JUNIT)
    implementation(Libs.Testing.TRUTH)
}

fun getNdf(): String {
    val gson = com.google.gson.GsonBuilder()
        .setLenient()
        .create()

    val fileContents = file("../ndf.json").readLines()
    val json = gson.toJson(gson.toJsonTree(fileContents))
    val parsedNdf = json.substring(1, json.length - 1)

    return parsedNdf
}
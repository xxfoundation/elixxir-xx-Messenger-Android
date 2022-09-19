plugins {
    id("com.android.application")
    kotlin("android")
    id("kotlin-kapt")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("androidx.navigation.safeargs")
}

android {
    compileSdk = 31

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
        versionCode = 629
        versionName = "3.0"
        minSdk = 26
        targetSdk = 31
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
    implementation("com.googlecode.libphonenumber:libphonenumber:8.12.31")

    // Android wrapper
    implementation(project(":xxclient"))

    implementation(project(":proto")) {
        exclude("com.google.protobuf")
    }

    // Core
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.5.30")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.4.3")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.work:work-runtime:2.7.0")

    // AndroidX Components
    implementation("androidx.appcompat:appcompat:1.3.1")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.1")
    implementation("androidx.preference:preference-ktx:1.1.1")
    implementation("androidx.biometric:biometric:1.2.0-alpha03")
    implementation("androidx.recyclerview:recyclerview:1.2.1")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:27.0.0"))
    implementation("com.google.firebase:firebase-crashlytics-ktx")

    // Navigation
    implementation("androidx.navigation:navigation-fragment-ktx:2.3.5")
    implementation("androidx.navigation:navigation-ui-ktx:2.3.5")

    // Logging assistant Timber
    implementation("com.jakewharton.timber:timber:4.7.1")

    // Gson
    implementation("com.google.code.gson:gson:2.8.6")

    // Image
    implementation("com.github.bumptech.glide:glide:4.12.0")
    kapt("com.github.bumptech.glide:compiler:4.12.0")
    implementation("io.github.florent37:shapeofview:1.4.7")
    implementation("id.zelory:compressor:3.0.1")

    // Protobuf
    implementation("com.google.protobuf:protobuf-javalite:3.15.3")

    // Local unit testing
    testImplementation("junit:junit:4.13.2")

    // AndroidX Test - Instrumented testing
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")

    // Dependencies for Android instrumented unit tests
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.2-native-mt")
    androidTestImplementation("org.mockito:mockito-core:4.1.0")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.4.0")

    androidTestImplementation("androidx.fragment:fragment-testing:1.3.6")
    testImplementation("androidx.test:core:1.4.0")

    // AndroidX Test - JVM testing
    testImplementation("androidx.test.ext:junit-ktx:1.1.3")
    testImplementation("androidx.test:core-ktx:1.4.0")
    testImplementation("org.robolectric:robolectric:4.7.1")

    // Other test dependencies
    testImplementation("com.google.truth:truth:1.1.3")
    testImplementation("androidx.arch.core:core-testing:2.1.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.5.2-native-mt")
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
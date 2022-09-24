object Versions {
    const val COMPILE_SDK = 33
    const val MIN_SDK = 26
    const val TARGET_SDK = 33
    const val VERSION_CODE = 629
    const val VERSION_NAME = "3.0"
}

object Plugins {
    const val AGP = "7.2.2"
    const val KOTLIN = "1.7.10"
    const val PROTOBUF = "0.8.18"
    const val GOOGLE_SVCS = "4.3.14"
    const val CRASHLYTICS = "2.9.2"
    const val NAVIGATION_SAFEARGS = "2.5.2"
}

object Libs {
    object Core {
        const val ANDROIDX_APPCOMPAT = "androidx.appcompat:appcompat"
        const val CORE_KTX = "androidx.core:core-ktx"
        const val CORE_COROUTINES = "androidx.core:core-ktx"
    }

    object Ui {
        const val MATERIAL = "com.google.android.material:material"
        const val BIOMETRIC = "androidx.biometric:biometric"
        const val RECYCLERVIEW = "androidx.recyclerview:recyclerview"
        const val CONSTRAINT_LAYOUT = "androidx.constraintlayout:constraintlayout"
        const val NAVIGATION = "androidx.navigation:navigation-fragment-ktx"
        const val NAVIGATION_UI = "androidx.navigation:navigation-ui-ktx"
    }

    object Data {
        const val PREFERENCE = "androidx.preference:preference-ktx"
        const val PHONE_NUMBER = "com.googlecode.libphonenumber:libphonenumber"
        const val GSON = "com.google.code.gson:gson"
        const val PROTOBUF = "com.google.protobuf:protobuf-javalite"
    }

    object Media {
        const val GLIDE_VERSION = "4.12.0"
        const val GLIDE = "com.github.bumptech.glide:glide"
        const val GLIDE_KAPT = "com.github.bumptech.glide:compiler:$GLIDE_VERSION"
    }

    object Logging {
        const val CRASHLYTICS = "com.google.firebase:firebase-crashlytics-ktx"
        const val FIREBASE_BOM = "com.google.firebase:firebase-bom"
        const val TIMBER = "com.jakewharton.timber:timber"
    }

    object Testing {
        const val CORE_TEST = "androidx.test:core"
        const val JUNIT = "junit:junit"
        const val TRUTH = "com.google.truth:truth"
        const val EXT_JUNIT = "androidx.test.ext:junit"
    }

    object DI {
        const val HILT_VERSION = "2.38.1"
        const val HILT = "com.google.dagger:hilt-android"
        const val HILT_KAPT = "com.google.dagger:hilt-android-compiler:$HILT_VERSION"
    }
}
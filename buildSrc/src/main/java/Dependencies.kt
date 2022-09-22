object Versions {
    const val ANDROID_GRADLE_PLUGIN = "7.1.2"
    const val KOTLIN = "1.6.10"

    const val COMPILE_SDK = 31
    const val MIN_SDK = 26
    const val TARGET_SDK = 32
    const val VERSION_CODE = 629
    const val VERSION_NAME = "3.0"
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
        const val ANDROIDX_PREFERENCE = "androidx.preference:preference-ktx"
        const val PHONE_NUMBER = "com.googlecode.libphonenumber:libphonenumber"
        const val GSON = "com.google.code.gson:gson:2.8.6"
        const val PROTOBUF = "com.google.protobuf:protobuf-javalite"
    }

    object Images {
        const val GLIDE = "com.github.bumptech.glide:glide:4.12.0"
        const val GLIDE_KAPT = "com.github.bumptech.glide:compiler:4.12.0"
    }

    object Logging {
        const val CRASHLYTICS = "com.google.firebase:firebase-crashlytics-ktx"
        const val FIREBASE_BOM = "com.google.firebase:firebase-bom"
        const val TIMBER = "com.jakewharton.timber:timber"
    }

    object Testing {
        const val TEST_CORE = "androidx.test:core"
        const val JUNIT = "junit:junit"
        const val TRUTH = "com.google.truth:truth"
        const val ANDROID_JUNIT = "androidx.test.ext:junit"
    }
}
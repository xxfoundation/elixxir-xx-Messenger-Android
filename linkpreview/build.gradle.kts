plugins {
    id("com.android.library")
    kotlin("android")
    kotlin("kapt")
    kotlin("android.extensions")
}

android {
    compileSdkVersion(31)

    defaultConfig {
        minSdkVersion(21)
        targetSdkVersion(31)

        testInstrumentationRunner = "android.support.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        all {
            isMinifyEnabled = true
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
}

dependencies {
    //Core
    implementation("org.jsoup:jsoup:1.13.1")
    implementation("androidx.core:core-ktx:1.6.0")

    //Test
    androidTestImplementation("junit:junit:4.13.2")
    androidTestImplementation("com.android.support.test:runner:1.0.2")
    androidTestImplementation("org.mockito:mockito-android:3.11.2")

    //RxJava
    implementation("io.reactivex.rxjava2:rxjava:2.2.21")
    implementation("io.reactivex.rxjava2:rxkotlin:2.4.0")
    implementation("io.reactivex.rxjava2:rxandroid:2.1.1")

    implementation("com.squareup.okhttp3:okhttp:5.0.0-alpha.2")
}
apply {
    plugin("kotlin-android")
}
repositories {
    mavenCentral()
}
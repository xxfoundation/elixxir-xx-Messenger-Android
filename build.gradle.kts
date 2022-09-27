import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        google()
        maven(url = "https://maven.google.com")
        maven(url = "https://plugins.gradle.org/m2/")
        maven(url = "https://jitpack.io")
    }

    dependencies {
        classpath("com.android.tools.build:gradle:${Plugins.AGP}")
        classpath("com.google.protobuf:protobuf-gradle-plugin:${Plugins.PROTOBUF}")
        classpath("com.google.gms:google-services:${Plugins.GOOGLE_SVCS}")
        classpath("com.google.firebase:firebase-crashlytics-gradle:${Plugins.CRASHLYTICS}")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:${Plugins.NAVIGATION_SAFEARGS}")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")
        classpath("com.google.dagger:hilt-android-gradle-plugin:${Libs.DI.HILT_VERSION}")
    }
}

allprojects {
    tasks.withType<KotlinCompile>().configureEach {
        kotlinOptions.freeCompilerArgs += listOf(
            "-Xuse-experimental=kotlinx.coroutines.ExperimentalCoroutinesApi",
            "-opt-in=androidx.compose.material.ExperimentalMaterialApi",
        )
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
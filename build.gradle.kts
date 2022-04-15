// Top-level build file where you can add configuration options common to all sub-projects/modules.

buildscript {
    repositories {
        google()
        maven(url = "https://maven.google.com")
        maven(url = "https://plugins.gradle.org/m2/")
        maven(url = "https://jitpack.io")
        maven(url = "https://kotlin.bintray.com/kotlinx/")
        maven(url = "https://maven.appspector.com/artifactory/android-sdk")
    }

    dependencies {
        classpath("org.jacoco:org.jacoco.core:0.8.7")
        classpath("com.android.tools.build:gradle:7.1.2")
        classpath("com.google.protobuf:protobuf-gradle-plugin:0.8.18")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.10")
        classpath("com.google.gms:google-services:4.3.10")
        classpath("com.google.firebase:firebase-crashlytics-gradle:2.7.1")
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.4.1")
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
        maven(url = "https://maven.appspector.com/artifactory/android-sdk")
        maven(url = "https://oss.jfrog.org/libs-snapshot")
        maven(url = "https://oss.sonatype.org/content/repositories/snapshots")
    }
}

tasks.register("clean", Delete::class) {
    delete(rootProject.buildDir)
}
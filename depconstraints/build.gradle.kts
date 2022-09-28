plugins {
    id("java-platform")
    id("maven-publish")
}

val appCompat = "1.5.1"
val biometric = "1.2.0-alpha04"
val constraintLayout = "2.1.4"
val core = "1.9.0"
val coreTest = "1.4.0"
val coroutines = "1.6.1"
val crypto = "1.1.0-alpha03"
val extJunit = "1.1.3"
val firebase = "27.0.0"
val gson = "2.8.9"
val glide = Libs.Media.GLIDE_VERSION
val hilt = Libs.DI.HILT_VERSION
val junit = "4.13.2"
val material = "1.6.1"
val navigation = "2.5.2"
val phoneNumber = "8.12.31"
val preference = "1.2.0"
val protobuf = "3.17.3"
val recyclerView = "1.2.1"
val timber = "5.0.1"
val truth = "1.1.3"

dependencies {
    constraints {
        api("${Libs.Core.ANDROIDX_APPCOMPAT}:$appCompat")
        api("${Libs.Core.CORE_KTX}:$core")
        api("${Libs.Core.CORE_COROUTINES}:$coroutines")
        api("${Libs.Ui.MATERIAL}:$material")
        api("${Libs.Ui.BIOMETRIC}:$biometric")
        api("${Libs.Ui.RECYCLERVIEW}:$recyclerView")
        api("${Libs.Ui.CONSTRAINT_LAYOUT}:$constraintLayout")
        api("${Libs.Ui.NAVIGATION}:$navigation")
        api("${Libs.Ui.NAVIGATION_UI}:$navigation")
        api("${Libs.Data.PREFERENCE}:$preference")
        api("${Libs.Data.PHONE_NUMBER}:$phoneNumber")
        api("${Libs.Data.GSON}:$gson")
        api("${Libs.Data.PROTOBUF}:$protobuf")
        api("${Libs.Data.CRYPTO}:$crypto")
        api("${Libs.Media.GLIDE}:$glide")
        api(Libs.Logging.CRASHLYTICS)
        api("${Libs.Logging.FIREBASE_BOM}:$firebase")
        api("${Libs.Logging.TIMBER}:$timber")
        api("${Libs.Testing.CORE_TEST}:$coreTest")
        api("${Libs.Testing.JUNIT}:$junit")
        api("${Libs.Testing.TRUTH}:$truth")
        api("${Libs.Testing.EXT_JUNIT}:$extJunit")
        api("${Libs.DI.HILT}:$hilt")
    }
}

publishing {
    publications {
        create<MavenPublication>("xxmessenger") {
            from(components["javaPlatform"])
        }
    }
}
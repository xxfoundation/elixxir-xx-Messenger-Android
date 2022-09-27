dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        maven(url = "https://jitpack.io")
    }
}

rootProject.name = "xx Messenger"

include(":app")

// Feature modules
include(":feature:splash")
include(":feature:registration")
include(":feature:home")

// Data modules
include(":data:session")

// Core modules
include(":core:ui")
include(":core:preferences")
include(":core:logging")

// Resolve dependencies across modules
include(":depconstraints")

// Cmix/Client modules
include(":xxclient")
project(":xxclient").projectDir = File("elixxir-dapps-sdk-kotlin/xxclient")

include(":cmix")
project(":cmix").projectDir = File("elixxir-dapps-sdk-kotlin/cmix")

// Protobuf
include(":proto")
include(":data:version")
include(":data:networking")

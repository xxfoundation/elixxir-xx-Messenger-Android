rootProject.name = "xx Messenger 1.5"

include(":app")
include(":linkpreview")
include(":proto")

// Android wrapper
include(":xxclient")
project(":xxclient").projectDir = File(rootProject.projectDir, "elixxir-dapps-sdk-kotlin/xxclient")

// Bindings 2.0
include(":cmix")
project(":cmix").projectDir = File(rootProject.projectDir, "elixxir-dapps-sdk-kotlin/cmix")

// Client migration adapter
include(":xxmessengerclient")

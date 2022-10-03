rootProject.name = "xx Messenger 1.5"

include(":app")
include(":linkpreview")
include(":proto")

include(":xxclient")
project(":xxclient").projectDir = File(rootProject.projectDir, "elixxir-dapps-sdk-kotlin/xxclient")

include(":cmix")
project(":cmix").projectDir = File(rootProject.projectDir, "elixxir-dapps-sdk-kotlin/cmix")
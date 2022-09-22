include(":app")
//include(":linkpreview")
include(":data:proto")

include(":xxclient:cmix")
project(":xxclient:cmix").projectDir = File("elixxir-dapps-sdk-kotlin/cmix")

include("xxclient")
project(":xxclient").projectDir = File("elixxir-dapps-sdk-kotlin/xxclient")

include(":feature:registration")
include(":data:session")
include(":core:ui")
include(":core:preferences")

include(":app")
//include(":linkpreview")
include(":data:proto")

include("cmix")
project(":cmix").projectDir = File("elixxir-dapps-sdk-kotlin/cmix")

include("xxclient")
project(":xxclient").projectDir = File("elixxir-dapps-sdk-kotlin/xxclient")

include(":feature:registration")
include(":data:session")

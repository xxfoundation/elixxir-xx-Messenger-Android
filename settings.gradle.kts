include(":app")

// Feature modules
include(":feature:registration")

// Data modules
include(":data:session")
include(":data:proto")

// Core modules
include(":core:ui")
include(":core:preferences")

// Resolve dependencies across modules
include(":depconstraints")

// Cmix/Client modules
include(":xxclient:cmix")
project(":xxclient:cmix").projectDir = File("elixxir-dapps-sdk-kotlin/cmix")

include("xxclient")
project(":xxclient").projectDir = File("elixxir-dapps-sdk-kotlin/xxclient")
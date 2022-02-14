import com.google.protobuf.gradle.*
import org.gradle.kotlin.dsl.proto

plugins {
    java
    id("com.google.protobuf")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

sourceSets {
    main {
        proto {
            srcDir("${projectDir}/src/main/proto")
        }

        java {
            // include self written and generated code
            srcDirs("${projectDir}/src/main/java", "${projectDir}/src/generated/main/java")
        }
    }
}

// Resolves protobuf duplicate resource error
// https://stackoverflow.com/questions/69676587/duplicate-handling-strategy-error-with-gradle-while-using-protobuf-for-java
tasks {
    withType<Copy> {
        filesMatching("pbpayload.proto") {
            duplicatesStrategy = DuplicatesStrategy.INCLUDE
        }
    }
}

dependencies {
    implementation("com.google.protobuf:protobuf-javalite:3.15.3")
    implementation("io.grpc:grpc-okhttp:1.36.0")
    implementation("io.grpc:grpc-protobuf-lite:1.36.0")
    implementation("io.grpc:grpc-stub:1.36.0")
    implementation("io.grpc:grpc-protobuf:1.36.0")
}

protobuf {
    generatedFilesBaseDir = "$projectDir/src/generated"

    protoc {
        artifact = "com.google.protobuf:protoc:3.15.1"
    }

    plugins {
        id("grpc") {
            artifact = "io.grpc:protoc-gen-grpc-java:1.36.0"
        }
        id("grpckt") {
            artifact =
                "io.grpc:protoc-gen-grpc-kotlin:1.0.0:jdk7@jar"
        }
    }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                named("java") {
                    option("lite")
                }
            }
            task.plugins {
                id("grpc") {
                    option("lite")
                }
                id("grpckt") {
                    option("lite")
                }
            }
        }
    }

    tasks.named("clean") {
        delete(generatedFilesBaseDir)
    }
}
plugins {
  kotlin("jvm")
}

group = "br.com.simulator.credit.creditas"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

subprojects {
  dependencies {
    implementation(project(":core:simulation-domain"))
    implementation(project(":core:common-domain"))
    implementation("io.awspring.cloud:spring-cloud-aws-dependencies:3.3.0")
  }
}

tasks.test {
  useJUnitPlatform()
}
kotlin {
  jvmToolchain(21)
}

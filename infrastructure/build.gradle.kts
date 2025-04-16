plugins {
  kotlin("jvm")
}

group = "br.com.simulator.credit.creditas"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  implementation(project(":core:simulation-domain"))
  implementation(project(":core:common-domain"))
}

tasks.test {
  useJUnitPlatform()
}
kotlin {
  jvmToolchain(21)
}

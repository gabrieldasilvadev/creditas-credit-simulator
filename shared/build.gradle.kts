plugins {
  kotlin("jvm")
}

group = "br.com.simulator.credit.creditas"
version = "0.0.1-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  implementation(project(":core:common-domain"))
  implementation(project(":core:simulation-domain"))
  testImplementation(kotlin("test"))
}

tasks.test {
  useJUnitPlatform()
}
kotlin {
  jvmToolchain(21)
}

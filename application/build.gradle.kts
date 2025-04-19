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
  implementation(project(":adapters:outbound:messaging"))
  implementation(project(":adapters:outbound:external-api"))
  implementation(project(":adapters:outbound:persistence"))
  implementation(project(":infrastructure"))
  implementation(project(":shared"))
  testImplementation(kotlin("test"))
}

tasks.test {
  useJUnitPlatform()
}
kotlin {
  jvmToolchain(21)
}

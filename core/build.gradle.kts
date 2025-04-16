plugins {
  kotlin("jvm")
}

group = "br.com.simulator.credit.creditas"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

tasks.test {
  useJUnitPlatform()
}
kotlin {
  jvmToolchain(21)
}

plugins {
  kotlin("jvm")
}

group = "br.com.simulator.credit.creditas"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
}

tasks.test {
  useJUnitPlatform()
}
kotlin {
  jvmToolchain(21)
}

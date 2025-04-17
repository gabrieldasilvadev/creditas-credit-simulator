plugins {
  kotlin("jvm")
  kotlin("plugin.spring") version "1.9.25"
  id("org.jetbrains.kotlin.plugin.jpa") version "2.1.20"
}

group = "br.com.simulator.credit.creditas"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  implementation(project(":infrastructure"))
  implementation("org.springframework.boot:spring-boot-starter-data-mongodb")
}

tasks.test {
  useJUnitPlatform()
}
kotlin {
  jvmToolchain(21)
}

allOpen {
  annotation("jakarta.persistence.Entity")
  annotation("jakarta.persistence.MappedSuperclass")
  annotation("jakarta.persistence.Embeddable")
}

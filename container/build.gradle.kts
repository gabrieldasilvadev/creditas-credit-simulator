plugins {
  kotlin("jvm")
  kotlin("plugin.spring")
}

group = "br.com.simulator.credit.creditas"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
  maven("https://jitpack.io")
}

dependencies {
  implementation(project(":adapters:inbound:api-rest"))
  implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.0")
  implementation("io.awspring.cloud:spring-cloud-aws-starter-sqs")
  runtimeOnly("org.glassfish.jaxb:jaxb-runtime:4.0.0")

  rootProject.subprojects
    .filter { it.path != project.path && it.path != ":integration-test" }
    .forEach {
      implementation(project(it.path))
      println("modulo carregado: ${it.path}")
    }
}

tasks.test {
  useJUnitPlatform()
}

kotlin {
  jvmToolchain(21)
}

tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> {
  archiveFileName.set("app.jar")
}


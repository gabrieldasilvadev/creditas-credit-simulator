plugins {
  kotlin("jvm")
  kotlin("plugin.spring")
}

group = "br.com.simulator.credit.creditas"
version = "1.0-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  implementation("jakarta.xml.bind:jakarta.xml.bind-api:4.0.0")
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

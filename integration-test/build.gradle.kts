plugins {
  kotlin("jvm")
}

group = "br.com.simulator.credit.creditas"
version = "1.0-SNAPSHOT"
val restAssuredVersion = "5.1.1"

repositories {
  mavenCentral()
}

dependencies {
}

tasks.test {
  useJUnitPlatform()
}
kotlin {
  jvmToolchain(21)
}

dependencies {
  implementation(project(":container"))
  testImplementation("org.springframework.cloud:spring-cloud-contract-wiremock")
  testImplementation("org.awaitility:awaitility-kotlin:4.2.0")
  testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
  testImplementation("io.rest-assured:json-path:$restAssuredVersion")
  testImplementation("io.rest-assured:xml-path:$restAssuredVersion")
  testImplementation("io.rest-assured:kotlin-extensions:$restAssuredVersion")
}

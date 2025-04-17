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

  testImplementation("org.springframework.boot:spring-boot-starter-test") {
    exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
  }

  testImplementation("org.springframework.cloud:spring-cloud-contract-wiremock")
  testImplementation("org.springframework.cloud:spring-cloud-starter-contract-stub-runner")
  testImplementation("org.awaitility:awaitility-kotlin:4.2.0")
  testImplementation("io.rest-assured:rest-assured:$restAssuredVersion")
  testImplementation("io.rest-assured:json-path:$restAssuredVersion")
  testImplementation("io.rest-assured:xml-path:$restAssuredVersion")
  testImplementation("io.rest-assured:kotlin-extensions:$restAssuredVersion")

  testImplementation("org.testcontainers:junit-jupiter:1.19.3")
  testImplementation("org.testcontainers:mongodb:1.19.3")
  testImplementation("org.testcontainers:localstack:1.19.3")

  implementation("com.amazonaws:aws-java-sdk-sqs:1.12.684")
  implementation("com.amazonaws:aws-java-sdk-sns:1.12.684")
}

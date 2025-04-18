plugins {
  kotlin("jvm")
}

group = "br.com.simulator.credit.creditas.messaging.sqs"
version = "0.0.1-SNAPSHOT"

repositories {
  mavenCentral()
}

dependencies {
  implementation(project(":adapters:outbound:notification"))
  implementation(project(":infrastructure"))
  implementation("io.awspring.cloud:spring-cloud-aws-starter-sqs")
  testImplementation(kotlin("test"))
}

tasks.test {
  useJUnitPlatform()
}
kotlin {
  jvmToolchain(21)
}

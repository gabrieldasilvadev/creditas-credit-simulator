import org.jlleitschuh.gradle.ktlint.KtlintExtension
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
  kotlin("jvm") version "1.9.25"
  kotlin("plugin.spring") version "1.9.25"
  id("org.springframework.boot") version "3.4.4"
  id("io.spring.dependency-management") version "1.1.7"
  id("com.adarshr.test-logger") version "3.2.0"
  id("org.jlleitschuh.gradle.ktlint") version "12.2.0"
}

group = "br.com.simulator.credit.creditas"
version = "1.0-SNAPSHOT"

val mockkVersion = "1.13.17"
val kediatrVersion = "3.1.1"
val resilience4jVersion = "2.3.0"
val jacksonVersion = "2.18.2"

extra["springCloudVersion"] = "2024.0.1"

allprojects {
  group = "br.com.simulator.credit.creditas"
  version = "0.0.1-SNAPSHOT"

  repositories {
    mavenCentral()
  }
}

subprojects {
  apply(plugin = "org.jetbrains.kotlin.jvm")
  apply(plugin = "org.jetbrains.kotlin.plugin.spring")
  apply(plugin = "org.springframework.boot")
  apply(plugin = "io.spring.dependency-management")
  apply(plugin = "com.adarshr.test-logger")
  apply(plugin = "org.jlleitschuh.gradle.ktlint")

  group = "br.com.simulator.credit.creditas"
  version = "0.0.1-SNAPSHOT"

  repositories {
    mavenCentral()
  }

  dependencyManagement {
    imports {
      mavenBom("org.springframework.cloud:spring-cloud-dependencies:${property("springCloudVersion")}")
    }
  }

  dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web") {
      exclude(group = "org.springframework.boot", module = "spring-boot-starter-tomcat")
    }
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-jetty")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-cache")

    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation(kotlin("stdlib-jdk8"))
    implementation("org.jetbrains.kotlin:kotlin-stdlib:2.0.0")

    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:2.17.2")
    implementation("org.openapitools:jackson-databind-nullable:0.2.6")

    implementation(platform("io.awspring.cloud:spring-cloud-aws-dependencies:3.3.0"))
    implementation("io.awspring.cloud:spring-cloud-aws-starter")

    implementation("org.springframework.retry:spring-retry:2.0.11")
    implementation("io.github.resilience4j:resilience4j-spring-boot3")
    implementation("io.github.resilience4j:resilience4j-reactor")
    implementation("io.github.resilience4j:resilience4j-retry:$resilience4jVersion")
    implementation("io.github.resilience4j:resilience4j-timelimiter:$resilience4jVersion")
    implementation("io.github.resilience4j:resilience4j-kotlin:$resilience4jVersion")

    implementation("io.micrometer:micrometer-registry-prometheus")
    implementation("io.micrometer:micrometer-core")
    implementation("org.aspectj:aspectjweaver:1.9.21")
    implementation("jakarta.validation:jakarta.validation-api:3.1.1")
    implementation("org.apache.commons:commons-lang3:3.17.0")

    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
    implementation("com.trendyol:kediatr-spring-boot-3x-starter:$kediatrVersion")
    implementation("org.instancio:instancio-junit:5.4.0")
    ktlint("io.github.oshai:kotlin-logging:5.0.0")

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")

    if (project.name != "simulation-domain" && project.name != "common-domain") {
      implementation(project(":core:common-domain"))
    }
  }

  if (project.name != "container") {
    tasks.named<BootJar>("bootJar") {
      enabled = false
    }
  } else {
    // Configuração específica para o módulo 'container'
    tasks.named<BootJar>("bootJar") {
      archiveFileName.set("app.jar")
      mainClass.set("br.com.simulator.credit.creditas.container.ApplicationKt")
    }
  }

  extensions.configure<KtlintExtension> {
    verbose.set(true)
    android.set(false)
    outputToConsole.set(true)
    reporters {
      reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
      reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }

    filter {
      exclude("**/generated/**")
      exclude("**/generated/openapi/**")
      include("**/kotlin/**")
    }
  }

  tasks.matching {
    it.name.startsWith("runKtlint") || it.name.startsWith("ktlint")
  }.configureEach {
    notCompatibleWithConfigurationCache("ktlint plugin is not yet compatible with configuration cache")
  }
}

tasks.test {
  useJUnitPlatform()
}

kotlin {
  jvmToolchain(21)
  compilerOptions {
    freeCompilerArgs.addAll("-Xjsr305=strict")
  }
}

extensions.configure<KtlintExtension> {
  verbose.set(true)
  android.set(false)
  outputToConsole.set(true)
  reporters {
    reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
    reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
  }
  filter {
    exclude("**/generated/**")
    exclude("**/generated/openapi/**")
    include("**/kotlin/**")
  }
}

springBoot {
  mainClass.set("br.com.simulator.credit.creditas.container.ApplicationKt")
}

repositories {
  mavenCentral()
}

tasks.bootJar {
  archiveFileName.set("app.jar")
  mainClass.set("br.com.simulator.credit.creditas.container.ApplicationKt")
}

tasks.withType<org.springframework.boot.gradle.tasks.run.BootRun> {
  mainClass.set("br.com.simulator.credit.creditas.container.ApplicationKt")
}


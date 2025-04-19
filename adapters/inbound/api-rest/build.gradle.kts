plugins {
  kotlin("jvm")
  id("org.openapi.generator") version "7.12.0"
}

group = "br.com.simulator.credit.creditas"
version = "1.0-SNAPSHOT"

val basePackage = "br.com.simulator.credit.openapi"

repositories {
  mavenCentral()
}

dependencies {
  implementation(project(":core:simulation-domain"))
  implementation(project(":application"))
  implementation(project(":shared"))
  implementation(project(":infrastructure"))
  implementation(project(":core:common-domain"))
  implementation(project(":adapters:outbound:persistence"))
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.6")
  compileOnly("io.swagger.core.v3:swagger-annotations:2.2.30")
  compileOnly("io.swagger.core.v3:swagger-core-jakarta:2.2.30")
}

tasks.test {
  useJUnitPlatform()
}

tasks.compileTestKotlin {
  dependsOn("openApiGenerate")
}

kotlin {
  jvmToolchain(21)
}

tasks.compileKotlin { dependsOn("openApiGenerate") }
openApiGenerate {
  generatorName.set("kotlin-spring")
  inputSpec.set("$projectDir/src/main/resources/contract/api-docs.yaml")
  outputDir.set("$projectDir/generated/openapi")
  modelNameSuffix.set("Dto")
  configOptions.set(
    mapOf(
      "annotationLibrary" to "swagger2",
      "useSpringBoot3" to "true",
      "gradleBuildFile" to "false",
      "basePackage" to "$basePackage.web.api",
      "apiPackage" to "$basePackage.web.api",
      "modelPackage" to "$basePackage.web.dto",
      "interfaceOnly" to "true",
      "hideGenerationTimestamp" to "true",
      "openApiNullable" to "false",
      "useTags" to "true",
      "useSwaggerUI" to "true",
    ),
  )
  additionalProperties.set(
    mapOf(
      "reactive" to "true",
      "useCoroutines" to "true",
    ),
  )
}

sourceSets["main"].java {
  srcDir("$projectDir/generated/openapi")
}

tasks.named<Delete>("clean") {
  delete("$projectDir/generated/openapi")
}
tasks.named("build") {
  dependsOn("openApiGenerate")
}
tasks.named("clean") {
  delete("$projectDir/generated/openapi")
}

tasks.named("compileKotlin") {
  dependsOn("openApiGenerate")
}

tasks.named("runKtlintCheckOverMainSourceSet") {
  dependsOn(tasks.named("openApiGenerate"))
}

ktlint {
  filter {
    exclude {
      it.file.toPath().startsWith(projectDir.toPath().resolve("generated/openapi"))
    }
  }
}

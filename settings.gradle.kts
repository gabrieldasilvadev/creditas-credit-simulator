plugins {
  id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "credit-simulator"

include(
  ":application",
  ":container",
  ":integration-test",
  ":infrastructure",
  ":core:simulation-domain",
  ":core:common-domain",
  ":adapters:inbound:api-rest",
  ":adapters:outbound:persistence",
  ":adapters:outbound:notification",
  ":adapters:outbound:messaging",
  ":adapters:outbound:external-api",
)

buildCache {
  local {
    isEnabled = true
  }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
include("adapters:inbound:messaging")
include("adapters:inbound:api-rest")
findProject(":adapters:inbound:messaging")?.name = "messaging"
findProject(":adapters:inbound:api-rest")?.name = "api-rest"
findProject(":adapters:outbound:persistence")?.name = "persistence"

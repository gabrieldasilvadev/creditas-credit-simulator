package br.com.simulator.credit.creditas.integrationtest

import br.com.simulator.credit.creditas.container.Application
import br.com.simulator.credit.creditas.integrationtest.cofig.AwsLocalstackInitializer
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import io.restassured.RestAssured
import io.restassured.config.HttpClientConfig
import java.io.File
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [Application::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureWireMock(port = 9291, stubs = ["classpath:/stubs"])
@ActiveProfiles("integration-test")
abstract class BaseIntegrationTest {

  companion object {
    private val resource = File("docker-compose-test.yaml")

    @Container
    private val environment =
      DockerComposeContainer(File(resource.toURI()))
        .withExposedService("mongo", 27017, Wait.forListeningPort())
        .withExposedService("localstack", 4566, Wait.forListeningPort())
        .withLocalCompose(true)

    @JvmStatic
    @BeforeAll
    fun initializeInfra() {
      AwsLocalstackInitializer.setupInfra()
    }

    @JvmStatic
    @DynamicPropertySource
    fun dynamicProps(registry: DynamicPropertyRegistry) {
      registry.add("spring.data.mongodb.uri") { "mongodb://localhost:27017/credit-test" }
      registry.add("spring.cloud.aws.endpoint") { "http://localhost:4566" }
      registry.add("spring.cloud.aws.region.static") { "us-east-1" }
      registry.add("spring.cloud.aws.credentials.access-key") { "test" }
      registry.add("spring.cloud.aws.credentials.secret-key") { "test" }
    }
  }

  @Autowired
  private lateinit var wireMockServer: WireMockServer

  @LocalServerPort
  protected var port: Int = 0

  @BeforeEach
  fun setup() {
    if (port == 0) throw IllegalStateException("Port not initialized")
    println("▶️ WireMock running on port: ${wireMockServer.port()}")
    RestAssured.port = port
    RestAssured.config = RestAssured.config()
      .httpClient(
        HttpClientConfig.httpClientConfig()
          .setParam("http.connection.timeout", 5000)
          .setParam("http.socket.timeout", 5000)
          .setParam("http.connection-manager.timeout", 5000L)
      )
  }

  @AfterEach
  fun tearDown() {
    RestAssured.reset()
    WireMock.reset()
  }
}

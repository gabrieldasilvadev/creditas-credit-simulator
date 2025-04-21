package br.com.simulator.credit.creditas.integrationtest

import br.com.simulator.credit.creditas.container.Application
import br.com.simulator.credit.creditas.integrationtest.cofig.AwsLocalstackInitializer
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock
import io.restassured.RestAssured
import io.restassured.config.HttpClientConfig
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File
import java.time.Duration

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [Application::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureWireMock(port = 9291, stubs = ["classpath:/stubs"])
@ActiveProfiles("integration-test")
@Testcontainers
abstract class BaseIntegrationTest {
  companion object {
    private const val MONGO_SERVICE = "mongo"
    private const val MONGO_PORT = 37017
    private const val LOCALSTACK_SERVICE = "localstack"
    private const val LOCALSTACK_PORT = 4566

    private val logger = LoggerFactory.getLogger(BaseIntegrationTest::class.java)

    init {
      System.setProperty("testcontainers.reuse.enable", "true")
      System.setProperty("compose.container.id", "credit-test")
    }

    private val environment =
      DockerComposeContainer(File("docker-compose-test.yaml"))
        .withLocalCompose(true)
        .withExposedService(
          MONGO_SERVICE,
          MONGO_PORT,
          Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(60)),
        )
        .withExposedService(
          LOCALSTACK_SERVICE,
          LOCALSTACK_PORT,
          Wait.forListeningPort().withStartupTimeout(Duration.ofSeconds(60)),
        )

    init {
      try {
        environment.start()

        Thread.sleep(5000)
        val localstackHost = environment.getServiceHost(LOCALSTACK_SERVICE, LOCALSTACK_PORT)
        val localstackPort = environment.getServicePort(LOCALSTACK_SERVICE, LOCALSTACK_PORT)
        logger.info("LocalStack Available in $localstackHost:$localstackPort")

        AwsLocalstackInitializer.setupInfra(localstackHost, localstackPort)
      } catch (e: Exception) {
        logger.error("Error to initize container: ${e.message} | cause: ${e.cause}")
        e.printStackTrace()
      }

      try {
        environment.start()
        Thread.sleep(5000)

        val mongoHost = environment.getServiceHost(MONGO_SERVICE, MONGO_PORT)
        val mongoPort = environment.getServicePort(MONGO_SERVICE, MONGO_PORT)
        logger.info("MongoDB Available in $mongoHost:$mongoPort")
      } catch (e: Exception) {
        logger.error("Error to initize container: ${e.message} | cause: ${e.cause}")
      }
    }

    @JvmStatic
    @DynamicPropertySource
    fun dynamicProps(registry: DynamicPropertyRegistry) {
      try {
        val mongoHost = environment.getServiceHost(MONGO_SERVICE, MONGO_PORT)
        val mongoPort = environment.getServicePort(MONGO_SERVICE, MONGO_PORT)
        val localstackHost = environment.getServiceHost(LOCALSTACK_SERVICE, LOCALSTACK_PORT)
        val localstackPort = environment.getServicePort(LOCALSTACK_SERVICE, LOCALSTACK_PORT)

        logger.info("MongoDB: $mongoHost:$mongoPort")
        logger.info("Localstack: $localstackHost:$localstackPort")

        registry.add("spring.data.mongodb.uri") {
          "mongodb://root:rootpass@$mongoHost:$mongoPort/credit-test?authSource=admin"
        }

        registry.add("spring.cloud.aws.endpoint") { "http://$localstackHost:$localstackPort" }
        registry.add("spring.cloud.aws.region.static") { "us-east-1" }
        registry.add("spring.cloud.aws.credentials.access-key") { "test" }
        registry.add("spring.cloud.aws.credentials.secret-key") { "test" }
      } catch (e: Exception) {
        logger.info("Error when configuring dynamic properties: ${e.message}")
        e.printStackTrace()
      }
    }
  }

  @Autowired
  private lateinit var wireMockServer: WireMockServer

  @LocalServerPort
  protected var port: Int = 0

  @BeforeEach
  fun setup() {
    if (port == 0) throw IllegalStateException("Port not initialized")
    logger.info("WireMock running on port: ${wireMockServer.port()}")
    RestAssured.port = port
    RestAssured.config =
      RestAssured.config()
        .httpClient(
          HttpClientConfig.httpClientConfig()
            .setParam("http.connection.timeout", 10000)
            .setParam("http.socket.timeout", 10000)
            .setParam("http.connection-manager.timeout", 10000L),
        )
  }

  @AfterEach
  fun tearDown() {
    RestAssured.reset()
    WireMock.reset()
  }
}

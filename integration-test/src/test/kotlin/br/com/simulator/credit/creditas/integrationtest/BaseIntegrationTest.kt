package br.com.simulator.credit.creditas.integrationtest

import br.com.simulator.credit.creditas.container.Application
import com.github.tomakehurst.wiremock.client.WireMock
import io.restassured.RestAssured
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.TestInstance
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.cloud.contract.wiremock.AutoConfigureWireMock
import org.springframework.test.context.ActiveProfiles

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, classes = [Application::class])
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureWireMock(port = 8087, stubs = ["classpath:/stubs"])
@ActiveProfiles("integration-test")
abstract class BaseIntegrationTest {
  @LocalServerPort
  protected var port: Int = 0

  @BeforeEach
  fun setup() {
    if (port == 0) {
      throw IllegalStateException("Port not initialized")
    }
    RestAssured.port = port
  }

  @AfterEach
  fun tearDown() {
    RestAssured.reset()
    WireMock.reset()
  }
}

package br.com.simulator.credit.creditas.integrationtest

import io.restassured.RestAssured
import io.restassured.http.ContentType
import kotlin.test.Test
import org.hamcrest.CoreMatchers.notNullValue

class SimulationIntegrationTest : BaseIntegrationTest() {

  @Test
  fun `given valid simulation request when post to simulations then return 200`() {
    val payload = """
            {
              "loan_amount": {
                "amount": "10000.00",
                "currency": "BRL"
              },
              "customer_info": {
                "birth_date": "1990-04-15",
                "email": "cliente@teste.com"
              },
              "months": 12,
              "source_currency": "BRL",
              "target_currency": "USD"
            }
        """.trimIndent()

    RestAssured.given()
      .contentType(ContentType.JSON)
      .body(payload)
      .`when`()
      .post("/simulations")
      .then()
      .statusCode(200)
  }

  @Test
  fun `should simulate loan with BRL to BRL and age 30 (3 percent rate)`() {
    val payload = """
        {
          "loan_amount": { "amount": "5000.00", "currency": "BRL" },
          "customer_info": { "birth_date": "1994-04-15", "email": "user1@teste.com" },
          "months": 24,
          "source_currency": "BRL",
          "target_currency": "BRL"
        }
    """.trimIndent()

    RestAssured.given()
      .contentType(ContentType.JSON)
      .body(payload)
      .post("/simulations")
      .then()
      .statusCode(200)
      .body("target.monthly_installment", notNullValue())
  }

  @Test
  fun `should simulate loan with BRL to USD and age 22 (5 percent rate)`() {
    val payload = """
        {
          "loan_amount": { "amount": "15000.00", "currency": "BRL" },
          "customer_info": { "birth_date": "2002-04-15", "email": "user2@teste.com" },
          "months": 36,
          "source_currency": "BRL",
          "target_currency": "USD"
        }
    """.trimIndent()

    RestAssured.given()
      .contentType(ContentType.JSON)
      .body(payload)
      .post("/simulations")
      .then()
      .statusCode(200)
      .body("target.monthly_installment", notNullValue())
      .body("target.total_payment", notNullValue())
      .body("target.total_interest", notNullValue())
  }

  @Test
  fun `should simulate loan with BRL to USD and age 65 (4 percent rate)`() {
    val payload = """
        {
          "loan_amount": { "amount": "8000.00", "currency": "BRL" },
          "customer_info": { "birth_date": "1960-04-15", "email": "user3@teste.com" },
          "months": 18,
          "source_currency": "BRL",
          "target_currency": "USD"
        }
    """.trimIndent()

    RestAssured.given()
      .contentType(ContentType.JSON)
      .body(payload)
      .post("/simulations")
      .then()
      .statusCode(200)
      .body("target.monthly_installment", notNullValue())
      .body("target.total_payment", notNullValue())
      .body("target.total_interest", notNullValue())
  }
}

package br.com.simulator.credit.creditas.simulationdomain.model

import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.AgeBracket
import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.CustomerInfo
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class CustomerInfoTest {
  @Test
  fun `should calculate correct age`() {
    val birthDate = LocalDate.of(2000, 4, 10)
    val customer = CustomerInfo(birthDate, "email@example.com")

    val age = customer.age(onDate = LocalDate.of(2025, 4, 10))
    assertEquals(25, age)
  }

  @Test
  fun `should return correct age bracket`() {
    val customer = CustomerInfo(LocalDate.of(1989, 1, 1), "email@example.com")
    assertEquals(AgeBracket.FROM_26_TO_40, customer.ageBracket())
  }
}

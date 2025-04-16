package br.com.simulator.credit.creditas.simulationdomain.model

import br.com.simulator.credit.creditas.simulationdomain.model.valueobjects.AgeBracket
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class AgeBracketTest {
  @Test
  fun `should map correct age to age bracket`() {
    assertEquals(AgeBracket.UP_TO_25, AgeBracket.from(20))
    assertEquals(AgeBracket.FROM_26_TO_40, AgeBracket.from(30))
    assertEquals(AgeBracket.FROM_41_TO_60, AgeBracket.from(50))
    assertEquals(AgeBracket.ABOVE_60, AgeBracket.from(70))
  }
}

package br.com.simulator.credit.creditas.simulationdomain.model.valueobjects

import java.math.BigDecimal

enum class AgeBracket(val annualInterestRate: BigDecimal) {
  UP_TO_25(BigDecimal("0.05")),
  FROM_26_TO_40(BigDecimal("0.03")),
  FROM_41_TO_60(BigDecimal("0.02")),
  ABOVE_60(BigDecimal("0.04")),
  ;

  companion object {
    fun from(age: Int): AgeBracket =
      when (age) {
        in 0..25 -> UP_TO_25
        in 26..40 -> FROM_26_TO_40
        in 41..60 -> FROM_41_TO_60
        else -> ABOVE_60
      }
  }
}

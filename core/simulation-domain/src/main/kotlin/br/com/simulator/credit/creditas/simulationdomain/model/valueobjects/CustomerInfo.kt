package br.com.simulator.credit.creditas.simulationdomain.model.valueobjects

import java.time.LocalDate
import java.time.Period

data class CustomerInfo(val birthDate: LocalDate, val customerEmail: String) {
  fun age(onDate: LocalDate = LocalDate.now()): Int = Period.between(birthDate, onDate).years

  fun ageBracket(): AgeBracket = AgeBracket.from(age())
}

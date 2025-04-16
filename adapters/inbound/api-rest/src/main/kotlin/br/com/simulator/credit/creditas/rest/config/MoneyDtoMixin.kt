package br.com.simulator.credit.creditas.rest.config

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty

abstract class MoneyDtoMixin
  @JsonCreator
  constructor(
    @JsonProperty("amount") amount: String,
    @JsonProperty("currency") currency: String,
  )

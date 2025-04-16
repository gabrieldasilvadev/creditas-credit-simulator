package br.com.simulator.credit.creditas.exchangerate.responses

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

data class AwesomeApiResponse
  @JsonCreator
  constructor(
    @JsonProperty("USDBRL")
    val usdBrl: CurrencyPair,
    @JsonProperty("EURBRL")
    val eurBrl: CurrencyPair,
  ) {
    data class CurrencyPair
      @JsonCreator
      constructor(
        @JsonProperty("code")
        val code: String,
        @JsonProperty("codein")
        val codein: String,
        @JsonProperty("name")
        val ask: BigDecimal,
      )
  }

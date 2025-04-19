package br.com.simulator.credit.creditas.messaging.sqs.email

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class EmailEnvelope
  @JsonCreator
  constructor(
    @JsonProperty("Type")
    val type: String,
    @JsonProperty("MessageId")
    val messageId: String,
    @JsonProperty("Message")
    val message: String,
    @JsonProperty("Timestamp")
    val timestamp: String,
  )

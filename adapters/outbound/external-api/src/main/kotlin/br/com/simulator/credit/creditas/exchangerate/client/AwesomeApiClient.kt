package br.com.simulator.credit.creditas.exchangerate.client

import org.springframework.cloud.openfeign.FeignClient
import org.springframework.stereotype.Component
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable

@FeignClient(name = "exchangeClient", url = "\${exchange.url}")
@Component
interface AwesomeApiClient {
  @GetMapping("/json/last/{from}-{to}")
  fun getRate(
    @PathVariable from: String,
    @PathVariable to: String,
  ): Map<String, Map<String, String>>
}

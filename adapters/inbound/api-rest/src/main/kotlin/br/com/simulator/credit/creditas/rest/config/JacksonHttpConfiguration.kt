package br.com.simulator.credit.creditas.rest.config

import br.com.simulator.credit.openapi.web.dto.MoneyDto
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

@Configuration
class JacksonHttpConfiguration {
  @Bean
  fun objectMapper(builder: Jackson2ObjectMapperBuilder): ObjectMapper {
    return builder.build<ObjectMapper>().apply {
      addMixIn(MoneyDto::class.java, MoneyDtoMixin::class.java)
    }
  }
}

package br.com.simulator.credit.creditas.container.config.jackson

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

@Configuration
class JacksonCustomizer : Jackson2ObjectMapperBuilderCustomizer {
  override fun customize(builder: Jackson2ObjectMapperBuilder) {
    builder.modulesToInstall(JavaTimeModule())
    builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
  }
}

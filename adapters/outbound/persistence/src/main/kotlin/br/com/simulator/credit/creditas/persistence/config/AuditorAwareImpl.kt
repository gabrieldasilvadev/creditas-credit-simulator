package br.com.simulator.credit.creditas.persistence.config

import org.springframework.data.domain.AuditorAware
import java.util.Optional
import java.util.UUID

class AuditorAwareImpl : AuditorAware<UUID> {
  override fun getCurrentAuditor(): Optional<UUID> {
    return Optional.of(UUID.randomUUID())
  }
}

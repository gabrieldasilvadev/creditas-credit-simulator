package br.com.simulator.credit.creditas.command

import kotlinx.coroutines.sync.Semaphore
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SimulationSemaphoreProvider(
  @Value("\${simulation.concurrency.limit:100}")
  private val maxConcurrentSimulations: Int,
) {
  val semaphore: Semaphore = Semaphore(maxConcurrentSimulations)
}

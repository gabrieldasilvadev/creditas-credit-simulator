package br.com.simulator.credit.creditas.command

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

internal class SimulationSemaphoreProviderTest {
  @ParameterizedTest
  @ValueSource(ints = [1, 10, 20, 40, 80, 100])
  fun `should create semaphore with configured permits`(size: Int) {
    val provider = SimulationSemaphoreProvider(size)

    val semaphore = provider.semaphore
    assertEquals(
      size,
      semaphore.availablePermits,
      "Semaphore should be initialized with $size permits",
    )
  }
}

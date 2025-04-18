package br.com.simulator.credit.creditas.infrastructure.annotations

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class Monitorable(
  val value: String = "",
)

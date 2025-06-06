package br.com.simulator.credit.creditas.infrastructure.annotations

import java.lang.annotation.Inherited

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Inherited
annotation class Monitorable(
  val value: String = "",
)

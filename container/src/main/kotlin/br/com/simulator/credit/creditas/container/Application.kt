package br.com.simulator.credit.creditas.container

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication
import org.springframework.context.annotation.ComponentScan

@SpringBootApplication
@ComponentScan(
  basePackages = ["br.com.simulator.credit.creditas"],
)
@EnableConfigurationProperties
class Application

fun main(args: Array<String>) {
  runApplication<Application>(*args)
}

package br.com.simulator.credit.creditas.commondomain.ports

interface EmailSender<T> {
  fun send(content: T)
}

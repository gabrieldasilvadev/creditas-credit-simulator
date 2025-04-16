package br.com.simulator.credit.creditas.commondomain

interface EmailSender<T> {
  fun send(content: T)
}

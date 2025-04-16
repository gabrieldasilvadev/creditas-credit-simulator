package br.com.simulator.credit.creditas.simulationdomain.policy

import org.apache.commons.lang3.StringUtils

enum class PolicyType(val value: String) {
  FIXED("fixed"),
  AGE_BASED("age"),
  ;

  companion object {
    fun entryOf(value: String): PolicyType {
      return entries.firstOrNull { StringUtils.equalsIgnoreCase(it.value, value) }
        ?: throw IllegalArgumentException("Invalid policy type: $value")
    }
  }
}

package br.com.simulator.credit.creditas.messaging.sns

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "aws.sns")
class SnsTopicsProperties {
  lateinit var topicNames: Map<String, String>

  fun getTopicArn(topicKey: String): String =
    topicNames[topicKey] ?: throw IllegalArgumentException("Topic not configured: $topicKey")
}

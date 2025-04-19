package br.com.simulator.credit.creditas.integrationtest.cofig

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import com.amazonaws.services.sns.model.CreateTopicRequest
import com.amazonaws.services.sqs.AmazonSQSClientBuilder
import com.amazonaws.services.sqs.model.CreateQueueRequest
import java.time.Duration
import org.awaitility.Awaitility
import org.awaitility.core.ConditionTimeoutException
import org.slf4j.LoggerFactory

object AwsLocalstackInitializer {
  private const val REGION = "us-east-1"
  private const val ACCESS_KEY = "test"
  private const val SECRET_KEY = "test"

  private val logger = LoggerFactory.getLogger(AwsLocalstackInitializer::class.java)

  fun setupInfra(host: String, port: Int) {
    val endpoint = "http://$host:$port"
    logger.info("Configuring AWS with Endpoint: $endpoint")

    try {
      Awaitility.await()
        .atMost(Duration.ofSeconds(30))
        .pollInterval(Duration.ofSeconds(2))
        .until {
          try {
            val connection = java.net.URL("$endpoint/health").openConnection() as java.net.HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 2000
            connection.connect()
            val responseCode = connection.responseCode
            logger.info("Locastack Health Verification: $responseCode")
            responseCode in 200..299
          } catch (e: Exception) {
            logger.info("Waiting for LocalStack to be available...")
            false
          }
        }
    } catch (e: ConditionTimeoutException) {
      logger.info("Locastack may not be completely ready, but we will try to continue")
    }

    try {
      val credentials = BasicAWSCredentials(ACCESS_KEY, SECRET_KEY)

      val sqsClient = AmazonSQSClientBuilder.standard()
        .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(endpoint, REGION))
        .withCredentials(AWSStaticCredentialsProvider(credentials))
        .build()

      val snsClient = AmazonSNSClientBuilder.standard()
        .withEndpointConfiguration(AwsClientBuilder.EndpointConfiguration(endpoint, REGION))
        .withCredentials(AWSStaticCredentialsProvider(credentials))
        .build()

      val queueAttributes = HashMap<String, String>()
      queueAttributes["ReceiveMessageWaitTimeSeconds"] = "20"
      queueAttributes["VisibilityTimeout"] = "30"

      val createQueueRequest = CreateQueueRequest()
        .withQueueName("credit-simulator-queue")
        .withAttributes(queueAttributes)

      try {
        val queueUrl = sqsClient.createQueue(createQueueRequest).queueUrl
        logger.info("SQS line created: $queueUrl")

        val createTopicRequest = CreateTopicRequest()
          .withName("credit-simulator-topic")

        val topicArn = snsClient.createTopic(createTopicRequest).topicArn
        logger.info("SNS Topic created: $topicArn")

        val queueArn = sqsClient.getQueueAttributes(queueUrl, listOf("QueueArn"))
          .attributes["QueueArn"]

        snsClient.subscribe(topicArn, "sqs", queueArn)
        logger.info("Queue registered in the Topic SNS")
      } catch (e: Exception) {
        logger.error("SQS/SNS configuration error: ${e.message} | cause ${e.cause}")
        e.printStackTrace()
      }
    } catch (e: Exception) {
      logger.info("Error configuring infrastructure AWS: ${e.message}")
      e.printStackTrace()
    }
  }
}

package br.com.simulator.credit.creditas.integrationtest.cofig

import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration
import com.amazonaws.services.sns.AmazonSNS
import com.amazonaws.services.sns.AmazonSNSClientBuilder
import com.amazonaws.services.sqs.AmazonSQS
import com.amazonaws.services.sqs.AmazonSQSClientBuilder

object AwsLocalstackInitializer {
  private const val endpoint = "http://localhost:4566"
  private const val region = "us-east-1"
  private val credentials = BasicAWSCredentials("test", "test")

  private lateinit var sqs: AmazonSQS

  private lateinit var sns: AmazonSNS

  fun setupInfra() {
    sqs =
      AmazonSQSClientBuilder.standard()
        .withEndpointConfiguration(EndpointConfiguration(endpoint, region))
        .withCredentials(AWSStaticCredentialsProvider(credentials))
        .build()

    sns =
      AmazonSNSClientBuilder.standard()
        .withEndpointConfiguration(EndpointConfiguration(endpoint, region))
        .withCredentials(AWSStaticCredentialsProvider(credentials))
        .build()

    val topicArn = sns.createTopic("credit-simulation-events").topicArn
    val queueUrl = sqs.createQueue("credit-simulation-queue").queueUrl

    val queueArn =
      sqs.getQueueAttributes(queueUrl, listOf("QueueArn"))
        .attributes["QueueArn"] ?: throw IllegalStateException("Queue ARN not found")

    sns.subscribe(topicArn, "sqs", queueArn)
  }
}

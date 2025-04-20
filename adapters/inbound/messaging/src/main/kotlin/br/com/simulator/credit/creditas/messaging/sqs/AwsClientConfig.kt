package br.com.simulator.credit.creditas.messaging.sqs

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsClient
import java.net.URI

@Configuration
class AwsClientConfig {
  @Bean
  fun sqsClient(): SqsClient {
    return SqsClient.builder()
      .endpointOverride(URI.create("http://localhost:4566"))
      .region(Region.US_EAST_1)
      .credentialsProvider(
        StaticCredentialsProvider.create(
          AwsBasicCredentials.create("localstack", "localstack"),
        ),
      )
      .build()
  }
}

package br.com.simulator.credit.creditas.container

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsClient
import java.net.URI

@Configuration
class AwsConfiguration {
  @Bean
  fun sqsClient(): SqsClient {
    return SqsClient.builder()
      .region(Region.of("us-east-1"))
      .endpointOverride(URI("http://localhost:4566"))
      .credentialsProvider {
        AwsBasicCredentials.create("localstack", "localstack")
      }
      .build()
  }
}

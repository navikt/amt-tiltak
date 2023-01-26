package no.nav.amt.tiltak.kafka.config

import io.confluent.kafka.schemaregistry.client.CachedSchemaRegistryClient
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClient
import io.confluent.kafka.schemaregistry.client.SchemaRegistryClientConfig
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile

@Configuration
open class SchemaRegistryConfiguration {
	@Profile("default")
	@Bean
	open fun schemaRegistryClient(
		@Value("\${kafka.schema.registry.url}") schemaRegistryUrl: String,
		@Value("\${kafka.schema.registry.username}") schemaRegistryUsername: String,
		@Value("\${kafka.schema.registry.password}") schemaRegistryPassword: String,
	) : SchemaRegistryClient {
		val configs: MutableMap<String, Any> = HashMap()
		configs[SchemaRegistryClientConfig.BASIC_AUTH_CREDENTIALS_SOURCE] = "USER_INFO"
		configs[SchemaRegistryClientConfig.USER_INFO_CONFIG] = String.format("%s:%s", schemaRegistryUsername, schemaRegistryPassword)

		return CachedSchemaRegistryClient(schemaRegistryUrl, 100, configs)
	}
}

package no.nav.amt.tiltak.clients.norg

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
open class NorgClientConfig {

	@Value("\${norg.url}")
	lateinit var url: String

	@Bean
	open fun norgClient(): NorgClient {
		return NorgClientImpl(url)
	}

}

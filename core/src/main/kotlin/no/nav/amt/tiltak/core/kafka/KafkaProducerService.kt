package no.nav.amt.tiltak.core.kafka

interface KafkaProducerService {

	fun sendNavEnhet(navEnhetKafkaDto: NavEnhetKafkaDto)

}

data class NavEnhetKafkaDto(
	val enhetId: String,
	val navn: String
)

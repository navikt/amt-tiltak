package no.nav.amt.tiltak.core.kafka

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import java.time.LocalDateTime
import java.util.*

interface KafkaProducerService {

	fun publiserDeltaker(deltaker: Deltaker, endretDato: LocalDateTime)

	fun publiserSlettDeltaker(deltakerId: UUID)

}

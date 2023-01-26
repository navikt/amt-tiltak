package no.nav.amt.tiltak.core.kafka

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import java.util.UUID

interface KafkaProducerService {

	fun publiserDeltaker(deltaker: Deltaker)

	fun publiserSlettDeltaker(deltakerId: UUID)

}

package no.nav.amt.navansatt

import no.nav.amt.tiltak.core.port.VeilederConnector
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

@Component
class NavAnsattUpdater(
	private val veilederConnector: VeilederConnector,

) {

	fun oppdaterBatch() {
		BatchBucket().id

		veilederConnector.hentVeileder()
	}

}

private const val bucketsPerDay : Int = 24 * 12 // 12 buckets per hour/5 minute interval

class BatchBucket {

	val id: Int

	constructor(uuid: UUID) {
		val mostSignificantBitsValue = uuid.mostSignificantBits and Long.MAX_VALUE // positive value of most significant bits
		id = mostSignificantBitsValue.toInt() % bucketsPerDay
	}

	constructor(time : LocalDateTime = LocalDateTime.now()) {
		id = (time.hour * 60 + time.minute) % bucketsPerDay
	}
}

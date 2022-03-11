package no.nav.amt.navansatt

import java.time.LocalDateTime
import java.util.*

private const val bucketsPerDay : Int = 24 * 12 // 12 buckets per hour/5 minute interval

/**
 * Fordeler døgnet i 5 minutters buckets, eller UUIDer i like mange buckets
 */
internal class NavAnsattBucket {

	internal val id: Int

	internal companion object {
		fun forUuid(uuid: UUID) = NavAnsattBucket(uuid)
		fun forCurrentTime() = NavAnsattBucket(LocalDateTime.now())
	}

	private constructor(uuid: UUID) {
		val mostSignificantBitsValue = uuid.mostSignificantBits and Long.MAX_VALUE // positive value of most significant bits
		id = mostSignificantBitsValue.toInt() % bucketsPerDay
	}

	private constructor(time : LocalDateTime) {
		id = (time.hour * 60 + time.minute) % bucketsPerDay
	}
}

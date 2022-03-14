package no.nav.amt.navansatt

import java.time.LocalTime
import kotlin.math.abs


private const val bucketsPerDay : Int = 24 * 12 // 12 buckets per hour/5 minute interval
private const val bucketIntervalInMinutes : Int = 5 // every 5 minutes

/**
 * Fordeler døgnet i 5 minutters buckets, eller UUIDer i like mange buckets
 */
internal data class Bucket(
	internal val id: Int
) {
	init {
	    require(id < bucketsPerDay) { "Bucket er utenfor forventet intervall"}
	}

	internal companion object {

		fun forNavIdent(navIdent: String) = Bucket(abs(navIdent.hashCode() % bucketsPerDay))
		fun forTidspunkt(time: LocalTime = LocalTime.now()) = Bucket(time)
	}

	private constructor(time : LocalTime) : this(id = (time.hour * 60 + time.minute) / bucketIntervalInMinutes)

}

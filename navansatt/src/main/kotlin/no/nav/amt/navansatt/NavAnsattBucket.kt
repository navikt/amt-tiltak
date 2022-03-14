package no.nav.amt.navansatt

import no.nav.common.types.identer.NavIdent
import org.springframework.util.DigestUtils
import java.time.LocalDateTime
import java.util.*

private const val bucketsPerDay : Int = 24 * 12 // 12 buckets per hour/5 minute interval

/**
 * Fordeler døgnet i 5 minutters buckets, eller UUIDer i like mange buckets
 */
internal class NavAnsattBucket {

	internal val id: Int

	internal companion object {
		fun forNavIdent(navIdent: String) = NavAnsattBucket(navIdent)
		fun forCurrentTime() = NavAnsattBucket(LocalDateTime.now())
	}

	private constructor(navIdent: String) {
		id = navIdent.hashCode() % bucketsPerDay
	}

	private constructor(time : LocalDateTime) {
		id = (time.hour * 60 + time.minute) % bucketsPerDay
	}
}

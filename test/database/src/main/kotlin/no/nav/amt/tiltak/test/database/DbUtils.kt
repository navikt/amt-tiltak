package no.nav.amt.tiltak.test.database

import io.kotest.matchers.date.shouldBeWithin
import io.kotest.matchers.shouldNotBe
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZonedDateTime

object DbUtils {

	/**
	 * A helping function as SQL Timestamp and LocalDateTime does not have the same precision
	 */
	fun LocalDateTime.isEqualTo(other: LocalDateTime?): Boolean {
		if (other == null) {
			return false
		}

		return this.year == other.year
			&& this.month == other.month
			&& this.dayOfMonth == other.dayOfMonth
			&& this.hour == other.hour
			&& this.minute == other.minute
			&& this.second == other.second
	}

	/**
	 * Should be used to check equality, 1 second skew is allowed to work around different precision on milliseconds
	 */
	infix fun ZonedDateTime.shouldBeEqualTo(expected: ZonedDateTime?) {
		expected shouldNotBe null
		expected!!.shouldBeWithin(Duration.ofSeconds(1), this)
	}
	infix fun ZonedDateTime.shouldBeCloseTo(expected: ZonedDateTime?) {
		expected shouldNotBe null
		expected!!.shouldBeWithin(Duration.ofSeconds(10), this)
	}

	fun LocalDate.isEqualTo(other: LocalDate?): Boolean {
		if (other == null) {
			return false
		}

		return this.year == other.year
			&& this.month == other.month
			&& this.dayOfMonth == other.dayOfMonth
	}

}

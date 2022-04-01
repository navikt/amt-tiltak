package no.nav.amt.tiltak.test.database

import java.time.LocalDate
import java.time.LocalDateTime

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

	fun LocalDate.isEqualTo(other: LocalDate?): Boolean {
		if (other == null) {
			return false
		}

		return this.year == other.year
			&& this.month == other.month
			&& this.dayOfMonth == other.dayOfMonth
	}

}

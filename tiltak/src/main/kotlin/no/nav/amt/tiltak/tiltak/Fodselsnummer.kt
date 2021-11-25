package no.nav.amt.tiltak.tiltak

import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class FodselOgPersonNr(
	val fodselOgPersonsNr: String
) {
	init {
		fodselOgPersonsNr.toInt()
	    if(fodselOgPersonsNr.length != 11) {
			throw RuntimeException(
				"Fødsels- og personnummer må være 11 siffer. Lengde var: " +
					"${fodselOgPersonsNr.length}"
			)
		}
	}

	fun toDate(): LocalDate {
		val format = DateTimeFormatter.ofPattern("ddMMuu")
		return LocalDate.parse(fodselOgPersonsNr, format)
	}

	fun toFnr() = fodselOgPersonsNr.substring(0, 10)

	override fun toString() = fodselOgPersonsNr
}

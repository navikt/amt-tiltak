package no.nav.amt.tiltak.core.domain.tiltak

import java.math.BigInteger
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Fodselsnummer(
	val fodselsnummer: String
) {

	init {
		BigInteger(fodselsnummer)
	    if(fodselsnummer.length != 11) {
			throw RuntimeException(
				"Fødsels- og personnummer må være 11 siffer. Lengde var: " +
					"${fodselsnummer.length}"
			)
		}
	}
/**
 * index: 01 23 45 679
 * fnr	  15 01 95 231 00
 */
	fun getFodselsdato() = fodselsnummer.substring(0, 6)
	fun getFodselsAar() = fodselsnummer.substring(4, 6)
	fun getIndividsifre() = fodselsnummer.substring(6, 9)

	fun toFodselDato(): LocalDate {
		val format = DateTimeFormatter.ofPattern("ddMMuuuu")
		val aarstall = getAarstall()
		val fnr = getFodselsdato()
		val fnrMedAarstall = fnr.substring(0, 4) + aarstall

		return LocalDate.parse(fnrMedAarstall, format)
	}

	fun getAarstall(): String {
		val aarsTall = getFodselsAar().toInt()
		val individsifre = getIndividsifre().toInt()

		val aarhundre = when {
			individsifre in 0..499 -> "19"
			individsifre in 500..999 && aarsTall<40 -> "20"
			individsifre in 500..749 -> "18"
			individsifre in 900..999 -> "19"
			else -> throw NumberFormatException("Ugyldige individsifre i fødselsnummer $individsifre")
		}
		return "$aarhundre${getFodselsAar()}"

	}
	override fun toString() = fodselsnummer
}

package no.nav.amt.tiltak.ingestors.arena.processors

import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

fun String.asLocalDate(): LocalDate {
	val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
	return LocalDate.parse(this, formatter)
}

fun String.asLocalDateTime(): LocalDateTime {
	val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
	return LocalDateTime.parse(this, formatter)
}

fun String?.asTime(): LocalTime {
	val logger = LoggerFactory.getLogger(String::class.java)

	if (this == null) {
		return LocalTime.MIDNIGHT
	} else if (this.matches("\\d\\d:\\d\\d".toRegex())) {
		val split = this.split(":")
		return LocalTime.of(split[0].toInt(), split[1].toInt())
	} else if (this.matches("\\d\\d\\.\\d\\d".toRegex())) {
		val split = this.split(".")
		return LocalTime.of(split[0].toInt(), split[1].toInt())
	} else if (this.matches("\\d\\d\\d\\d".toRegex())) {
		val hour = this.substring(0, 2)
		val minutes = this.substring(2, 4)

		return LocalTime.of(hour.toInt(), minutes.toInt())
	}

	if (this != null) logger.warn("Det er ikke implementert en handler for klokketid, pattern: $this")
	return LocalTime.MIDNIGHT
}

infix fun LocalDate?.withTime(time: LocalTime) =
	if (this != null) LocalDateTime.of(this, time) else null

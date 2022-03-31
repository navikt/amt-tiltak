package no.nav.amt.tiltak.common.db_utils

import java.sql.ResultSet
import java.time.*
import java.util.*

fun ResultSet.getUUID(columnLabel: String): UUID {
	return getNullableUUID(columnLabel) ?: throw IllegalStateException("Expected $columnLabel not to be null")
}

fun ResultSet.getNullableUUID(columnLabel: String): UUID? {
	return this.getString(columnLabel)
		?.let { UUID.fromString(it) }
}

fun ResultSet.getLocalDateTime(columnLabel: String): LocalDateTime {
	return getNullableLocalDateTime(columnLabel) ?: throw IllegalStateException("Expected $columnLabel not to be null")
}

fun ResultSet.getNullableLocalDateTime(columnLabel: String): LocalDateTime? {
	return this.getTimestamp(columnLabel)?.toLocalDateTime()
}

fun ResultSet.getLocalDate(columnLabel: String): LocalDate {
	return getNullableLocalDate(columnLabel) ?: throw IllegalStateException("Expected $columnLabel not to be null")
}

fun ResultSet.getNullableLocalDate(columnLabel: String): LocalDate? {
	return this.getDate(columnLabel)?.toLocalDate()
}

fun ResultSet.getNullableZonedDateTime(columnLabel: String): ZonedDateTime? {
	val timestamp = this.getTimestamp(columnLabel) ?: return null
	return ZonedDateTime.ofInstant(Instant.ofEpochMilli(timestamp.time), ZoneOffset.UTC)
}

fun ResultSet.getZonedDateTime(columnLabel: String): ZonedDateTime {
	return getNullableZonedDateTime(columnLabel) ?: throw IllegalStateException("Expected $columnLabel not to be null")
}

fun ResultSet.getNullableString(columnLabel: String): String? {
	return this.getString(columnLabel)
}

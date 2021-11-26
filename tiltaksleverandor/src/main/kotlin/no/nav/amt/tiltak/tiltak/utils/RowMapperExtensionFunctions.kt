package no.nav.amt.tiltak.tiltak.utils

import java.sql.ResultSet
import java.time.LocalDateTime
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

fun ResultSet.getNullableLocalDateTime(columnLabel: String): LocalDateTime?  {
	return this.getTimestamp(columnLabel)?.toLocalDateTime()
}

package no.nav.amt.tiltak.tiltak.utils

import java.sql.ResultSet
import java.util.*

fun ResultSet.getUUID(columnLabel: String): UUID {
	return getNullableUUID(columnLabel) ?: throw IllegalStateException("Expected $columnLabel not to be null")
}

fun ResultSet.getNullableUUID(columnLabel: String): UUID? {
	return this.getString(columnLabel)
		?.let { UUID.fromString(it) }
}

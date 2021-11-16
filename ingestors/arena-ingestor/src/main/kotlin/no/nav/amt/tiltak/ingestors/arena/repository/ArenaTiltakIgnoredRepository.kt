package no.nav.amt.tiltak.ingestors.arena.repository

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Component

@Component
open class ArenaTiltakIgnoredRepository(
	private val jdbcTemplate: JdbcTemplate
) {

	fun insert(tiltakId: Long) {
		val sql = "INSERT INTO arena_tiltak_ids_ignored (tiltak_id) VALUES (?) ON CONFLICT DO NOTHING"

		jdbcTemplate.update(sql, tiltakId)
	}

	fun contains(tiltakId: Long): Boolean {
		val rowMapper = RowMapper { rs, _ -> rs.getLong("tiltak_id") }

		val sql = "SELECT * FROM arena_tiltak_ids_ignored WHERE tiltak_id = ?"

		return jdbcTemplate.query(sql, rowMapper, tiltakId).isNotEmpty()
	}

}

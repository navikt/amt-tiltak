package no.nav.amt.tiltak.tiltak.repositories

import no.nav.amt.tiltak.tiltak.dbo.TiltakDbo
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.util.*

@Component
open class TiltakRepository(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		TiltakDbo(
			id = UUID.fromString(rs.getString("id")),
			navn = rs.getString("navn"),
			type = rs.getString("type"),
			createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
			modifiedAt = rs.getTimestamp("modified_at").toLocalDateTime()
		)
	}


	fun insert(id: UUID, navn: String, kode: String): TiltakDbo {
		//language=PostgreSQL
		val sql = """
            INSERT INTO tiltak(id, navn, type) VALUES (:id, :navn, :kode)
    	""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"id" to id,
				"navn" to navn,
				"kode" to kode
			)
		)

		template.update(sql, parameters)

		return get(id)
			?: throw NoSuchElementException("Tiltak med id $id finnes ikke")
	}

	fun update(id: UUID, navn: String, type: String): TiltakDbo {
		//language=PostgreSQL
		val sql = """
			UPDATE tiltak
			SET navn        = :navn,
				type        = :type,
				modified_at = :modifiedAt
			WHERE id = :id
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"navn" to navn,
				"type" to type,
				"modifiedAt" to LocalDateTime.now(),
				"id" to id
			)
		)

		template.update(sql, parameters)

		return get(id)
			?: throw NoSuchElementException("Tiltak med id ${id} finnes ikke")
	}

	fun getAll(): List<TiltakDbo> {
		return template.query(
			"SELECT * FROM tiltak",
			rowMapper
		)
	}

	private fun get(id: UUID): TiltakDbo? {
		//language=PostgreSQL
		val sql = """
            SELECT *
            FROM tiltak
			WHERE id = :id
        """.trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"id" to id
			)
		)

		return template.query(sql, parameters, rowMapper)
			.firstOrNull()
	}
}

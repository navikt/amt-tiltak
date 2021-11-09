package no.nav.amt.tiltak.tiltak.repositories

import no.nav.amt.tiltak.tiltak.dbo.TiltakDbo
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
open class TiltakRepository(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		TiltakDbo(
			internalId = rs.getInt("id"),
			externalId = UUID.fromString(rs.getString("external_id")),
			arenaId = rs.getString("arena_id"),
			navn = rs.getString("navn"),
			type = rs.getString("type"),
			createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
			modifiedAt = rs.getTimestamp("modified_at").toLocalDateTime()
		)
	}


	fun insert(arenaId: String, navn: String, kode: String): TiltakDbo {
		//language=PostgreSQL
		val sql = """
            insert into tiltak(external_id, arena_id, navn, type)
            values (:externalId,
                    :arenaId,
                    :navn,
                    :kode)
    """.trimIndent()

		val externalId = UUID.randomUUID()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"externalId" to externalId,
				"arenaId" to arenaId,
				"navn" to navn,
				"kode" to kode
			)
		)

		template.update(sql, parameters)

		return get(externalId)
			?: throw NoSuchElementException("Tiltak med id $externalId finnes ikke")
	}

	fun update(tiltak: TiltakDbo): TiltakDbo {
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
				"navn" to tiltak.navn,
				"type" to tiltak.type,
				"modifiedAt" to tiltak.modifiedAt,
				"id" to tiltak.internalId
			)
		)

		template.update(sql, parameters)

		return get(tiltak.externalId)
			?: throw NoSuchElementException("Tiltak med id ${tiltak.externalId} finnes ikke")
	}

	fun get(id: UUID): TiltakDbo? {
		//language=PostgreSQL
		val sql = """
            select id,
                   external_id,
                   arena_id,
                   navn,
                   type,
                   created_at,
                   modified_at
            from tiltak
			WHERE external_id = :external_id
        """.trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"external_id" to id
			)
		)

		return template.query(sql, parameters, rowMapper)
			.firstOrNull()
	}

	fun getByArenaId(arenaId: String): TiltakDbo? {
		//language=PostgreSQL
		val sql = """
            select id,
                   external_id,
                   arena_id,
                   navn,
                   type,
                   created_at,
                   modified_at
            from tiltak
			WHERE arena_id = :arena_id
        """.trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"arena_id" to arenaId
			)
		)

		return template.query(sql, parameters, rowMapper)
			.firstOrNull()
	}
}

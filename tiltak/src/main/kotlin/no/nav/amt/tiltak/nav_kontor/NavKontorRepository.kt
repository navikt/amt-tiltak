package no.nav.amt.tiltak.nav_kontor

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.utils.getUUID
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
open class NavKontorRepository(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		NavKontorDbo(
			id = rs.getUUID("id"),
			enhetId = rs.getString("enhet_id"),
			navn = rs.getString("navn")
		)
	}

	fun upsert(enhetId: String, navn: String): NavKontorDbo {
		val sql = """
			INSERT INTO nav_kontor(id, enhet_id, navn)
			VALUES (:id,
					:enhetId,
					:navn)
			ON CONFLICT (enhet_id) DO UPDATE SET navn = :navn
		""".trimIndent()

		val id = UUID.randomUUID()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"id" to id,
				"enhetId" to enhetId,
				"navn" to navn
			)
		)

		template.update(sql, parameters)
		return getByEnhetId(enhetId)
	}

	fun get(id: UUID): NavKontorDbo {
		return template.query(
			"SELECT * FROM nav_kontor WHERE id = :id",
			MapSqlParameterSource().addValues(mapOf("id" to id)),
			rowMapper
		).firstOrNull() ?: throw NoSuchElementException("Kontor med id $id eksisterer ikke.")
	}

	fun hentEnheter(enhetIder: List<String>): List<NavKontorDbo> {
		val sql = """
			SELECT * FROM nav_kontor WHERE enhet_id in(:enhetIder)
		""".trimIndent()

		return template.query(
			sql,
			sqlParameters("enhetIder" to enhetIder),
			rowMapper
		)
	}

	private fun getByEnhetId(enhetId: String): NavKontorDbo {
		return template.query(
			"SELECT * FROM nav_kontor WHERE enhet_id = :enhetId",
			MapSqlParameterSource().addValues(mapOf("enhetId" to enhetId)),
			rowMapper
		).firstOrNull() ?: throw NoSuchElementException("Kontor med enhetId $enhetId eksisterer ikke.")
	}
}

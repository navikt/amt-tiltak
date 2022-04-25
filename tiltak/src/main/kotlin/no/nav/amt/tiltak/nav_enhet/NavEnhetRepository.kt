package no.nav.amt.tiltak.nav_enhet

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.utils.getUUID
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
open class NavEnhetRepository(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		NavEnhetDbo(
			id = rs.getUUID("id"),
			enhetId = rs.getString("enhet_id"),
			navn = rs.getString("navn")
		)
	}

	fun upsert(enhetId: String, navn: String): NavEnhetDbo {
		val sql = """
			INSERT INTO nav_enhet(id, enhet_id, navn)
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
		return hentEnhet(enhetId) ?: throw NoSuchElementException("Enhet med enhetId $enhetId eksisterer ikke.")
	}

	fun get(id: UUID): NavEnhetDbo {
		return template.query(
			"SELECT * FROM nav_enhet WHERE id = :id",
			MapSqlParameterSource().addValues(mapOf("id" to id)),
			rowMapper
		).firstOrNull() ?: throw NoSuchElementException("Enhet med id $id eksisterer ikke.")
	}

	fun hentEnheter(enhetIder: List<String>): List<NavEnhetDbo> {
		val sql = """
			SELECT * FROM nav_enhet WHERE enhet_id in(:enhetIder)
		""".trimIndent()

		return template.query(
			sql,
			sqlParameters("enhetIder" to enhetIder),
			rowMapper
		)
	}

	fun hentEnhet(enhetId: String): NavEnhetDbo? {
		val sql = """
			SELECT * FROM nav_enhet WHERE enhet_id = :enhetId
		""".trimIndent()

		return template.query(
			sql,
			sqlParameters("enhetId" to enhetId),
			rowMapper
		).firstOrNull()
	}

}

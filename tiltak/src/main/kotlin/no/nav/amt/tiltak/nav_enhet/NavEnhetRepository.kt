package no.nav.amt.tiltak.nav_enhet

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.utils.getUUID
import org.springframework.jdbc.core.RowMapper
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

	fun insert(input: NavEnhetInsertInput) {
		val sql = """
			INSERT INTO nav_enhet(id, enhet_id, navn) VALUES (:id, :enhetId, :navn)
		""".trimIndent()

		val parameters = sqlParameters(
			"id" to input.id,
			"enhetId" to input.enhetId,
			"navn" to input.navn
		)

		template.update(sql, parameters)
	}

	fun get(id: UUID): NavEnhetDbo {
		val sql = """
			SELECT * FROM nav_enhet WHERE id = :id
		""".trimIndent()

		val parameters = sqlParameters("id" to id)

		return template.query(sql, parameters, rowMapper).firstOrNull()
			?: throw NoSuchElementException("Enhet med id $id eksisterer ikke.")
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

data class NavEnhetInsertInput(
	val id: UUID,
	val enhetId: String,
	val navn: String
)

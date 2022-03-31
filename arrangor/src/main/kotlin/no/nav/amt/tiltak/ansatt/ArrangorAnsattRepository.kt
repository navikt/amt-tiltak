package no.nav.amt.tiltak.ansatt

import no.nav.amt.tiltak.common.db_utils.getLocalDateTime
import no.nav.amt.tiltak.common.db_utils.getUUID
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
open class ArrangorAnsattRepository(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		AnsattDbo(
			id = rs.getUUID("id"),
			personligIdent = rs.getString("personlig_ident"),
			fornavn = rs.getString("fornavn"),
			etternavn = rs.getString("etternavn"),
			createdAt = rs.getLocalDateTime("created_at"),
			modifiedAt = rs.getLocalDateTime("modified_at")
		)
	}

	fun get(ansattId: UUID): AnsattDbo? {
		val parameters = MapSqlParameterSource().addValues(mapOf(
			"ansattId" to ansattId
		))

		return template.query(
			"SELECT * FROM arrangor_ansatt WHERE id = :ansattId",
			parameters,
			rowMapper
		).firstOrNull()
	}

	fun getByPersonligIdent(personligIdent: String): AnsattDbo? {
		val parameters = MapSqlParameterSource().addValues(mapOf(
			"personligIdent" to personligIdent
		))

		return template.query(
			"SELECT * FROM arrangor_ansatt WHERE personlig_ident = :personligIdent",
			parameters,
			rowMapper
		).firstOrNull()
	}

}

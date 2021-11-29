package no.nav.amt.tiltak.tiltaksleverandor.ansatt.repositories

import no.nav.amt.tiltak.tiltak.utils.getLocalDateTime
import no.nav.amt.tiltak.tiltak.utils.getUUID
import no.nav.amt.tiltak.tiltaksleverandor.ansatt.dbo.AnsattDbo
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
open class AnsattRepository(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		AnsattDbo(
			id = rs.getUUID("id"),
			personligIdent = rs.getString("personlig_ident"),
			fornavn = rs.getString("fornavn"),
			etternavn = rs.getString("etternavn"),
			telefonnummer = rs.getString("telefonnummer"),
			epost = rs.getString("epost"),
			createdAt = rs.getLocalDateTime("created_at"),
			modifiedAt = rs.getLocalDateTime("modified_at")
		)
	}

	fun getByPersonligIdent(personligIdent: String): AnsattDbo? {
		val parameters = MapSqlParameterSource().addValues(mapOf(
			"personligIdent" to personligIdent
		))

		return template.query(
			"SELECT * FROM tiltaksleverandor_ansatt WHERE personlig_ident = :personligIdent",
			parameters,
			rowMapper
		).firstOrNull()
	}

}

package no.nav.amt.tiltak.tiltak.deltaker.repositories

import no.nav.amt.tiltak.tiltak.deltaker.commands.UpsertNavAnsattCommand
import no.nav.amt.tiltak.tiltak.deltaker.dbo.NavAnsattDbo
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
open class NavAnsattRepository(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		NavAnsattDbo(
			id = UUID.fromString(rs.getString("id")),
			personligIdent = rs.getString("personlig_ident"),
			navn = rs.getString("navn"),
			telefonnummer = rs.getString("telefonnummer"),
			epost = rs.getString("epost")
		)
	}

	fun upsert(upsertCmd: UpsertNavAnsattCommand) {
		val sql = """
			INSERT INTO nav_ansatt(id, personlig_ident, navn, telefonnummer, epost)
			VALUES (:id,
					:personligIdent,
					:navn,
					:telefonnummer,
					:epost)
			ON CONFLICT (personlig_ident) DO UPDATE SET navn       	  = :navn,
														telefonnummer = :telefonnummer,
														epost         = :epost
		""".trimIndent()

		val parameterSource = MapSqlParameterSource().addValues(
			mapOf(
				"id" to UUID.randomUUID(),
				"personligIdent" to upsertCmd.personligIdent,
				"navn" to upsertCmd.navn,
				"telefonnummer" to upsertCmd.telefonnummer,
				"epost" to upsertCmd.epost
			)
		)

		template.update(sql, parameterSource)
	}

	fun getNavAnsattWithIdent(ident: String): NavAnsattDbo? {
		return template.query(
			"SELECT * FROM nav_ansatt WHERE personlig_ident = :personligIdent",
			MapSqlParameterSource().addValues(mapOf("personligIdent" to ident)),
			rowMapper
		).firstOrNull()
	}

}

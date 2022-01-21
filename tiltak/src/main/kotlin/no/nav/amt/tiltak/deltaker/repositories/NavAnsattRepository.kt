package no.nav.amt.tiltak.deltaker.repositories

import no.nav.amt.tiltak.deltaker.commands.UpsertNavAnsattCommand
import no.nav.amt.tiltak.deltaker.dbo.NavAnsattDbo
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
			navIdent = rs.getString("nav_ident"),
			navn = rs.getString("navn"),
			telefonnummer = rs.getString("telefonnummer"),
			epost = rs.getString("epost")
		)
	}

	fun upsert(upsertCmd: UpsertNavAnsattCommand) {
		val sql = """
			INSERT INTO nav_ansatt(id, nav_ident, navn, telefonnummer, epost)
			VALUES (:id,
					:navIdent,
					:navn,
					:telefonnummer,
					:epost)
			ON CONFLICT (nav_ident) DO UPDATE SET navn       	  = :navn,
														telefonnummer = :telefonnummer,
														epost         = :epost
		""".trimIndent()

		val parameterSource = MapSqlParameterSource().addValues(
			mapOf(
				"id" to UUID.randomUUID(),
				"navIdent" to upsertCmd.navIdent,
				"navn" to upsertCmd.navn,
				"telefonnummer" to upsertCmd.telefonnummer,
				"epost" to upsertCmd.epost
			)
		)

		template.update(sql, parameterSource)
	}

	fun getNavAnsattWithIdent(navIdent: String): NavAnsattDbo? {
		return template.query(
			"SELECT * FROM nav_ansatt WHERE nav_ident = :navIdent",
			MapSqlParameterSource().addValues(mapOf("navIdent" to navIdent)),
			rowMapper
		).firstOrNull()
	}

}

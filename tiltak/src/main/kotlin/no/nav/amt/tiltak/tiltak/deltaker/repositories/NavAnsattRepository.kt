package no.nav.amt.tiltak.tiltak.deltaker.repositories

import no.nav.amt.tiltak.tiltak.deltaker.dbo.NavAnsattDbo
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component

@Component
open class NavAnsattRepository(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		NavAnsattDbo(
			id = rs.getInt("id"),
			personligIdent = rs.getString("personlig_ident"),
			fornavn = rs.getString("fornavn"),
			etternavn = rs.getString("etternavn"),
			telefonnummer = rs.getString("telefonnummer"),
			epost = rs.getString("epost")
		)
	}

	fun upsert(navAnsattDbo: NavAnsattDbo) {
		val sql = """
			INSERT INTO nav_ansatt(personlig_ident, fornavn, etternavn, telefonnummer, epost)
			VALUES (:personligIdent,
					:fornavn,
					:etternavn,
					:telefonnummer,
					:epost)
			ON CONFLICT (personlig_ident) DO UPDATE SET fornavn       = :fornavn,
														etternavn     = :etternavn,
														telefonnummer = :telefonnummer,
														epost         = :epost
		""".trimIndent()

		template.update(sql, navAnsattDbo.asParameterSource())
	}

	fun getNavAnsattWithIdent(ident: String): NavAnsattDbo? {
		return template.query(
			"SELECT * FROM nav_ansatt WHERE personlig_ident = :personligIdent",
			MapSqlParameterSource().addValues(mapOf("personligIdent" to ident)),
			rowMapper
		).firstOrNull()
	}

	private fun NavAnsattDbo.asParameterSource() = MapSqlParameterSource().addValues(
		mapOf(
			"personligIdent" to personligIdent,
			"fornavn" to fornavn,
			"etternavn" to etternavn,
			"telefonnummer" to telefonnummer,
			"epost" to epost
		)
	)

}

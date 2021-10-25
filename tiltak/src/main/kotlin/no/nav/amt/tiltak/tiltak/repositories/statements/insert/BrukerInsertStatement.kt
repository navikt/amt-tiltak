package no.nav.amt.tiltak.tiltak.repositories.statements.insert

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class BrukerInsertStatement(
	private val template: NamedParameterJdbcTemplate,
	private val fodselsnummer: String,
	private val fornavn: String,
	private val etternavn: String,
	private val telefonnummer: String?,
	private val epost: String?,
	private val ansvarligVeilederId: Int?
) {

	//language=PostgreSQL
	private val sql = """
		INSERT INTO bruker(personlig_ident, fornavn, etternavn, telefonnummer, epost, ansvarlig_veileder_id)
		VALUES (:fodselsnummer,
				:fornanvn,
				:etternavn,
				:telefonnummer,
				:epost,
				:veileder_id)
	""".trimIndent()


	fun execute(): String {
		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"fodselsnummer" to fodselsnummer,
				"fornavn" to fornavn,
				"etternavn" to etternavn,
				"telefonnummer" to telefonnummer,
				"epost" to epost,
				"veileder_id" to ansvarligVeilederId
			)
		)

		template.update(sql, parameters)

		return fodselsnummer
	}

}

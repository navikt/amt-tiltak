package no.nav.amt.tiltak.tiltaksleverandor.repositories.statements.insert

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

class TiltaksleverandorInsertStatement(
	val template: NamedParameterJdbcTemplate,
	val organisasjonsnummer: String,
	val organisasjonsnavn: String,
	val virksomhetsnummer: String,
	val virksomhetsnavn: String
) {

	//language=PostgreSQL
	private val sql = """
		INSERT INTO tiltaksleverandor(external_id, organisasjonsnummer, organisasjonsnavn, virksomhetsnummer, virksomhetsnavn)
		VALUES (:externalId,
				:organisasjonsnummer,
				:organisasjonsnavn,
				:virksomhetsnummer,
				:virksomhetsnavn)
	""".trimIndent()

	fun execute(): UUID {
		val externalId = UUID.randomUUID()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"externalId" to externalId,
				"organisasjonsnummer" to organisasjonsnummer,
				"organisasjonsnavn" to organisasjonsnavn,
				"virksomhetsnummer" to virksomhetsnummer,
				"virksomhetsnavn" to virksomhetsnavn
			)
		)

		template.update(sql, parameters)

		return externalId
	}
}

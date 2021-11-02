package no.nav.amt.tiltak.tiltaksleverandor.repositories

import no.nav.amt.tiltak.tiltaksleverandor.dbo.TiltaksleverandorDbo
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.*

@Repository
open class TiltaksleverandorRepository(
	private val template: NamedParameterJdbcTemplate,
) {

	private val rowMapper = RowMapper { rs, _ ->
		TiltaksleverandorDbo(
			internalId = rs.getInt("id"),
			externalId = UUID.fromString(rs.getString("external_id")),
			organisasjonsnummer = rs.getString("organisasjonsnummer"),
			organisasjonsnavn = rs.getString("organisasjonsnavn"),
			virksomhetsnummer = rs.getString("virksomhetsnummer"),
			virksomhetsnavn = rs.getString("virksomhetsnavn"),
			createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
			modifiedAt = rs.getTimestamp("modified_at").toLocalDateTime()
		)
	}


	fun insert(
		organisasjonsnavn: String,
		organisasjonsnummer: String,
		virksomhetsnummer: String,
		virksomhetsnavn: String
	): TiltaksleverandorDbo {
		val savedTiltaksleverandor = getByVirksomhetsnummer(virksomhetsnummer)

		if (savedTiltaksleverandor != null) {
			return savedTiltaksleverandor
		}

		val sql = """
			INSERT INTO tiltaksleverandor(external_id, organisasjonsnummer, organisasjonsnavn, virksomhetsnummer, virksomhetsnavn)
			VALUES (:externalId,
					:organisasjonsnummer,
					:organisasjonsnavn,
					:virksomhetsnummer,
					:virksomhetsnavn)
		""".trimIndent()

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

		return getByVirksomhetsnummer(virksomhetsnummer)
			?: throw NoSuchElementException("Virksomhet med virksomhetsnummer $virksomhetsnummer finnes ikke")
	}

	fun getByVirksomhetsnummer(virksomhetsnummer: String): TiltaksleverandorDbo? {
		val sql = """
			SELECT id,
				   external_id,
				   organisasjonsnummer,
				   organisasjonsnavn,
				   virksomhetsnummer,
				   virksomhetsnavn,
				   created_at,
				   modified_at
			FROM tiltaksleverandor
			WHERE virksomhetsnummer = :virksomhetsnummer
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"virksomhetsnummer" to virksomhetsnummer
			)
		)

		return template.query(sql, parameters, rowMapper)
			.firstOrNull()
	}

}

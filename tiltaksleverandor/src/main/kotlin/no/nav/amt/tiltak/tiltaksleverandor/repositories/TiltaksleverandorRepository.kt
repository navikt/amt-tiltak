package no.nav.amt.tiltak.tiltaksleverandor.repositories

import no.nav.amt.tiltak.core.domain.tiltaksleverandor.Virksomhet
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
class TiltaksleverandorRepository(
	private val jdbcTemplate: JdbcTemplate
) {

	companion object Table {
		private const val TABLE_NAME = "tiltaksleverandor"

		private const val FIELD_ID = "id"
		private const val FIELD_EXTERNAL_ID = "external_id"
		private const val FIELD_ORGANISASJONSNUMMER = "organisasjonsnummer"
		private const val FIELD_ORGANISASJONSNAVN = "organisasjonsnavn"
		private const val FIELD_VIRKSOMHETSNUMMER = "virksomhetsnummer"
		private const val FIELD_VIRKSOMHETSNAVN = "virksomhetsnavn"
		private const val FIELD_CREATED_AT = "created_at"
		private const val FIELD_MODIFIED_AT = "modified_at"
	}

	private val rowMapper = RowMapper { rs, _ ->
		TiltaksleverandorDto(
			id = rs.getInt(FIELD_ID),
			externalId = UUID.fromString(rs.getString(FIELD_EXTERNAL_ID)),
			organisasjonsnummer = rs.getString(FIELD_ORGANISASJONSNUMMER),
			organisasjonsnavn = rs.getString(FIELD_ORGANISASJONSNAVN),
			virksomhetsnummer = rs.getString(FIELD_VIRKSOMHETSNUMMER),
			virksomhetsnavn = rs.getString(FIELD_VIRKSOMHETSNAVN),
			createdAt = rs.getTimestamp(FIELD_CREATED_AT).toLocalDateTime(),
			modifiedAt = rs.getTimestamp(FIELD_MODIFIED_AT).toLocalDateTime()
		)
	}

	fun insert(virksomhet: Virksomhet): TiltaksleverandorDto {
		val savedTiltaksleverandor = getByVirksomhetsnummer(virksomhet.virksomhetsnummer)

		if (savedTiltaksleverandor != null) {
			return savedTiltaksleverandor
		}

		val sql = """
			INSERT INTO $TABLE_NAME(
			$FIELD_EXTERNAL_ID, $FIELD_ORGANISASJONSNAVN, $FIELD_ORGANISASJONSNUMMER,
			$FIELD_VIRKSOMHETSNAVN, $FIELD_VIRKSOMHETSNUMMER
			)
			VALUES (?, ?, ?, ?, ?)
		""".trimIndent()

		jdbcTemplate.update(
			sql,
			UUID.randomUUID(),
			virksomhet.organisasjonsnavn,
			virksomhet.organisasjonsnummer,
			virksomhet.virksomhetsnavn,
			virksomhet.virksomhetsnummer
		)

		return getByVirksomhetsnummer(virksomhet.virksomhetsnummer)
			?: throw NoSuchElementException("Virksomhet med virksomhetsnummer ${virksomhet.virksomhetsnummer} finnes ikke")
	}

	fun getByVirksomhetsnummer(virksomhetsnummer: String): TiltaksleverandorDto? {
		val sql = """
			SELECT * FROM $TABLE_NAME WHERE $FIELD_VIRKSOMHETSNUMMER = ?
		""".trimIndent()

		return jdbcTemplate.query(
			sql,
			rowMapper,
			virksomhetsnummer
		).firstOrNull()
	}

}

data class TiltaksleverandorDto(
	val id: Int,
	val externalId: UUID,
	val organisasjonsnummer: String,
	val organisasjonsnavn: String,
	val virksomhetsnummer: String,
	val virksomhetsnavn: String,
	val createdAt: LocalDateTime,
	val modifiedAt: LocalDateTime
) {

	fun toVirksomhet(): Virksomhet {
		return Virksomhet(
			id = externalId,
			organisasjonsnummer = organisasjonsnummer,
			organisasjonsnavn = organisasjonsnavn,
			virksomhetsnummer = virksomhetsnummer,
			virksomhetsnavn = virksomhetsnavn
		)
	}
}

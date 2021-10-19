package no.nav.amt.tiltak.tiltak.repositories

import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
class TiltakRepository(
	private val jdbcTemplate: JdbcTemplate
) {

	companion object Table {
		private const val TABLE_NAME = "tiltak"

		private const val FIELD_ID = "id"
		private const val FIELD_EXTERNAL_ID = "external_id"
		private const val FIELD_TILTAKSLEVERANDOR_ID = "tiltaksleverandor_id"
		private const val FIELD_NAVN = "navn"
		private const val FIELD_TYPE = "type"
		private const val FIELD_CREATED_AT = "created_at"
		private const val FIELD_MODIFIED_AT = "modified_at"
	}

	private val rowMapper = RowMapper { rs, _ ->
		TiltakDto(
			id = rs.getInt(FIELD_ID),
			externalId = UUID.fromString(rs.getString(FIELD_EXTERNAL_ID)),
			tiltaksleverandorId = rs.getInt(FIELD_TILTAKSLEVERANDOR_ID),
			navn = rs.getString(FIELD_NAVN),
			type = rs.getString(FIELD_TYPE),
			createdAt = rs.getTimestamp(FIELD_CREATED_AT).toLocalDateTime(),
			modifiedAt = rs.getTimestamp(FIELD_MODIFIED_AT).toLocalDateTime()
		)
	}

	fun insert(tiltak: Tiltak): TiltakDto {
		val sql = """
			INSERT INTO $TABLE_NAME(
			$FIELD_EXTERNAL_ID, $FIELD_TILTAKSLEVERANDOR_ID,
			$FIELD_NAVN, $FIELD_TYPE
			)
			VALUES (
			?,
			(SELECT id FROM tiltaksleverandor WHERE external_id = ?),
			?,
			?)
		""".trimIndent()

		val externalId = UUID.randomUUID()

		jdbcTemplate.update(
			sql,
			externalId,
			tiltak.tiltaksleverandorId,
			tiltak.navn,
			tiltak.kode
		)

		return get(externalId)
			?: throw NoSuchElementException("Tiltak med id $externalId finnes ikke")
	}

	fun get(id: UUID): TiltakDto? {
		val sql = """
			SELECT * FROM $TABLE_NAME WHERE $FIELD_EXTERNAL_ID = ?
		""".trimIndent()

		return jdbcTemplate.query(
			sql,
			rowMapper,
			id
		).firstOrNull()
	}

	data class TiltakDto(
		val id: Int,
		val externalId: UUID,
		val tiltaksleverandorId: Int,
		val navn: String,
		val type: String,
		val createdAt: LocalDateTime,
		val modifiedAt: LocalDateTime
	) {

		fun toTiltak(tiltaksleverandorExternalId: UUID): Tiltak {
			return Tiltak(
				id = externalId,
				tiltaksleverandorId = tiltaksleverandorExternalId,
				navn = navn,
				kode = type
			)
		}
	}

}

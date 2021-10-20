package no.nav.amt.tiltak.tiltak.repositories

import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.RowMapper
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Repository
open class TiltaksinstansRepository(
	private val jdbcTemplate: JdbcTemplate
) {

	companion object Table {
		private const val TABLE_NAME = "tiltaksinstans"

		private const val FIELD_ID = "id"
		private const val FIELD_ARENA_ID = "arena_id"
		private const val FIELD_EXTERNAL_ID = "external_id"
		private const val FIELD_TILTAK_ID = "tiltak_id"
		private const val FIELD_NAVN = "navn"
		private const val FIELD_STATUS = "status"
		private const val FIELD_OPPSTART_DATO = "oppstart_dato"
		private const val FIELD_SLUTT_DATO = "slutt_dato"
		private const val FIELD_REGISTRERT_DATO = "registrert_dato"
		private const val FIELD_FREMMOTE_DATO = "fremmote_dato"

		private const val FIELD_CREATED_AT = "created_at"
		private const val FIELD_MODIFIED_AT = "modified_at"
	}

	private val rowMapper = RowMapper { rs, _ ->
		val statusString = rs.getString(FIELD_STATUS)

		TiltaksinstansDto(
			id = rs.getInt(FIELD_ID),
			externalId = UUID.fromString(rs.getString(FIELD_EXTERNAL_ID)),
			arenaId = rs.getInt(FIELD_ARENA_ID),
			tiltaksId = rs.getInt(FIELD_TILTAK_ID),
			navn = rs.getString(FIELD_NAVN),
			status = if (statusString != null) TiltakInstans.Status.valueOf(statusString) else null,
			oppstartsdato = rs.getTimestamp(FIELD_OPPSTART_DATO)?.toLocalDateTime()?.toLocalDate(),
			sluttdato = rs.getTimestamp(FIELD_SLUTT_DATO)?.toLocalDateTime()?.toLocalDate(),
			registrertDato = rs.getTimestamp(FIELD_REGISTRERT_DATO)?.toLocalDateTime(),
			fremmoteDato = rs.getTimestamp(FIELD_FREMMOTE_DATO)?.toLocalDateTime(),
			createdAt = rs.getTimestamp(FIELD_CREATED_AT).toLocalDateTime(),
			modifiedAt = rs.getTimestamp(FIELD_MODIFIED_AT).toLocalDateTime(),
		)
	}

	fun insert(arenaId: Int, instans: TiltakInstans): TiltaksinstansDto {
		val sql = """
			INSERT INTO $TABLE_NAME(
				$FIELD_ARENA_ID, $FIELD_EXTERNAL_ID, $FIELD_TILTAK_ID,
				$FIELD_NAVN, $FIELD_OPPSTART_DATO, $FIELD_SLUTT_DATO,
				$FIELD_REGISTRERT_DATO, $FIELD_FREMMOTE_DATO

			) VALUES(
				?,
				?,
				(SELECT id FROM tiltak t WHERE t.external_id = ?),
				?,
				?,
				?,
				?,
				?
			)
		""".trimIndent()

		val externalId = UUID.randomUUID()

		jdbcTemplate.update(
			sql,
			arenaId,
			externalId,
			instans.tiltakId,
			instans.navn,
			instans.oppstartDato,
			instans.sluttDato,
			instans.registrertDato,
			instans.fremmoteDato
		)

		return get(externalId)
			?: throw NoSuchElementException("Tiltak med id $externalId finnes ikke")
	}

	fun get(id: UUID): TiltaksinstansDto? {
		val sql = """
			SELECT * FROM $TABLE_NAME WHERE $FIELD_EXTERNAL_ID = ?
		""".trimIndent()

		return jdbcTemplate.query(
			sql,
			rowMapper,
			id
		).firstOrNull()
	}

	data class TiltaksinstansDto(
		val id: Int,
		val arenaId: Int,
		val externalId: UUID,
		val tiltaksId: Int,
		val navn: String,
		val status: TiltakInstans.Status?,

		val oppstartsdato: LocalDate?,
		val sluttdato: LocalDate?,
		val registrertDato: LocalDateTime?,
		val fremmoteDato: LocalDateTime?,

		val createdAt: LocalDateTime,
		val modifiedAt: LocalDateTime
	) {

		fun toTiltaksinstans(tiltaksId: UUID): TiltakInstans {
			return TiltakInstans(
				id = externalId,
				tiltakId = tiltaksId,
				navn = navn,
				status = status,
				oppstartDato = oppstartsdato,
				sluttDato = sluttdato,
				registrertDato = registrertDato,
				fremmoteDato = fremmoteDato
			)
		}

	}

}

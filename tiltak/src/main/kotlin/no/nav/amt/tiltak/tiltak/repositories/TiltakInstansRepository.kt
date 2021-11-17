package no.nav.amt.tiltak.tiltak.repositories
import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import no.nav.amt.tiltak.tiltak.dbo.TiltaksinstansDbo
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

// @Repository gir feilmelding (template == null), er det ikke dependency injection på @Repository?
@Component
open class TiltakInstansRepository(private val template: NamedParameterJdbcTemplate) {

	private val rowMapper = RowMapper { rs, _ ->
		val statusString = rs.getString("status")

		TiltaksinstansDbo(
			internalId = rs.getInt("tiltaksinstans_internal_id"),
			externalId = UUID.fromString(rs.getString("tiltaksinstans_external_id")),
			arenaId = rs.getInt("tiltaksinstans_arena_id"),
			tiltaksleverandorInternalId = rs.getInt("tiltaksleverandor_internal_id"),
			tiltaksleverandorExternalId = UUID.fromString(rs.getString("tiltaksleverandor_external_id")),
			tiltakInternalId = rs.getInt("tiltak_internal_id"),
			tiltakExternalId = UUID.fromString(rs.getString("tiltak_external_id")),
			navn = rs.getString("navn"),
			status = if (statusString != null) TiltakInstans.Status.valueOf(statusString) else null,
			oppstartDato = rs.getDate("oppstart_dato")?.toLocalDate(),
			sluttDato = rs.getDate("slutt_dato")?.toLocalDate(),
			registrertDato = rs.getTimestamp("registrert_dato")?.toLocalDateTime(),
			fremmoteDato = rs.getTimestamp("fremmote_dato")?.toLocalDateTime(),
			createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
			modifiedAt = rs.getTimestamp("modified_at").toLocalDateTime()
		)
	}

	fun insert(
		arenaId: Int,
		tiltakId: UUID,
		tiltaksleverandorId: UUID,
		navn: String,
		status: TiltakInstans.Status?,
		oppstartDato: LocalDate?,
		sluttDato: LocalDate?,
		registrertDato: LocalDateTime?,
		fremmoteDato: LocalDateTime?
	): TiltaksinstansDbo {

		//language=PostgreSQL
		val sql = """
		INSERT INTO tiltaksinstans(external_id, arena_id, tiltak_id, tiltaksleverandor_id, navn, status, oppstart_dato,
                           slutt_dato, registrert_dato, fremmote_dato)
		VALUES (:externalId,
				:arenaId,
				(SELECT id FROM tiltak where tiltak.external_id = :tiltakId),
				(SELECT id FROM tiltaksleverandor where tiltaksleverandor.external_id = :tiltaksleverandorId),
				:navn,
				:status,
				:oppstartDato,
				:sluttDato,
				:registrertDato,
				:fremmoteDato)
	""".trimIndent()

		val externalId = UUID.randomUUID()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"externalId" to externalId,
				"arenaId" to arenaId,
				"tiltakId" to tiltakId,
				"tiltaksleverandorId" to tiltaksleverandorId,
				"navn" to navn,
				"status" to status?.name,
				"oppstartDato" to oppstartDato,
				"sluttDato" to sluttDato,
				"registrertDato" to registrertDato,
				"fremmoteDato" to fremmoteDato
			)
		)

		template.update(sql, parameters)

		return get(externalId)
			?: throw NoSuchElementException("Tiltak med id $externalId finnes ikke")
	}

	fun update(tiltaksinstans: TiltaksinstansDbo): TiltaksinstansDbo {

		//language=PostgreSQL
		val sql = """
			UPDATE tiltaksinstans
			SET navn            = :navn,
				status          = :status,
				oppstart_dato   = :oppstart_dato,
				slutt_dato      = :slutt_dato,
				registrert_dato = :registrert_dato,
				fremmote_dato   = :fremmote_dato,
				modified_at     = :modified_at
			WHERE id = :id
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"navn" to tiltaksinstans.navn,
				"status" to tiltaksinstans.status?.name,
				"oppstart_dato" to tiltaksinstans.oppstartDato,
				"slutt_dato" to tiltaksinstans.sluttDato,
				"registrert_dato" to tiltaksinstans.registrertDato,
				"fremmote_dato" to tiltaksinstans.fremmoteDato,
				"modified_at" to tiltaksinstans.modifiedAt,
				"id" to tiltaksinstans.tiltakInternalId
			)
		)

		template.update(sql, parameters)

		return get(tiltaksinstans.externalId)
			?: throw NoSuchElementException("Tiltak med id ${tiltaksinstans.externalId} finnes ikke")
	}


	fun get(id: UUID): TiltaksinstansDbo? {

		//language=PostgreSQL
		val sql = """
			SELECT tiltaksinstans.id                                                           as tiltaksinstans_internal_id,
			       tiltaksinstans.external_id                                                  as tiltaksinstans_external_id,
			       tiltaksinstans.arena_id                                                     as tiltaksinstans_arena_id,
			       tiltaksinstans.tiltaksleverandor_id                                         as tiltaksleverandor_internal_id,
			       (SELECT external_id
			        FROM tiltaksleverandor
			        WHERE tiltaksleverandor.id = tiltaksinstans.tiltaksleverandor_id)          as tiltaksleverandor_external_id,
			       tiltaksinstans.tiltak_id                                                    as tiltak_internal_id,
			       (SELECT external_id FROM tiltak WHERE tiltak.id = tiltaksinstans.tiltak_id) as tiltak_external_id,
			       tiltaksinstans.navn                                                         as navn,
			       tiltaksinstans.status                                                       as status,
			       tiltaksinstans.oppstart_dato                                                as oppstart_dato,
			       tiltaksinstans.slutt_dato                                                   as slutt_dato,
			       tiltaksinstans.registrert_dato                                              as registrert_dato,
			       tiltaksinstans.fremmote_dato                                                as fremmote_dato,
				   tiltaksinstans.created_at 												   as created_at,
				   tiltaksinstans.modified_at                                                  as modified_at
			FROM tiltaksinstans
			WHERE tiltaksinstans.external_id = :externalId
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"externalId" to id
			)
		)

		return template.query(sql, parameters, rowMapper).firstOrNull()
	}

	fun getByArenaId(arenaId: Int): TiltaksinstansDbo? {

		//language=PostgreSQL
		val sql = """
			SELECT tiltaksinstans.id                                                           as tiltaksinstans_internal_id,
			       tiltaksinstans.external_id                                                  as tiltaksinstans_external_id,
			       tiltaksinstans.arena_id                                                     as tiltaksinstans_arena_id,
			       tiltaksinstans.tiltaksleverandor_id                                         as tiltaksleverandor_internal_id,
			       (SELECT external_id
			        FROM tiltaksleverandor
			        WHERE tiltaksleverandor.id = tiltaksinstans.tiltaksleverandor_id)          as tiltaksleverandor_external_id,
			       tiltaksinstans.tiltak_id                                                    as tiltak_internal_id,
			       (SELECT external_id FROM tiltak WHERE tiltak.id = tiltaksinstans.tiltak_id) as tiltak_external_id,
			       tiltaksinstans.navn                                                         as navn,
			       tiltaksinstans.status                                                       as status,
			       tiltaksinstans.oppstart_dato                                                as oppstart_dato,
			       tiltaksinstans.slutt_dato                                                   as slutt_dato,
			       tiltaksinstans.registrert_dato                                              as registrert_dato,
			       tiltaksinstans.fremmote_dato                                                as fremmote_dato,
				   tiltaksinstans.created_at 												   as created_at,
				   tiltaksinstans.modified_at                                                  as modified_at
			FROM tiltaksinstans
			WHERE tiltaksinstans.arena_id = :arenaId
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"arenaId" to arenaId
			)
		)

		return template.query(sql, parameters, rowMapper).firstOrNull()
	}
}

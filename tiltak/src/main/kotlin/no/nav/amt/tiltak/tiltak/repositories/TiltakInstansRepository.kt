package no.nav.amt.tiltak.tiltak.repositories

import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import no.nav.amt.tiltak.tiltak.dbo.TiltakInstansDbo
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Component
open class TiltakInstansRepository(private val template: NamedParameterJdbcTemplate) {

	private val rowMapper = RowMapper { rs, _ ->
		val statusString = rs.getString("status")

		TiltakInstansDbo(
			id = UUID.fromString(rs.getString("tiltaksinstans_id")),
			arenaId = rs.getInt("tiltaksinstans_arena_id"),
			arrangorId = UUID.fromString(rs.getString("arrangor_id")),
			tiltakId = UUID.fromString(rs.getString("tiltak_id")),
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
		arrangorId: UUID,
		navn: String,
		status: TiltakInstans.Status?,
		oppstartDato: LocalDate?,
		sluttDato: LocalDate?,
		registrertDato: LocalDateTime?,
		fremmoteDato: LocalDateTime?
	): TiltakInstansDbo {

		//language=PostgreSQL
		val sql = """
		INSERT INTO tiltaksinstans(id, arena_id, tiltak_id, arrangor_id, navn, status, oppstart_dato,
                           slutt_dato, registrert_dato, fremmote_dato)
		VALUES (:id,
				:arenaId,
				:tiltakId,
				:arrangorId,
				:navn,
				:status,
				:oppstartDato,
				:sluttDato,
				:registrertDato,
				:fremmoteDato)
	""".trimIndent()

		val id = UUID.randomUUID()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"id" to id,
				"arenaId" to arenaId,
				"tiltakId" to tiltakId,
				"arrangorId" to arrangorId,
				"navn" to navn,
				"status" to status?.name,
				"oppstartDato" to oppstartDato,
				"sluttDato" to sluttDato,
				"registrertDato" to registrertDato,
				"fremmoteDato" to fremmoteDato
			)
		)

		template.update(sql, parameters)

		return get(id)
			?: throw NoSuchElementException("Tiltak med id $id finnes ikke")
	}

	fun update(tiltaksinstans: TiltakInstansDbo): TiltakInstansDbo {

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
				"id" to tiltaksinstans.id
			)
		)

		template.update(sql, parameters)

		return get(tiltaksinstans.id)
			?: throw NoSuchElementException("Tiltak med id ${tiltaksinstans.id} finnes ikke")
	}


	fun get(id: UUID): TiltakInstansDbo? {

		//language=PostgreSQL
		val sql = """
			SELECT tiltaksinstans.id                                                           as tiltaksinstans_id,
			       tiltaksinstans.arena_id                                                     as tiltaksinstans_arena_id,
			       tiltaksinstans.arrangor_id                                       		   as arrangor_id,
			       tiltaksinstans.tiltak_id                                                    as tiltak_id,
			       tiltaksinstans.navn                                                         as navn,
			       tiltaksinstans.status                                                       as status,
			       tiltaksinstans.oppstart_dato                                                as oppstart_dato,
			       tiltaksinstans.slutt_dato                                                   as slutt_dato,
			       tiltaksinstans.registrert_dato                                              as registrert_dato,
			       tiltaksinstans.fremmote_dato                                                as fremmote_dato,
				   tiltaksinstans.created_at 												   as created_at,
				   tiltaksinstans.modified_at                                                  as modified_at
			FROM tiltaksinstans WHERE tiltaksinstans.id = :id
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"id" to id
			)
		)

		return template.query(sql, parameters, rowMapper).firstOrNull()
	}

	fun getByArenaId(arenaId: Int): TiltakInstansDbo? {

		//language=PostgreSQL
		val sql = """
			SELECT tiltaksinstans.id                                                           as tiltaksinstans_id,
			       tiltaksinstans.arena_id                                                     as tiltaksinstans_arena_id,
			       tiltaksinstans.arrangor_id                                         		   as arrangor_id,
			       tiltaksinstans.tiltak_id                                                    as tiltak_id,
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

	fun getByArrandorId(arrangorId: UUID): List<TiltakInstansDbo> {

		//language=PostgreSQL
		val sql = """
			SELECT tiltaksinstans.id                                                           as tiltaksinstans_id,
			       tiltaksinstans.arena_id                                                     as tiltaksinstans_arena_id,
			       tiltaksinstans.arrangor_id                                         		   as arrangor_id,
			       tiltaksinstans.tiltak_id                                                    as tiltak_id,
			       tiltaksinstans.navn                                                         as navn,
			       tiltaksinstans.status                                                       as status,
			       tiltaksinstans.oppstart_dato                                                as oppstart_dato,
			       tiltaksinstans.slutt_dato                                                   as slutt_dato,
			       tiltaksinstans.registrert_dato                                              as registrert_dato,
			       tiltaksinstans.fremmote_dato                                                as fremmote_dato,
				   tiltaksinstans.created_at 												   as created_at,
				   tiltaksinstans.modified_at                                                  as modified_at
			FROM tiltaksinstans
			WHERE tiltaksinstans.arrangor_id = :arrangorId
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"arrangorId" to arrangorId
			)
		)

		return template.query(sql, parameters, rowMapper)
	}

}

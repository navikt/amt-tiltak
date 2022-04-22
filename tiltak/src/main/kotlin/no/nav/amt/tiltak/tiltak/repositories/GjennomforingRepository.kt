package no.nav.amt.tiltak.tiltak.repositories

import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.tiltak.dbo.GjennomforingDbo
import no.nav.amt.tiltak.utils.getNullableUUID
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Component
open class GjennomforingRepository(private val template: NamedParameterJdbcTemplate) {

	private val rowMapper = RowMapper { rs, _ ->
		GjennomforingDbo(
			id = UUID.fromString(rs.getString("id")),
			arrangorId = UUID.fromString(rs.getString("arrangor_id")),
			tiltakId = UUID.fromString(rs.getString("tiltak_id")),
			navn = rs.getString("navn"),
			status = Gjennomforing.Status.valueOf(rs.getString("status")),
			startDato = rs.getDate("start_dato")?.toLocalDate(),
			sluttDato = rs.getDate("slutt_dato")?.toLocalDate(),
			registrertDato = rs.getTimestamp("registrert_dato").toLocalDateTime(),
			fremmoteDato = rs.getTimestamp("fremmote_dato")?.toLocalDateTime(),
			navKontorId = rs.getNullableUUID("nav_kontor_id"),
			createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
			modifiedAt = rs.getTimestamp("modified_at").toLocalDateTime()
		)
	}

	fun insert(
		id: UUID,
		tiltakId: UUID,
		arrangorId: UUID,
		navn: String,
		status: Gjennomforing.Status,
		startDato: LocalDate?,
		sluttDato: LocalDate?,
		registrertDato: LocalDateTime,
		fremmoteDato: LocalDateTime?
	): GjennomforingDbo {

		//language=PostgreSQL
		val sql = """
		INSERT INTO gjennomforing(id, tiltak_id, arrangor_id, navn, status, start_dato,
                           slutt_dato, registrert_dato, fremmote_dato)
		VALUES (:id,
				:tiltakId,
				:arrangorId,
				:navn,
				:status,
				:startDato,
				:sluttDato,
				:registrertDato,
				:fremmoteDato)
	""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"id" to id,
				"tiltakId" to tiltakId,
				"arrangorId" to arrangorId,
				"navn" to navn,
				"status" to status.name,
				"startDato" to startDato,
				"sluttDato" to sluttDato,
				"registrertDato" to registrertDato,
				"fremmoteDato" to fremmoteDato
			)
		)

		template.update(sql, parameters)

		return get(id)
			?: throw NoSuchElementException("Gjennomføring med id $id finnes ikke")
	}

	fun update(gjennomforing: GjennomforingDbo): GjennomforingDbo {
		//language=PostgreSQL
		val sql = """
			UPDATE gjennomforing
			SET navn            = :navn,
				status          = :status,
				start_dato      = :start_dato,
				slutt_dato      = :slutt_dato,
				registrert_dato = :registrert_dato,
				fremmote_dato   = :fremmote_dato,
				modified_at     = :modified_at
			WHERE id = :id
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"navn" to gjennomforing.navn,
				"status" to gjennomforing.status.name,
				"start_dato" to gjennomforing.startDato,
				"slutt_dato" to gjennomforing.sluttDato,
				"registrert_dato" to gjennomforing.registrertDato,
				"fremmote_dato" to gjennomforing.fremmoteDato,
				"modified_at" to gjennomforing.modifiedAt,
				"id" to gjennomforing.id
			)
		)

		template.update(sql, parameters)

		return get(gjennomforing.id)
			?: throw NoSuchElementException("Tiltak med id ${gjennomforing.id} finnes ikke")
	}


	fun get(id: UUID): GjennomforingDbo? {
		val sql = "SELECT * FROM gjennomforing WHERE id = :id"

		val parameters = MapSqlParameterSource().addValues(mapOf("id" to id))

		return template.query(sql, parameters, rowMapper).firstOrNull()
	}

	fun get(gjennomforingIder: List<UUID>): List<GjennomforingDbo> {
		if (gjennomforingIder.isEmpty()) return emptyList()

		val sql = "SELECT * FROM gjennomforing WHERE id in(:ids)"

		val parameters = MapSqlParameterSource().addValues(mapOf("ids" to gjennomforingIder))

		return template.query(sql, parameters, rowMapper)
	}

	fun delete(gjennomforingId: UUID) {
		val sql = "DELETE FROM gjennomforing WHERE id = :gjennomforingId"

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"gjennomforingId" to gjennomforingId
			)
		)

		template.update(sql, parameters)
	}

}

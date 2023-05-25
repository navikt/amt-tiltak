package no.nav.amt.tiltak.deltaker.repositories

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getNullableString
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusDbo
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusInsertDbo
import no.nav.amt.tiltak.utils.getLocalDateTime
import no.nav.amt.tiltak.utils.getUUID
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
open class DeltakerStatusRepository(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		DeltakerStatusDbo(
			id = rs.getUUID("id"),
			deltakerId = rs.getUUID("deltaker_id"),
			type = DeltakerStatus.Type.valueOf(rs.getString("status")),
			aarsak = rs.getNullableString("aarsak")?.let { DeltakerStatus.Aarsak.valueOf(it) },
			aktiv = rs.getBoolean("aktiv"),
			gyldigFra = rs.getLocalDateTime("gyldig_fra"),
			opprettetDato = rs.getTimestamp("created_at").toLocalDateTime(),
		)

	}

	fun deaktiver(id: UUID) {
		val sql = """
			UPDATE deltaker_status SET aktiv = false WHERE id = :id
		""".trimIndent()
		val params = sqlParameters("id" to id)
		template.update(sql, params)
	}

	fun insert(status: DeltakerStatusInsertDbo) {
		val sql = """
			INSERT INTO deltaker_status (id, deltaker_id, gyldig_fra, status, aarsak, aktiv)
			VALUES (
				:id, :deltaker_id, :gyldig_fra, :status, :aarsak, true
			)
		""".trimIndent()
		val params = sqlParameters(
			"id" to status.id,
			"deltaker_id" to status.deltakerId,
			"gyldig_fra" to status.gyldigFra,
			"status" to status.type.name,
			"aarsak" to status.aarsak?.name
		)

		template.update(sql, params)
	}

	fun getStatuserForDeltaker(deltakerId: UUID): List<DeltakerStatusDbo> {
		val sql = """
			SELECT id, deltaker_id, gyldig_fra, status, aarsak, aktiv, created_at
			FROM deltaker_status
			WHERE deltaker_id = :deltakerId;
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf("deltakerId" to deltakerId)
		)

		return template.query(sql, parameters, rowMapper)
	}

	fun getStatusForDeltaker(deltakerId: UUID): DeltakerStatusDbo? {
		val sql = """
			SELECT id, deltaker_id, gyldig_fra, status, aarsak, aktiv, created_at
			FROM deltaker_status
			WHERE deltaker_id = :deltakerId
			AND aktiv = true
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf("deltakerId" to deltakerId)
		)

		return template.query(sql, parameters, rowMapper).firstOrNull()
	}

	fun slett(deltakerId: UUID) {
		val sql = "DELETE FROM deltaker_status where deltaker_id = :deltakerId"

		val parameters = MapSqlParameterSource().addValues(
			mapOf("deltakerId" to deltakerId)
		)

		template.update(sql, parameters)
	}

	fun getAktiveStatuserForDeltakere(deltakerIder: List<UUID>): List<DeltakerStatusDbo> {
		if (deltakerIder.isEmpty()) return emptyList()

		val sql = """
			SELECT id, deltaker_id, gyldig_fra, status, aarsak, aktiv, created_at
			FROM deltaker_status
			WHERE deltaker_id IN (:deltakerIder)
			AND aktiv = true
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf("deltakerIder" to deltakerIder)
		)

		return template.query(sql, parameters, rowMapper)

	}

}

package no.nav.amt.tiltak.deltaker.repositories

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.deltaker.dbo.DeltakerStatusDbo
import no.nav.amt.tiltak.utils.getUUID
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Component
open class DeltakerStatusRepository(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->

		DeltakerStatusDbo(
			id = rs.getUUID("id"),
			deltakerId = rs.getUUID("deltaker_id"),
			endretDato = rs.getTimestamp("endret_dato").toLocalDateTime(),
			status = Deltaker.Status.valueOf(rs.getString("status")),
			aktiv = rs.getBoolean("aktiv"),
		)

	}

	@Transactional
	open fun upsert(deltakerStatuser: List<DeltakerStatusDbo>) = deltakerStatuser.forEach { upsert(it) }

	private fun upsert(dbo: DeltakerStatusDbo) {
		val sql = """
			INSERT INTO deltaker_status(id, deltaker_id, endret_dato, status, aktiv)
			VALUES (:id,
					:deltakerId,
					:endretDato,
					:status,
					:aktiv)
			ON CONFLICT (id) DO UPDATE SET endret_dato  = :endretDato,
										   status = :status,
										   aktiv  = :aktiv
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"id" to dbo.id,
				"deltakerId" to dbo.deltakerId,
				"endretDato" to dbo.endretDato,
				"status" to dbo.status.name,
				"aktiv" to dbo.aktiv,
			)
		)

		template.update(sql, parameters)
	}

	fun getStatuserForDeltaker(deltakerId: UUID): List<DeltakerStatusDbo> {
		val sql = """
			SELECT id, deltaker_id, endret_dato, status, aktiv
			FROM deltaker_status
			WHERE deltaker_id = :deltakerId;
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf("deltakerId" to deltakerId)
		)

		return template.query(sql, parameters, rowMapper)
	}
}

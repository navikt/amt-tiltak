package no.nav.amt.tiltak.endringsmelding

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getNullableZonedDateTime
import no.nav.amt.tiltak.utils.getNullableUUID
import no.nav.amt.tiltak.utils.getUUID
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.support.TransactionTemplate
import java.time.LocalDate
import java.util.*

@Component
open class EndringsmeldingRepository(
	private val template: NamedParameterJdbcTemplate,
	private val transactionTemplate: TransactionTemplate
) {
	private val rowMapper = RowMapper { rs, _ ->
		EndringsmeldingDbo(
			id = rs.getUUID("id"),
			deltakerId = rs.getUUID("deltaker_id"),
			startDato = rs.getDate("start_dato")?.toLocalDate(),
			sluttDato = rs.getDate("slutt_dato")?.toLocalDate(),
			ferdiggjortAvNavAnsattId = rs.getNullableUUID("ferdiggjort_av_nav_ansatt_id"),
			ferdiggjortTidspunkt = rs.getNullableZonedDateTime("ferdiggjort_tidspunkt"),
			aktiv = rs.getBoolean("aktiv"),
			opprettetAvArrangorAnsattId = rs.getUUID("opprettet_av_arrangor_ansatt_id"),
			createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
			modifiedAt = rs.getTimestamp("modified_at").toLocalDateTime()
		)
	}

	fun getByGjennomforing(gjennomforingId: UUID): List<EndringsmeldingDbo> {
		val sql = """
			SELECT *
			FROM endringsmelding
			JOIN deltaker on endringsmelding.deltaker_id = deltaker.id
			WHERE deltaker.gjennomforing_id = :gjennomforing_id
		""".trimIndent()

		val param = sqlParameters("gjennomforing_id" to gjennomforingId)

		return template.query(sql, param, rowMapper)
	}

	fun getByDeltaker(deltakerId: UUID): List<EndringsmeldingDbo> {
		val sql = """
			SELECT * FROM endringsmelding WHERE deltaker_id = :deltakerId
		""".trimIndent()

		val param = sqlParameters("deltakerId" to deltakerId)

		return template.query(sql, param, rowMapper)
	}

	fun getAktiv(deltakerId: UUID): EndringsmeldingDbo? {
		return getAktive(listOf(deltakerId)).firstOrNull()
	}

	fun getAktive(deltakerIder: List<UUID>): List<EndringsmeldingDbo> {
		if (deltakerIder.isEmpty())
			return emptyList()

		val sql = """
			SELECT * FROM endringsmelding
			WHERE aktiv = true and deltaker_id in(:deltakerIder)
		""".trimIndent()

		val parameters = sqlParameters("deltakerIder" to deltakerIder)

		return template.query(sql, parameters, rowMapper)
	}

	fun get(id: UUID): EndringsmeldingDbo {
		val sql = """
			SELECT *
			FROM endringsmelding
			WHERE id = :id
		""".trimIndent()

		val params = sqlParameters("id" to id)

		return template.query(sql, params, rowMapper).firstOrNull()
			?: throw NoSuchElementException("Fant ingen endringsmelding med id=$id")
	}

	fun markerSomFerdig(endringsmeldingId: UUID, navAnsattId: UUID) {
		val sql = """
			UPDATE endringsmelding
				SET aktiv = false,
					ferdiggjort_tidspunkt = current_timestamp,
					ferdiggjort_av_nav_ansatt_id = :navAnsattId
				WHERE id = :endringsmeldingId
		""".trimIndent()

		val params = sqlParameters(
			"endringsmeldingId" to endringsmeldingId,
			"navAnsattId" to navAnsattId,
		)

		template.update(sql, params)
	}

	open fun insertOgInaktiverStartDato(startDato: LocalDate, deltakerId: UUID, opprettetAv: UUID): EndringsmeldingDbo {
		return transactionTemplate.execute {
			inaktiverMeldingerMedStartDato(deltakerId)
			return@execute insertNyDato(startDato, sluttDato = null, deltakerId, opprettetAv)
		}!!
	}

	open fun insertOgInaktiverSluttDato(sluttDato: LocalDate, deltakerId: UUID, opprettetAv: UUID): EndringsmeldingDbo {
		return transactionTemplate.execute {
			inaktiverMeldingerMedSluttDato(deltakerId)
			return@execute insertNyDato(startDato = null, sluttDato,  deltakerId, opprettetAv)
		}!!
	}

	private fun inaktiverMeldingerMedStartDato(deltakerId: UUID): Int {
		return inaktiverMeldingerMedDato(deltakerId, "start_dato")
	}

	private fun inaktiverMeldingerMedSluttDato(deltakerId: UUID): Int {
		return inaktiverMeldingerMedDato(deltakerId, "slutt_dato")
	}

	private fun inaktiverMeldingerMedDato(deltakerId: UUID, datoColumn: String): Int {
		val sql = """
				UPDATE endringsmelding
				SET aktiv = false
				WHERE deltaker_id = :deltaker_id AND $datoColumn IS NOT NULL
			""".trimIndent()

		val params = sqlParameters("deltaker_id" to deltakerId)

		return template.update(sql, params)
	}


	private fun insertNyDato(startDato: LocalDate?, sluttDato: LocalDate?, deltakerId: UUID, opprettetAvArrangorAnsattId: UUID): EndringsmeldingDbo {
		val id = UUID.randomUUID()

		val sql = """
			INSERT INTO endringsmelding(id, deltaker_id, start_dato, slutt_dato, aktiv, opprettet_av_arrangor_ansatt_id)
			VALUES (:id, :deltaker_id, :start_dato, :slutt_dato, true, :opprettet_av)
		""".trimIndent()

		val params = sqlParameters(
			"id" to id,
			"deltaker_id" to deltakerId,
			"start_dato" to startDato,
			"slutt_dato" to sluttDato,
			"opprettet_av" to opprettetAvArrangorAnsattId
		)

		template.update(sql, params)

		return get(id)
	}
}

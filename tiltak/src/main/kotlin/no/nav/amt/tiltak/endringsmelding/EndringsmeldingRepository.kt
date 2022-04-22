package no.nav.amt.tiltak.endringsmelding

import no.nav.amt.tiltak.utils.getNullableUUID
import no.nav.amt.tiltak.utils.getUUID
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

@Component
@EnableTransactionManagement
open class EndringsmeldingRepository(
	private val template: NamedParameterJdbcTemplate
) {
	private val rowMapper = RowMapper { rs, _ ->
		EndringsmeldingDbo(
			id = rs.getUUID("id"),
			deltakerId = rs.getUUID("deltaker_id"),
			startDato = rs.getDate("start_dato")?.toLocalDate(),
			godkjentAvNavAnsatt = rs.getNullableUUID("godkjent_av_nav_ansatt"),
			aktiv = rs.getBoolean("aktiv"),
			opprettetAvId = rs.getUUID("opprettet_av"),
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

		val param = MapSqlParameterSource().addValue("gjennomforing_id", gjennomforingId)
		return template.query(sql, param, rowMapper)
	}

	fun get(id: UUID): EndringsmeldingDbo? {
		val sql = """
			SELECT *
			FROM endringsmelding
			WHERE id=:id
		""".trimIndent()

		val params = MapSqlParameterSource().addValue("id", id)

		val res = template.query(sql, params, rowMapper)
		return res.firstOrNull()
	}

	@Transactional
	open fun insertOgInaktiverStartDato(startDato: LocalDate, deltakerId: UUID, opprettetAv: UUID): EndringsmeldingDbo {
		inaktiverTidligereMeldinger(deltakerId)
		return insertNyStartDato(startDato, deltakerId, opprettetAv)
	}

	private fun inaktiverTidligereMeldinger(deltakerId: UUID): Int {
		val sql = """
			UPDATE endringsmelding
			SET aktiv=false
			WHERE deltaker_id = :deltaker_id
		""".trimIndent()

		val params = MapSqlParameterSource().addValue("deltaker_id", deltakerId)

		return template.update(sql, params)
	}

	private fun insertNyStartDato(startDato: LocalDate, deltakerId: UUID, opprettetAv: UUID): EndringsmeldingDbo {
		val id = UUID.randomUUID()
		// language=sql
		val sql = """
			INSERT INTO endringsmelding(id, deltaker_id, start_dato, aktiv, opprettet_av)
			VALUES (:id, :deltaker_id, :start_dato, true, :opprettet_av)
		""".trimIndent()
		val params = MapSqlParameterSource().addValues(
			mapOf(
				"id" to id,
				"deltaker_id" to deltakerId,
				"start_dato" to startDato,
				"opprettet_av" to opprettetAv
			)
		)

		template.update(sql, params)
		return get(id)?: throw Error("Fant ikke aktiv endringsmelding på deltaker: ${deltakerId} etter insert ")
	}
}

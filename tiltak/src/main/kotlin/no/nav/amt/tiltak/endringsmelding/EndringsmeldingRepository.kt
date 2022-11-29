package no.nav.amt.tiltak.endringsmelding

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getNullableZonedDateTime
import no.nav.amt.tiltak.common.db_utils.getZonedDateTime
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.utils.getNullableUUID
import no.nav.amt.tiltak.utils.getUUID
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
open class EndringsmeldingRepository(
	private val template: NamedParameterJdbcTemplate,
	private val objectMapper: ObjectMapper,
) {
	private val rowMapper = RowMapper { rs, _ ->
		val type = EndringsmeldingDbo.Type.valueOf(rs.getString("type"))
		EndringsmeldingDbo(
			id = rs.getUUID("id"),
			deltakerId = rs.getUUID("deltaker_id"),
			utfortAvNavAnsattId = rs.getNullableUUID("utfort_av_nav_ansatt_id"),
			utfortTidspunkt = rs.getNullableZonedDateTime("utfort_tidspunkt"),
			opprettetAvArrangorAnsattId = rs.getUUID("opprettet_av_arrangor_ansatt_id"),
			status = Endringsmelding.Status.valueOf(rs.getString("status")),
			type = type,
			innhold = parseInnholdJson(rs.getString("innhold"), type),
			createdAt = rs.getZonedDateTime("created_at"),
			modifiedAt = rs.getZonedDateTime("modified_at"),
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

	fun getAktive(deltakerIder: List<UUID>): List<EndringsmeldingDbo> {
		if (deltakerIder.isEmpty())
			return emptyList()

		val sql = """
			SELECT * FROM endringsmelding
			WHERE status = 'AKTIV' and deltaker_id in(:deltakerIder)
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

	fun markerSomTilbakekalt(id: UUID) {
		val sql = """
			UPDATE endringsmelding
				SET status = 'TILBAKEKALT'
				WHERE id = :id AND status = 'AKTIV'
		""".trimIndent()

		val params = sqlParameters(
			"id" to id
		)
		template.update(sql, params)
	}

	fun markerSomUtfort(endringsmeldingId: UUID, navAnsattId: UUID) {
		val sql = """
			UPDATE endringsmelding
				SET status = 'UTFORT',
					utfort_tidspunkt = current_timestamp,
					utfort_av_nav_ansatt_id = :navAnsattId
				WHERE id = :endringsmeldingId
		""".trimIndent()

		val params = sqlParameters(
			"endringsmeldingId" to endringsmeldingId,
			"navAnsattId" to navAnsattId,
		)

		template.update(sql, params)
	}

	fun markerSomUtdatert(deltakerId: UUID, type: EndringsmeldingDbo.Type) {
		val sql = """
			UPDATE endringsmelding
				SET status = 'UTDATERT'
				WHERE deltaker_id = :deltakerId AND type = :type
		""".trimIndent()

		val params = sqlParameters(
			"deltakerId" to deltakerId,
			"type" to type.name,
		)
		template.update(sql, params)
	}


	fun insert(id: UUID, deltakerId: UUID, opprettetAvArrangorAnsattId: UUID, innhold: EndringsmeldingDbo.Innhold) {
		val sql = """
			INSERT INTO endringsmelding(id, deltaker_id, opprettet_av_arrangor_ansatt_id, type, innhold, status)
			VALUES(:id, :deltakerId, :opprettetAvArrangorAnsattId, :type, CAST(:innhold as jsonb), 'AKTIV')
		""".trimIndent()

		val params = sqlParameters(
			"id" to id,
			"deltakerId" to deltakerId,
			"opprettetAvArrangorAnsattId" to opprettetAvArrangorAnsattId,
			"type" to innhold.type().name,
			"innhold" to objectMapper.writeValueAsString(innhold),
		)
		template.update(sql, params)
	}

	private fun parseInnholdJson(innholdJson: String, type: EndringsmeldingDbo.Type): EndringsmeldingDbo.Innhold {
		return when(type) {
			EndringsmeldingDbo.Type.LEGG_TIL_OPPSTARTSDATO ->
				objectMapper.readValue<EndringsmeldingDbo.Innhold.LeggTilOppstartsdatoInnhold>(innholdJson)
			EndringsmeldingDbo.Type.ENDRE_OPPSTARTSDATO ->
				objectMapper.readValue<EndringsmeldingDbo.Innhold.EndreOppstartsdatoInnhold>(innholdJson)
			EndringsmeldingDbo.Type.FORLENG_DELTAKELSE ->
				objectMapper.readValue<EndringsmeldingDbo.Innhold.ForlengDeltakelseInnhold>(innholdJson)
			EndringsmeldingDbo.Type.AVSLUTT_DELTAKELSE ->
				objectMapper.readValue<EndringsmeldingDbo.Innhold.AvsluttDeltakelseInnhold>(innholdJson)
			EndringsmeldingDbo.Type.DELTAKER_IKKE_AKTUELL ->
				objectMapper.readValue<EndringsmeldingDbo.Innhold.DeltakerIkkeAktuellInnhold>(innholdJson)
		}

	}
}

package no.nav.amt.tiltak.data_publisher.publish

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getLocalDateTime
import no.nav.amt.tiltak.common.db_utils.getNullableLocalDateTime
import no.nav.amt.tiltak.common.db_utils.getNullableUUID
import no.nav.amt.tiltak.common.db_utils.getUUID
import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.data_publisher.model.EndringsmeldingPublishDto
import no.nav.amt.tiltak.data_publisher.model.Innhold
import no.nav.amt.tiltak.data_publisher.model.Type
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

class EndringsmeldingPublishQuery(
	private val template: NamedParameterJdbcTemplate
) {

	private val objectMapper = JsonUtils.objectMapper

	private val rowMapper = RowMapper { rs, _ ->
		val type = Type.valueOf(rs.getString("type"))

		EndringsmeldingPublishDto(
			id = rs.getUUID("id"),
			deltakerId = rs.getUUID("deltaker_id"),
			utfortAvNavAnsattId = rs.getNullableUUID("utfort_av_nav_ansatt_id"),
			opprettetAvArrangorAnsattId = rs.getUUID("opprettet_av_arrangor_ansatt_id"),
			utfortTidspunkt = rs.getNullableLocalDateTime("utfort_tidspunkt"),
			status = rs.getString("status"),
			type = type,
			innhold = rs.getString("innhold")?.let { parseInnholdJson(it, type) },
			createdAt = rs.getLocalDateTime("created_at")
		)
	}

	fun get(id: UUID): EndringsmeldingPublishDto {
		return template.query(
			"SELECT * FROM endringsmelding WHERE id = :id",
			sqlParameters("id" to id),
			rowMapper
		).first()
	}

	private fun parseInnholdJson(innholdJson: String, type: Type): Innhold? {
		return when (type) {
			Type.LEGG_TIL_OPPSTARTSDATO ->
				objectMapper.readValue<Innhold.LeggTilOppstartsdatoInnhold>(innholdJson)

			Type.ENDRE_OPPSTARTSDATO ->
				objectMapper.readValue<Innhold.EndreOppstartsdatoInnhold>(innholdJson)

			Type.FORLENG_DELTAKELSE ->
				objectMapper.readValue<Innhold.ForlengDeltakelseInnhold>(innholdJson)

			Type.AVSLUTT_DELTAKELSE ->
				objectMapper.readValue<Innhold.AvsluttDeltakelseInnhold>(innholdJson)

			Type.DELTAKER_IKKE_AKTUELL ->
				objectMapper.readValue<Innhold.DeltakerIkkeAktuellInnhold>(innholdJson)

			Type.ENDRE_DELTAKELSE_PROSENT ->
				objectMapper.readValue<Innhold.EndreDeltakelseProsentInnhold>(innholdJson)
			Type.ENDRE_SLUTTDATO ->	objectMapper.readValue<Innhold.EndreSluttdatoInnhold>(innholdJson)
			Type.DELTAKER_ER_AKTUELL -> null



		}

	}

}

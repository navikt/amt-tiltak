package no.nav.amt.tiltak.tilgangskontroll.tilgang

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getNullableUUID
import no.nav.amt.tiltak.common.db_utils.getNullableZonedDateTime
import no.nav.amt.tiltak.common.db_utils.getUUID
import no.nav.amt.tiltak.common.db_utils.getZonedDateTime
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.util.*

@Component
open class GjennomforingTilgangRepository(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		GjennomforingTilgangDbo(
			id = rs.getUUID("id"),
			ansattId = rs.getUUID("ansatt_id"),
			gjennomforingId = rs.getUUID("gjennomforing_id"),
			opprettetAvNavAnsattId = rs.getNullableUUID("opprettet_av_nav_ansatt_id"),
			stoppetAvNavAnsattId = rs.getNullableUUID("stoppet_av_nav_ansatt_id"),
			stoppetTidspunkt = rs.getNullableZonedDateTime("stoppet_tidspunkt"),
			createdAt = rs.getZonedDateTime("created_at"),
		)
	}

	internal fun get(id: UUID): GjennomforingTilgangDbo {
		val sql = """
			SELECT * FROM arrangor_ansatt_gjennomforing_tilgang WHERE id = :id
		""".trimIndent()

		val parameters = sqlParameters("id" to id,)

		return template.query(sql, parameters, rowMapper).firstOrNull()
			?: throw NoSuchElementException("Fant ikke arrangor_ansatt_gjennomforing_tilgang med id $id")
	}

	internal fun opprettTilgang(id: UUID, arrangorAnsattId: UUID, opprettetAvNavAnsattId: UUID, gjennomforingId: UUID) {
		val sql = """
			INSERT INTO arrangor_ansatt_gjennomforing_tilgang(id, ansatt_id, opprettet_av_nav_ansatt_id, gjennomforing_id)
				VALUES(:id, :ansattId, :opprettetAvNavAnsattId, :gjennomforingId)
		""".trimIndent()

		val parameters = sqlParameters(
			"id" to id,
			"ansattId" to arrangorAnsattId,
			"opprettetAvNavAnsattId" to opprettetAvNavAnsattId,
			"gjennomforingId" to gjennomforingId
		)

		template.update(sql, parameters)
	}

	internal fun stopTilgang(id: UUID, stoppetAvNavAnsattId: UUID, stopTidspunkt: ZonedDateTime) {
		val sql = """
			update arrangor_ansatt_gjennomforing_tilgang
				set stoppet_av_nav_ansatt_id = :stoppetAvNavAnsattId,
				 	stoppet_tidspunkt = :stopTidspunkt
				where id = :id
		""".trimIndent()

		val parameters = sqlParameters(
			"id" to id,
			"stoppetAvNavAnsattId" to stoppetAvNavAnsattId,
			"stopTidspunkt" to stopTidspunkt.toOffsetDateTime()
		)

		template.update(sql, parameters)
	}

	internal fun hentAktiveGjennomforingTilgangerForAnsatt(ansattId: UUID): List<GjennomforingTilgangDbo> {
		val sql = """
			SELECT * FROM arrangor_ansatt_gjennomforing_tilgang WHERE ansatt_id = :ansattId AND stoppet_tidspunkt is null
		""".trimIndent()

		val parameters = sqlParameters("ansattId" to ansattId)

		return template.query(sql, parameters, rowMapper)
	}

}

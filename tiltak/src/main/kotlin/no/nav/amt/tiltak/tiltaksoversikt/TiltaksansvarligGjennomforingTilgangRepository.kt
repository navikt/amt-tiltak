package no.nav.amt.tiltak.tiltaksoversikt

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getUUID
import no.nav.amt.tiltak.common.db_utils.getZonedDateTime
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.util.*

@Component
class TiltaksansvarligGjennomforingTilgangRepository(
	private val template: NamedParameterJdbcTemplate
) {
	private val DEFAULT_GYLDIG_TIL = ZonedDateTime.parse("3000-01-01T00:00:00.00000+00:00")

	private val rowMapper = RowMapper { rs, _ ->
		TiltaksansvarligGjennomforingTilgangDbo(
			id = rs.getUUID("id"),
			navAnsattId = rs.getUUID("nav_ansatt_id"),
			gjennomforingId = rs.getUUID("gjennomforing_id"),
			gyldigTil = rs.getZonedDateTime("gyldig_til"),
			createdAt = rs.getZonedDateTime("created_at"),
		)
	}

	fun hentAktiveTilgangerTilTiltaksansvarlig(navAnsattId: UUID): List<TiltaksansvarligGjennomforingTilgangDbo> {
		val sql = """
			SELECT * FROM tiltaksansavarlig_gjennomforing_tilgang
				WHERE nav_ansatt_id = :navAnsattId AND gyldig_til > current_timestamp
		""".trimIndent()

		val parameters = sqlParameters("navAnsattId" to navAnsattId)

		return template.query(sql, parameters, rowMapper)
	}

	fun opprettTilgang(id: UUID, navAnsattId: UUID, gjennomforingId: UUID) {
		val sql = """
			INSERT INTO tiltaksansavarlig_gjennomforing_tilgang(id, nav_ansatt_id, gjennomforing_id, gyldig_til)
			 VALUES(:id, :navAnsattId, :gjennomforingId, :gyldigTil)
		""".trimIndent()

		val parameters = sqlParameters(
			"id" to id,
			"navAnsattId" to navAnsattId,
			"gjennomforingId" to gjennomforingId,
			"gyldigTil" to DEFAULT_GYLDIG_TIL.toOffsetDateTime(),
		)

		template.update(sql, parameters)
	}

	fun stopTilgang(id: UUID) {
		val sql = """
			UPDATE tiltaksansavarlig_gjennomforing_tilgang SET gyldig_til = current_timestamp WHERE id = :id
		""".trimIndent()

		val parameters = sqlParameters("id" to id)

		template.update(sql, parameters)
	}

}

package no.nav.amt.tiltak.tilgangskontroll.tiltaksansvarlig_tilgang

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

	private val rowMapper = RowMapper { rs, _ ->
		TiltaksansvarligGjennomforingTilgangDbo(
			id = rs.getUUID("id"),
			navAnsattId = rs.getUUID("nav_ansatt_id"),
			gjennomforingId = rs.getUUID("gjennomforing_id"),
			gyldigTil = rs.getZonedDateTime("gyldig_til"),
			createdAt = rs.getZonedDateTime("created_at"),
		)
	}

	fun hentTilgang(id:  UUID): TiltaksansvarligGjennomforingTilgangDbo {
		val sql = """
			SELECT * FROM tiltaksansavarlig_gjennomforing_tilgang WHERE id = :id
		""".trimIndent()

		val parameters = sqlParameters("id" to id)

		return template.query(sql, parameters, rowMapper).firstOrNull()
			?: throw NoSuchElementException("Fant ikke tilgang med id=$id")
	}

	fun hentAktiveTilganger(navAnsattId: UUID): List<TiltaksansvarligGjennomforingTilgangDbo> {
		val sql = """
			SELECT * FROM tiltaksansavarlig_gjennomforing_tilgang
				WHERE nav_ansatt_id = :navAnsattId AND gyldig_til > current_timestamp
		""".trimIndent()

		val parameters = sqlParameters("navAnsattId" to navAnsattId)

		return template.query(sql, parameters, rowMapper)
	}

	fun opprettTilgang(id: UUID, navAnsattId: UUID, gjennomforingId: UUID, gyldigTil: ZonedDateTime) {
		val sql = """
			INSERT INTO tiltaksansavarlig_gjennomforing_tilgang(id, nav_ansatt_id, gjennomforing_id, gyldig_til)
			 VALUES(:id, :navAnsattId, :gjennomforingId, :gyldigTil)
		""".trimIndent()

		val parameters = sqlParameters(
			"id" to id,
			"navAnsattId" to navAnsattId,
			"gjennomforingId" to gjennomforingId,
			"gyldigTil" to gyldigTil.toOffsetDateTime(),
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

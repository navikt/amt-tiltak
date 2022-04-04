package no.nav.amt.tiltak.tilgangskontroll.invitasjon

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getNullableUUID
import no.nav.amt.tiltak.common.db_utils.getNullableZonedDateTime
import no.nav.amt.tiltak.common.db_utils.getUUID
import no.nav.amt.tiltak.common.db_utils.getZonedDateTime
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime
import java.util.*

@Repository
class TilgangInvitasjonRepository(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		TilgangInvitasjonDbo(
			id = rs.getUUID("id"),
			gjennomforingId = rs.getUUID("gjennomforing_id"),
			gyldigTil = rs.getZonedDateTime("gyldig_til"),
			opprettetAvNavAnsattId = rs.getUUID("opprettet_av_nav_ansatt_id"),
			erBrukt = rs.getBoolean("er_brukt"),
			tidspunktBrukt = rs.getNullableZonedDateTime("tidspunkt_brukt"),
			tilgangForesporselId = rs.getNullableUUID("tilgang_foresporsel_id"),
			createdAt = rs.getZonedDateTime("created_at"),
		)
	}

	internal fun get(id: UUID): TilgangInvitasjonDbo {
		val sql = """
			SELECT * FROM gjennomforing_tilgang_invitasjon WHERE id = :id
		""".trimIndent()

		val parameters = sqlParameters("id" to id)

		return template.query(sql, parameters, rowMapper).firstOrNull()
			?: throw NoSuchElementException("Fant ikke tilgang invitasjon med id: $id")
	}

	internal fun opprettInvitasjon(id: UUID, gjennomforingId: UUID, opprettetAvNavAnsattId: UUID, gyldigTil: ZonedDateTime) {
		val sql = """
			INSERT INTO gjennomforing_tilgang_invitasjon(id, gjennomforing_id, gyldig_til, opprettet_av_nav_ansatt_id)
			 	VALUES(:id, :gjennomforingId, :gyldigTil, :opprettetAvNavAnsattId)
		""".trimIndent()

		val parameters = sqlParameters(
			"id" to id,
			"gjennomforingId" to gjennomforingId,
			"gyldigTil" to gyldigTil.toOffsetDateTime(),
			"opprettetAvNavAnsattId" to opprettetAvNavAnsattId
		)

		template.update(sql, parameters)
	}

	internal fun settTilBrukt(invitasjonId: UUID, tilhorendeTilgangForesporselId: UUID) {
		val sql = """
			UPDATE gjennomforing_tilgang_invitasjon
				SET er_brukt = true, tidspunkt_brukt = current_timestamp, tilgang_foresporsel_id = :tilgangForesporselId
				WHERE id = :invitasjonId
		""".trimIndent()

		val parameters = sqlParameters(
			"invitasjonId" to invitasjonId,
			"tilgangForesporselId" to tilhorendeTilgangForesporselId
		)

		template.update(sql, parameters)
	}

	internal fun slettInvitasjon(invitasjonId: UUID) {
		val sql = """
			DELETE FROM gjennomforing_tilgang_invitasjon WHERE id = :invitasjonId
		""".trimIndent()

		val parameters = sqlParameters(
			"invitasjonId" to invitasjonId
		)

		template.update(sql, parameters)
	}

}

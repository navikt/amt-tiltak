package no.nav.amt.tiltak.tilgangskontroll.foresporsel

import no.nav.amt.tiltak.common.db_utils.*
import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
open class TilgangForesporselRepository(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		TilgangForesporselDbo(
			id = rs.getUUID("id"),
			personligIdent = rs.getString("personlig_ident"),
			fornavn = rs.getString("fornavn"),
			mellomnavn = rs.getNullableString("mellomnavn"),
			etternavn = rs.getString("etternavn"),
			gjennomforingId = rs.getUUID("gjennomforing_id"),
			beslutningAvNavAnsattId = rs.getNullableUUID("beslutning_av_nav_ansatt_id"),
			tidspunktBeslutning = rs.getNullableZonedDateTime("tidspunkt_beslutning"),
			beslutning = rs.getString("beslutning")?.let { Beslutning.valueOf(it) },
			gjennomforingTilgangId = rs.getNullableUUID("gjennomforing_tilgang_id"),
			createdAt = rs.getZonedDateTime("created_at"),
		)
	}

	internal fun opprettForesporsel(input: OpprettForesporselInput) {
		val sql = """
			INSERT INTO gjennomforing_tilgang_foresporsel(id, personlig_ident, fornavn, mellomnavn, etternavn, gjennomforing_id)
				VALUES(:id, :personligIdent, :fornavn, :mellomnavn, :etternavn, :gjennomforingId)
		""".trimIndent()

		val parameters = sqlParameters(
			"id" to input.id,
			"personligIdent" to input.personligIdent,
			"fornavn" to input.fornavn,
			"mellomnavn" to input.mellomnavn,
			"etternavn" to input.etternavn,
			"gjennomforingId" to input.gjennomforingId,
		)

		template.update(sql, parameters)
	}

	internal fun hentForesporsel(foresporselId: UUID): TilgangForesporselDbo {
		val sql = """
			select * from gjennomforing_tilgang_foresporsel where id = :foresporselId
		""".trimIndent()

		return template.query(
			sql,
			sqlParameters("foresporselId" to foresporselId),
			rowMapper
		).firstOrNull() ?: throw NoSuchElementException("Fant ikke tilgang forespørsel med id: $foresporselId")
	}

	internal fun hentUbesluttedeForesporsler(gjennomforingId: UUID): List<TilgangForesporselDbo> {
		val sql = """
			select * from gjennomforing_tilgang_foresporsel where gjennomforing_id = :gjennomforingId and beslutning is null
		""".trimIndent()

		return template.query(
			sql,
			sqlParameters("gjennomforingId" to gjennomforingId),
			rowMapper
		)
	}

	internal fun godkjennForesporsel(foresporselId: UUID, beslutningAvNavAnsattId: UUID, gjennomforingTilgangId: UUID) {
		val sql = """
			update gjennomforing_tilgang_foresporsel set
				beslutning_av_nav_ansatt_id = :beslutningAvNavAnsattId,
				gjennomforing_tilgang_id = :gjennomforingTilgangId,
				beslutning = :beslutning,
				tidspunkt_beslutning = current_timestamp
			 where id = :id
		""".trimIndent()

		val params = sqlParameters(
			"id" to foresporselId,
			"gjennomforingTilgangId" to gjennomforingTilgangId,
			"beslutning" to Beslutning.GODKJENT.name,
			"beslutningAvNavAnsattId" to beslutningAvNavAnsattId
		)

		template.update(sql, params)
	}

	internal fun avvisForesporsel(foresporselId: UUID, beslutningAvNavAnsattId: UUID) {
		val sql = """
			update gjennomforing_tilgang_foresporsel set
				beslutning_av_nav_ansatt_id = :beslutningAvNavAnsattId,
				beslutning = :beslutning,
				tidspunkt_beslutning = current_timestamp
			 where id = :id
		""".trimIndent()

		val params = sqlParameters(
			"id" to foresporselId,
			"beslutning" to Beslutning.AVVIST.name,
			"beslutningAvNavAnsattId" to beslutningAvNavAnsattId
		)

		template.update(sql, params)
	}

}

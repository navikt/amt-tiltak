package no.nav.amt.tiltak.tilgangskontroll.foresporsel

import no.nav.amt.tiltak.common.db_utils.*
import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class TilgangForesporselRepository(
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

	internal fun opprettForesporsel(opprettForesporselCmd: OpprettForesporselCmd) {

	}

	internal fun hentForesporsel(foresporselId: UUID): TilgangForesporselDbo? {
		val sql = """
			select * from gjennomforing_tilgang_foresporsel where id = :foresporselId
		""".trimIndent()

		return template.query(
			sql,
			sqlParameters("foresporselId" to foresporselId),
			rowMapper
		).firstOrNull()
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
			 where foresporsel_id = :foresporselId
		""".trimIndent()

		val params = sqlParameters(
			"foresporselId" to foresporselId,
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
				gjennomforing_tilgang_id = :gjennomforingTilgangId,
				beslutning = :beslutning,
				tidspunkt_beslutning = current_timestamp
			 where foresporsel_id = :foresporselId
		""".trimIndent()

		val params = sqlParameters(
			"foresporselId" to foresporselId,
			"beslutning" to Beslutning.AVVIST.name,
			"beslutningAvNavAnsattId" to beslutningAvNavAnsattId
		)

		template.update(sql, params)
	}

}

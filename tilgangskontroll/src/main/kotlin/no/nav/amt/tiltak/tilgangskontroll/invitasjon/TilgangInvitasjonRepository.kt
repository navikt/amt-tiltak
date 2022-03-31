package no.nav.amt.tiltak.tilgangskontroll.invitasjon

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime
import java.util.*

@Repository
class TilgangInvitasjonRepository(
	private val template: NamedParameterJdbcTemplate
) {

	internal fun hentUbrukteInvitasjoner(gjennomforingId: UUID): List<TilgangInvitasjonDbo> {
		return emptyList()
	}

	internal fun opprettInvitasjon(id: UUID, gjennomforingId: UUID, opprettetAvNavAnsattId: UUID, gydligTil: ZonedDateTime) {
		val sql = """
			INSERT INTO gjennomforing_tilgang_invitasjon(id, gjennomforing_id, gydlig_til, opprettet_av_nav_ansatt_id)
			 	VALUES(:id, :gjennomforingId, :gydligTil, :opprettetAvNavAnsattId)
		""".trimIndent()

		val parameters = sqlParameters(
			"id" to id,
			"gjennomforingId" to gjennomforingId,
			"gydligTil" to gydligTil,
			"opprettetAvNavAnsattId" to opprettetAvNavAnsattId
		)

		template.update(sql, parameters)
	}

	internal fun settTilBrukt(tilhorendeTilgangForesporselId: UUID) {

//		er_brukt                   boolean                  not null default false,
//		tidspunkt_brukt            timestamp with time zone not null,
//		tilgang_foresporsel_id     uuid references gjennomforing_tilgang_foresporsel (id),
	}

	internal fun avbrytInvitasjon(invitasjonId: UUID) {

	}

}

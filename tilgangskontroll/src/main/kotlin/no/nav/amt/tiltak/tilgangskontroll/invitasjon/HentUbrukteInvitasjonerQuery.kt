package no.nav.amt.tiltak.tilgangskontroll.invitasjon

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getUUID
import no.nav.amt.tiltak.common.db_utils.getZonedDateTime
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime
import java.util.*

data class UbruktInvitasjonDbo(
	val id: UUID,
	val opprettetAvNavIdent: String,
	val opprettetDato: ZonedDateTime,
	val gyldigTilDato: ZonedDateTime,
)

@Repository
class HentUbrukteInvitasjonerQuery(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		UbruktInvitasjonDbo(
			id = rs.getUUID("gjennomforing_tilgang_invitasjon__id"),
			opprettetAvNavIdent = rs.getString("nav_ansatt__nav_ident"),
			opprettetDato = rs.getZonedDateTime("gjennomforing_tilgang_invitasjon__created_at"),
			gyldigTilDato = rs.getZonedDateTime("gjennomforing_tilgang_invitasjon__gyldig_til")
		)
	}

	private val sql = """
		select
			gti.id as gjennomforing_tilgang_invitasjon__id,
			na.nav_ident as nav_ansatt__nav_ident,
			gti.created_at as gjennomforing_tilgang_invitasjon__created_at,
			gti.gyldig_til as gjennomforing_tilgang_invitasjon__gyldig_til
		 from gjennomforing_tilgang_invitasjon gti
			left join nav_ansatt na on na.id = gti.opprettet_av_nav_ansatt_id
			where gti.gjennomforing_id = :gjennomforingId
	""".trimIndent()

	internal fun query(gjennomforingId: UUID): List<UbruktInvitasjonDbo> {
		val parameters = sqlParameters("gjennomforingId" to gjennomforingId)
		return template.query(sql, parameters, rowMapper)
	}

}

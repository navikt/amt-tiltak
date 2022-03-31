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
			id = rs.getUUID("gti.id"),
			opprettetAvNavIdent = rs.getString(""),
			opprettetDato = rs.getZonedDateTime("gti.created_at"),
			gyldigTilDato = rs.getZonedDateTime("gti.gyldig_til")
		)
	}

	private val sql = """
		select * from gjennomforing_tilgang_invitasjon gti
			left join nav_ansatt na on na.id = gti.opprettet_av_nav_ansatt_id
			where gti.gjennomforing_id = :gjennomforingId
	""".trimIndent()

	internal fun query(gjennomforingId: UUID): List<UbruktInvitasjonDbo> {
		val parameters = sqlParameters("gjennomforingId" to gjennomforingId)
		return template.query(sql, parameters, rowMapper)
	}

}

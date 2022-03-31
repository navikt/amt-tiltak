package no.nav.amt.tiltak.tilgangskontroll.invitasjon

import no.nav.amt.tiltak.common.db_utils.DbUtils
import no.nav.amt.tiltak.common.db_utils.getZonedDateTime
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.ZonedDateTime
import java.util.*

data class InvitasjonInfoDbo(
	val overordnetEnhetNavn: String,
	val gjennomforingNavn: String,
	val erBrukt: Boolean,
	val gyldigTil: ZonedDateTime
)

@Repository
class HentInvitasjonInfoQuery(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		InvitasjonInfoDbo(
			overordnetEnhetNavn = rs.getString("a.overordnet_enhet_navn"),
			gjennomforingNavn = rs.getString("g.navn"),
			erBrukt = rs.getBoolean("gti.er_brukt"),
			gyldigTil = rs.getZonedDateTime("gti.gyldig_til"),
		)
	}

	private val sql = """
		select * from gjennomforing_tilgang_invitasjon gti
			left join gjennomforing g on g.id = gti.gjennomforing_id
			left join arrangor a on a.id = g.arrangor_id
			where gti.id = :invitasjonId
	""".trimIndent()

	internal fun query(invitasjonId: UUID): InvitasjonInfoDbo {
		val parameters = DbUtils.sqlParameters("invitasjonId" to invitasjonId)
		return template.query(sql, parameters, rowMapper).firstOrNull() ?: throw NoSuchElementException("Fant ikke tilgang invitasjon med id: $invitasjonId")
	}

}

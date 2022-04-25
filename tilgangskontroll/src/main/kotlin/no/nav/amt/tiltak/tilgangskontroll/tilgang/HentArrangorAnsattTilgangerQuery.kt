package no.nav.amt.tiltak.tilgangskontroll.tilgang

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getNullableString
import no.nav.amt.tiltak.common.db_utils.getUUID
import no.nav.amt.tiltak.common.db_utils.getZonedDateTime
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.util.*

@Component
open class HentArrangorAnsattTilgangerQuery(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		ArrangorAnsattGjennomforingTilgangDbo(
			id = rs.getUUID("id"),
			fornavn = rs.getString("fornavn"),
			mellomnavn = rs.getNullableString("mellomnavn"),
			etternavn = rs.getString("etternavn"),
			opprettetDato = rs.getZonedDateTime("created_at"),
			opprettetAvNavIdent = rs.getNullableString("nav_ident"),
		)
	}

	internal open fun query(gjennomforingId: UUID): List<ArrangorAnsattGjennomforingTilgangDbo> {
		val sql = """
			select
			 	aagt.id as id,
			  	aa.fornavn as fornavn,
			  	aa.mellomnavn as mellomnavn,
			  	aa.etternavn as etternavn,
			  	na.nav_ident as nav_ident,
			  	aagt.created_at as created_at
			 from arrangor_ansatt_gjennomforing_tilgang aagt
				inner join arrangor_ansatt aa on aagt.ansatt_id = aa.id
				left join nav_ansatt na on aagt.opprettet_av_nav_ansatt_id = na.id
				where aagt.gjennomforing_id = :gjennomforingId and aagt.stoppet_tidspunkt is null
		""".trimIndent()

		val parameters = sqlParameters("gjennomforingId" to gjennomforingId)

		return template.query(sql, parameters, rowMapper)
	}

	data class ArrangorAnsattGjennomforingTilgangDbo(
		val id: UUID,
		val fornavn: String,
		val mellomnavn: String?,
		val etternavn: String,
		val opprettetDato: ZonedDateTime,
		val opprettetAvNavIdent: String?
	)

}

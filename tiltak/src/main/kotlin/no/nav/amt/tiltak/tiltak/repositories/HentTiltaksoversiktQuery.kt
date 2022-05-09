package no.nav.amt.tiltak.tiltak.repositories

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getNullableString
import no.nav.amt.tiltak.common.db_utils.getUUID
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
open class HentTiltaksoversiktQuery(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		HentTiltaksoversiktQueryDbo(
			id = rs.getUUID("gjennomforing_id"),
			navn = rs.getString("gjennomforing_navn"),
			lopenr = rs.getInt("gjennomforing_lopenr"),
			opprettetAar = rs.getInt("gjennomforing_opprettet_aar"),
			arrangorVirksomhetsnavn = rs.getString("arrangor_navn"),
			arrangorOrganisasjonsnavn = rs.getNullableString("arrangor_overordnet_enhet_navn"),
		)
	}

	open fun query(gjennomforingIder: List<UUID>): List<HentTiltaksoversiktQueryDbo> {
		if (gjennomforingIder.isEmpty()) return emptyList()

		val sql = """
			SELECT
			 	g.id as gjennomforing_id,
			 	g.navn as gjennomforing_navn,
			 	g.lopenr as gjennomforing_lopenr,
			 	g.opprettet_aar as gjennomforing_opprettet_aar,
			 	a.navn as arrangor_navn,
			 	a.overordnet_enhet_navn as arrangor_overordnet_enhet_navn
			 FROM gjennomforing g
				JOIN arrangor a on a.id = g.arrangor_id
				WHERE g.id in(:gjennomforingIder)
		""".trimIndent()

		val parameters = sqlParameters(
			"gjennomforingIder" to gjennomforingIder
		)

		return template.query(sql, parameters, rowMapper)
	}

}

data class HentTiltaksoversiktQueryDbo(
	val id: UUID,
	val navn: String,
	val lopenr: Int,
	val opprettetAar: Int,
	val arrangorVirksomhetsnavn: String,
	val arrangorOrganisasjonsnavn: String?
)

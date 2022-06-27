package no.nav.amt.tiltak.tiltak.repositories

import no.nav.amt.tiltak.common.db_utils.DbUtils
import no.nav.amt.tiltak.common.db_utils.getUUID
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
open class HentGjennomforingerFraArrangorerQuery(
	private val template: NamedParameterJdbcTemplate
)  {

	open fun query(arrangorVirksomhetsnummere: List<String>): List<UUID> {
		if (arrangorVirksomhetsnummere.isEmpty())
			return emptyList()

		val sql = """
			SELECT g.id as gjennomforing_id FROM gjennomforing g
				JOIN arrangor a on a.id = g.arrangor_id
			WHERE g.status = 'GJENNOMFORES' AND a.organisasjonsnummer in(:arrangorVirksomhetsnummere)
		""".trimIndent()

		val parameters = DbUtils.sqlParameters(
			"arrangorVirksomhetsnummere" to arrangorVirksomhetsnummere
		)

		return template.query(sql, parameters) { rs, _ -> rs.getUUID("gjennomforing_id") }
	}
}

package no.nav.amt.tiltak.tiltak.repositories

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getUUID
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
open class GjennomforingerPaEnheterQuery(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		GjennomforingPaEnheterQueryDbo(
			id = rs.getUUID("id"),
			navn = rs.getString("navn"),
		)
	}

	open fun query(navEnhetIder: List<UUID>): List<GjennomforingPaEnheterQueryDbo> {
		if (navEnhetIder.isEmpty()) return emptyList()

		val sql = """
			SELECT * FROM gjennomforing WHERE nav_enhet_id in(:navEnhetIder)
		""".trimIndent()

		val parameters = sqlParameters(
			"navEnhetIder" to navEnhetIder
		)

		return template.query(sql, parameters, rowMapper)
	}

}

data class GjennomforingPaEnheterQueryDbo(
	val id: UUID,
	val navn: String,
)

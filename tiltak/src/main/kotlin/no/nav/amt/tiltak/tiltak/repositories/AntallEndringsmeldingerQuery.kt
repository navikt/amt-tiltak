package no.nav.amt.tiltak.tiltak.repositories

import no.nav.amt.tiltak.common.db_utils.DbUtils
import no.nav.amt.tiltak.common.db_utils.getUUID
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class AntallEndringsmeldingerQuery(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		AntallEndringsmeldingerQueryDbo(
			gjennomforingId = rs.getUUID("gjennomforing_id"),
			antallMeldinger = rs.getInt("antall_endringsmeldinger"),
		)
	}

	open fun query(gjennomforingIder: List<UUID>): List<AntallEndringsmeldingerQueryDbo> {
		if (gjennomforingIder.isEmpty())
			return emptyList()

		val sql = """
			SELECT g.id as gjennomforing_id, count(e.id) as antall_endringsmeldinger FROM endringsmelding e
				JOIN deltaker d on d.id = e.deltaker_id
				JOIN gjennomforing g on g.id = d.gjennomforing_id
			WHERE g.id in(:gjennomforingIder) AND e.aktiv = true GROUP BY g.id
		""".trimIndent()

		val parameters = DbUtils.sqlParameters(
			"gjennomforingIder" to gjennomforingIder
		)

		return template.query(sql, parameters, rowMapper)
	}

}

data class AntallEndringsmeldingerQueryDbo(
	val gjennomforingId: UUID,
	val antallMeldinger: Int
)

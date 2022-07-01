package no.nav.amt.tiltak.endringsmelding

import no.nav.amt.tiltak.common.db_utils.DbUtils
import no.nav.amt.tiltak.common.db_utils.getNullableLocalDate
import no.nav.amt.tiltak.common.db_utils.getUUID
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

@Component
open class HentAktivEndringsmeldingForDeltakereQuery(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		AktivEndringsmeldingQueryDbo(
			deltakerId = rs.getUUID("deltaker_id"),
			startDato = rs.getNullableLocalDate("start_dato")
		)
	}

	open fun query(deltakerIder: List<UUID>): List<AktivEndringsmeldingQueryDbo> {
		if (deltakerIder.isEmpty())
			return emptyList()

		val sql = """
			SELECT deltaker_id, start_dato FROM endringsmelding
			WHERE aktiv = true and deltaker_id in(:deltakerIder)
		""".trimIndent()

		val parameters = DbUtils.sqlParameters(
			"deltakerIder" to deltakerIder
		)

		return template.query(sql, parameters, rowMapper)
	}

}

data class AktivEndringsmeldingQueryDbo(
	val deltakerId: UUID,
	val startDato: LocalDate?
)

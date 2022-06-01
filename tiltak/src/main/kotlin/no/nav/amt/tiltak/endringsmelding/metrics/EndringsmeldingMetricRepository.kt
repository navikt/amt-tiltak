package no.nav.amt.tiltak.endringsmelding.metrics

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component

@Component
class EndringsmeldingMetricRepository(
	private val template: NamedParameterJdbcTemplate
) {

	fun totaltAntallEndringsmeldinger() = template.queryForObject(
		"SELECT COUNT(*) FROM endringsmelding",
		MapSqlParameterSource(),
		Int::class.java) ?: 0

	fun antallAktiveEndringsmeldinger(): Int = template.queryForObject(
		"SELECT COUNT(*) FROM endringsmelding WHERE aktiv = true",
		MapSqlParameterSource(),
		Int::class.java) ?: 0

}

package no.nav.amt.tiltak.endringsmelding.metrics

import no.nav.amt.tiltak.utils.getNullableLocalDateTime
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class EndringsmeldingMetricRepository(
	private val template: NamedParameterJdbcTemplate
) {

	fun getMetrics(): EndringsmeldingMetricHolder? {
		val sql = """
			select count(*)                                                                                                as antall_total,
				   (select count(*) from endringsmelding where aktiv is true)                                              as antall_aktive,
				   (select min(created_at) from endringsmelding where aktiv is true)                                       as eldste_aktive,
				   (select count(*) FROM endringsmelding WHERE aktiv = false AND ferdiggjort_av_nav_ansatt_id IS NOT NULL) as manuelt_ferdige,
				   (select count(*) FROM endringsmelding WHERE aktiv = false AND ferdiggjort_av_nav_ansatt_id IS NULL)     as automatisk_ferdige,
				   (select avg((EXTRACT(EPOCH FROM ferdiggjort_tidspunkt) - EXTRACT(EPOCH FROM created_at)) / 60))       as gjennomsnittelig_tid_minutter
			from endringsmelding
		""".trimIndent()

		return template.query(sql) { rs, _ ->
			EndringsmeldingMetricHolder(
				antallTotalt = rs.getInt("antall_total"),
				antallAktive = rs.getInt("antall_aktive"),
				eldsteAktive = rs.getNullableLocalDateTime("eldste_aktive"),
				manueltFerdige = rs.getInt("manuelt_ferdige"),
				automatiskFerdige = rs.getInt("automatisk_ferdige"),
				gjennomsnitteligTidIMinutter = rs.getDouble("gjennomsnittelig_tid_minutter")
			)
		}
			.firstOrNull()

	}

	data class EndringsmeldingMetricHolder(
		val antallTotalt: Int,
		val antallAktive: Int,
		val eldsteAktive: LocalDateTime?,
		val manueltFerdige: Int,
		val automatiskFerdige: Int,
		val gjennomsnitteligTidIMinutter: Double
	)

}

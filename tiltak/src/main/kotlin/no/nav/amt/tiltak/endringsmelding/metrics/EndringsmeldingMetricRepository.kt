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
			select count(*) as antall_total,
				   (select count(*) from endringsmelding where status = 'AKTIV') as antall_aktive,
				   (select min(created_at) from endringsmelding where status = 'AKTIV') as eldste_aktive,
				   (select count(*) FROM endringsmelding WHERE status = 'UTFORT') as manuelt_ferdige,
				   (select count(*) FROM endringsmelding WHERE status = 'UTDATERT') as automatisk_ferdige,
				   (select avg((EXTRACT(EPOCH FROM utfort_tidspunkt) - EXTRACT(EPOCH FROM created_at)) / 60)) as gjennomsnittelig_tid_minutter
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
		}.firstOrNull()

	}


	fun getAntallEndringsmeldingerPerType(): List<AntallEndringsmeldingerPerType> {
		val sql = "SELECT type, count(*) AS antall FROM endringsmelding GROUP BY type"
	 	return template.query(sql) { rs, _ ->
			AntallEndringsmeldingerPerType(
				type = rs.getString("type"),
				antall = rs.getInt("antall"),
			)
		}
	}

	data class AntallEndringsmeldingerPerType(
		val type: String,
		val antall: Int,
	)

	data class EndringsmeldingMetricHolder(
		val antallTotalt: Int,
		val antallAktive: Int,
		val eldsteAktive: LocalDateTime?,
		val manueltFerdige: Int,
		val automatiskFerdige: Int,
		val gjennomsnitteligTidIMinutter: Double
	)

}

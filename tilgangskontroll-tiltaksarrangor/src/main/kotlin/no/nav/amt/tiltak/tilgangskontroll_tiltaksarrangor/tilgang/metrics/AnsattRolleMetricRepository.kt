package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.tilgang.metrics

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
open class AnsattRolleMetricRepository(
	private val template: NamedParameterJdbcTemplate
) {
	fun getMetrikker(): AnsattRolleMetrikkHolder {
		val sql = """
			select
			(
				select count(distinct ansatt_id) as antall_veiledere
				from arrangor_ansatt_rolle
				where gyldig_til > current_timestamp and rolle = 'VEILEDER' and ansatt_id not in (
					 select ansatt_id
					 from arrangor_ansatt_rolle
					 where gyldig_til > current_timestamp and rolle = 'KOORDINATOR'
				)
			),
			(
				select count(distinct ansatt_id) as antall_koordinatorer
				from arrangor_ansatt_rolle
				where gyldig_til > current_timestamp and rolle = 'KOORDINATOR' and ansatt_id not in (
					 select ansatt_id
					 from arrangor_ansatt_rolle
					 where gyldig_til > current_timestamp and rolle = 'VEILEDER'
				)
			),
			(
				select count(distinct ansatt_id) as antall_med_begge_roller
				from arrangor_ansatt_rolle
				where gyldig_til > current_timestamp and rolle = 'VEILEDER' and ansatt_id in (
					select ansatt_id
					from arrangor_ansatt_rolle
					where gyldig_til > current_timestamp and rolle = 'KOORDINATOR'
				)
			);
		""".trimIndent()

		return template.query(sql) { rs, _ ->
			AnsattRolleMetrikkHolder(
				antallVeiledere = rs.getInt("antall_veiledere"),
				antallKoordinatorer = rs.getInt("antall_koordinatorer"),
				antallMedBeggeRoller = rs.getInt("antall_med_begge_roller"),
			)
		}.first()
	}
}

data class AnsattRolleMetrikkHolder(
	val antallKoordinatorer: Int,
	val antallVeiledere: Int,
	val antallMedBeggeRoller: Int,
)

package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.tilgang.metrics

import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
open class AnsattRolleMetricRepository(
	private val template: NamedParameterJdbcTemplate
) {

	fun antallAnsatteSomErKunKoordinator() = tellAnsatteMedRoller(
			rolleSomSkalTelles = ArrangorAnsattRolle.KOORDINATOR,
			rolleSomIkkeSkalTelles = ArrangorAnsattRolle.VEILEDER
		)

	fun antallAnsatteSomErKunVeileder() = tellAnsatteMedRoller(
			rolleSomSkalTelles = ArrangorAnsattRolle.VEILEDER,
			rolleSomIkkeSkalTelles = ArrangorAnsattRolle.KOORDINATOR
		)

	fun antallAnsatteSomHarBeggeRollene() = tellAnsatteMedRoller(
			rolleSomSkalTelles = ArrangorAnsattRolle.VEILEDER,
			rolleSomIkkeSkalTelles = ArrangorAnsattRolle.KOORDINATOR,
			tellDeSomHarBegge = true,
		)

	private fun tellAnsatteMedRoller(
		rolleSomSkalTelles: ArrangorAnsattRolle,
		rolleSomIkkeSkalTelles: ArrangorAnsattRolle,
		tellDeSomHarBegge: Boolean = false
	): Int {
		val not = if (tellDeSomHarBegge) "" else "not"

		val sql = """
			select count(distinct ansatt_id) as antall
			from arrangor_ansatt_rolle
			where gyldig_til > current_timestamp and rolle = '${rolleSomSkalTelles.name}' and ansatt_id $not in (
				select ansatt_id from arrangor_ansatt_rolle where gyldig_til > current_timestamp and rolle = '${rolleSomIkkeSkalTelles.name}'
			);
		""".trimIndent()

		return template.query(sql) { rs, _ -> rs.getInt("antall") }.first()
	}
}

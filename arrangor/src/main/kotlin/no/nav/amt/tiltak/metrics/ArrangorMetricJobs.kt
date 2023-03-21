package no.nav.amt.tiltak.metrics

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.nav.common.job.JobRunner
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.atomic.AtomicInteger

@Configuration
open class ArrangorMetricJobs(
	registry: MeterRegistry,
	private val metricRepository: MetricRepository
) {

	private val antallAnsatte = "amt_tiltak_arrangor_ansatt_antall_ansatte"
	private val loggetInnSisteTime = "amt_tiltak_arrangor_ansatt_innlogget_siste_1_time"
	private val loggetInnSisteDag = "amt_tiltak_arrangor_ansatt_innlogget_siste_24_timer"
	private val loggetInnSisteUke = "amt_tiltak_arrangor_ansatt_innlogget_siste_1_uke"


	private val antallAktiveDeltakere = "amt_tiltak_aktive_deltakere_antall"
	private val antallAktiveDeltakereMedVeileder = "amt_tiltak_aktive_deltakere_med_veileder_antall"

	private val sistInnloggetGauges: Map<String, AtomicInteger> = mapOf(
		Pair(antallAnsatte, registry.gauge(antallAnsatte, AtomicInteger(0))!!),
		Pair(loggetInnSisteDag, registry.gauge(loggetInnSisteDag, AtomicInteger(0))!!),
		Pair(loggetInnSisteUke, registry.gauge(loggetInnSisteUke, AtomicInteger(0))!!)
	)

	private val tildeltVeilederGauges: Map<String, AtomicInteger> = mapOf(
		Pair(antallAktiveDeltakere, registry.gauge(antallAktiveDeltakere, AtomicInteger(0))!!),
		Pair(antallAktiveDeltakereMedVeileder, registry.gauge(antallAktiveDeltakereMedVeileder, AtomicInteger(0))!!)
	)

	private val antallRolleInnloggetGauges: Map<RollePermutasjon, AtomicInteger>  = mapOf(
		RollePermutasjon.KOORDINATOR to registry.gauge(
			loggetInnSisteTime, Tags.of("rolle", RollePermutasjon.KOORDINATOR.name), AtomicInteger(0)
		)!!,
		RollePermutasjon.VEILEDER to registry.gauge(
			loggetInnSisteTime, Tags.of("rolle", RollePermutasjon.VEILEDER.name), AtomicInteger(0)
		)!!,
		RollePermutasjon.KOORDINATOR_OG_VEILEDER to registry.gauge(
			loggetInnSisteTime, Tags.of("rolle", RollePermutasjon.KOORDINATOR_OG_VEILEDER.name), AtomicInteger(0)
		)!!,
		RollePermutasjon.ANY to registry.gauge(
			loggetInnSisteTime, Tags.of("rolle", RollePermutasjon.ANY.name), AtomicInteger(0)
		)!!,
	)

	//Every 5 minutes
	@Scheduled(cron = "0 */5 * ? * *")
	open fun updateSistInnloggetMetrics() {
		val metrics = metricRepository.getSistInnloggetMetrics()

		sistInnloggetGauges[antallAnsatte]?.set(metrics.antallAnsatte)
		sistInnloggetGauges[loggetInnSisteDag]?.set(metrics.antallAnsatteInnloggetSisteDag)
		sistInnloggetGauges[loggetInnSisteUke]?.set(metrics.antallAnsatteInnloggetSisteUke)
	}

	@Scheduled(cron = "0 */10 * ? * *")
	open fun updateSistInnloggetRolleMetrics() {
		val metrics = metricRepository.getRolleInnloggetSisteTime()

		JobRunner.run("oppdaterer_roller_innlogget_siste_time") {
			antallRolleInnloggetGauges[RollePermutasjon.KOORDINATOR]?.set(metrics.antallKoordinatorer)
			antallRolleInnloggetGauges[RollePermutasjon.VEILEDER]?.set(metrics.antallVeiledere)
			antallRolleInnloggetGauges[RollePermutasjon.KOORDINATOR_OG_VEILEDER]?.set(metrics.antallBegge)
			antallRolleInnloggetGauges[RollePermutasjon.ANY]?.set(metrics.totaltAntallAnsatte)
		}
	}

	// Hver halve time
	@Scheduled(cron = "0 */30 * ? * *")
	open fun updateTildeltVeilederMetrics() {
		JobRunner.run("oppdater_deltakere_med_veileder_metric") {
			val metrics = metricRepository.hentAndelAktiveDeltakereMedVeileder()

			tildeltVeilederGauges[antallAktiveDeltakere]?.set(metrics.totalAntallDeltakere)
			tildeltVeilederGauges[antallAktiveDeltakereMedVeileder]?.set(metrics.antallMedVeileder)
		}
	}

	private enum class RollePermutasjon {
		KOORDINATOR, VEILEDER, KOORDINATOR_OG_VEILEDER, ANY
	}
}

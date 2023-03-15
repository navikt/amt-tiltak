package no.nav.amt.tiltak.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.amt.tiltak.ansatt.ArrangorAnsattRepository
import no.nav.common.job.JobRunner
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.atomic.AtomicInteger

@Configuration
open class ArrangorMetricJobs(
	registry: MeterRegistry,
	private val arrangorAnsattRepository: ArrangorAnsattRepository,
	private val arrangorVeilederMetricRepository: ArrangorVeilederMetricRepository
) {

	private val antallAnsatte = "amt_tiltak_arrangor_ansatt_antall_ansatte"
	private val loggetInnSisteTime = "amt_tiltak_arrangor_ansatt_innlogget_siste_1_time"
	private val loggetInnSisteDag = "amt_tiltak_arrangor_ansatt_innlogget_siste_24_timer"
	private val loggetInnSisteUke = "amt_tiltak_arrangor_ansatt_innlogget_siste_1_uke"

	private val antallAktiveDeltakere = "amt_tiltak_aktive_deltakere_antall"
	private val antallAktiveDeltakereMedVeileder = "amt_tiltak_aktive_deltakere_med_veileder_antall"



	private val sistInnloggetGauges: Map<String, AtomicInteger> = mapOf(
		Pair(antallAnsatte, registry.gauge(antallAnsatte, AtomicInteger(0))!!),
		Pair(loggetInnSisteTime, registry.gauge(loggetInnSisteTime, AtomicInteger(0))!!),
		Pair(loggetInnSisteDag, registry.gauge(loggetInnSisteDag, AtomicInteger(0))!!),
		Pair(loggetInnSisteUke, registry.gauge(loggetInnSisteUke, AtomicInteger(0))!!)
	)

	private val tildeltVeilederGauges: Map<String, AtomicInteger> = mapOf(
		Pair(antallAktiveDeltakere, registry.gauge(antallAnsatte, AtomicInteger(0))!!),
		Pair(antallAktiveDeltakereMedVeileder, registry.gauge(loggetInnSisteTime, AtomicInteger(0))!!)
	)


	//Every 5 minutes
	@Scheduled(cron = "0 */5 * ? * *")
	open fun updateSistInnloggetMetrics() {
		val metrics = arrangorAnsattRepository.getAnsattMetrics()

		sistInnloggetGauges[antallAnsatte]?.set(metrics.antallAnsatte)
		sistInnloggetGauges[loggetInnSisteTime]?.set(metrics.antallAnsatteInnloggetSisteTime)
		sistInnloggetGauges[loggetInnSisteDag]?.set(metrics.antallAnsatteInnloggetSisteDag)
		sistInnloggetGauges[loggetInnSisteUke]?.set(metrics.antallAnsatteInnloggetSisteUke)
	}

	// Hver halve time
	@Scheduled(cron = "0 */30 * ? * *")
	open fun updateTildeltVeilederMetrics() {
		JobRunner.run("oppdater_deltakere_med_veileder_metric", this::runTildelVeileder)
	}

	private fun runTildelVeileder() {
		val metrics = arrangorVeilederMetricRepository.hentAndelAktiveDeltakereMedVeileder()

		tildeltVeilederGauges[antallAktiveDeltakere]?.set(metrics.totalAntallDeltakere)
		tildeltVeilederGauges[antallAktiveDeltakereMedVeileder]?.set(metrics.antallMedVeileder)
	}

}

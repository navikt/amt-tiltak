package no.nav.amt.tiltak.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.common.job.JobRunner
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.atomic.AtomicInteger

@Configuration
open class ArrangorMetricJobs(
	registry: MeterRegistry,
	private val metricRepository: MetricRepository
) {
	private val antallAktiveDeltakere = "amt_tiltak_aktive_deltakere_antall"
	private val antallAktiveDeltakereMedVeileder = "amt_tiltak_aktive_deltakere_med_veileder_antall"

	private val tildeltVeilederGauges: Map<String, AtomicInteger> = mapOf(
		Pair(antallAktiveDeltakere, registry.gauge(antallAktiveDeltakere, AtomicInteger(0))!!),
		Pair(antallAktiveDeltakereMedVeileder, registry.gauge(antallAktiveDeltakereMedVeileder, AtomicInteger(0))!!)
	)

	// Hver halve time
	@Scheduled(cron = "0 */30 * ? * *")
	open fun updateTildeltVeilederMetrics() {
		JobRunner.run("oppdater_deltakere_med_veileder_metric") {
			val metrics = metricRepository.hentAndelAktiveDeltakereMedVeileder()

			tildeltVeilederGauges[antallAktiveDeltakere]?.set(metrics.totalAntallDeltakere)
			tildeltVeilederGauges[antallAktiveDeltakereMedVeileder]?.set(metrics.antallMedVeileder)
		}
	}
}

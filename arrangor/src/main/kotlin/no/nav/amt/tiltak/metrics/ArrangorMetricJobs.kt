package no.nav.amt.tiltak.metrics

import io.micrometer.core.instrument.MeterRegistry
import no.nav.amt.tiltak.ansatt.ArrangorAnsattRepository
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import java.util.concurrent.atomic.AtomicInteger

@Configuration
open class ArrangorMetricJobs(
	registry: MeterRegistry,
	private val arrangorAnsattRepository: ArrangorAnsattRepository
) {

	private val antallAnsatte = "amt_tiltak_arrangor_ansatt_antall_ansatte"
	private val loggetInnSisteTime = "amt_tiltak_arrangor_ansatt_innlogget_siste_1_time"
	private val loggetInnSisteDag = "amt_tiltak_arrangor_ansatt_innlogget_siste_24_timer"
	private val loggetInnSisteUke = "amt_tiltak_arrangor_ansatt_innlogget_siste_1_uke"

	private val simpleGauges: Map<String, AtomicInteger> = mapOf(
		Pair(antallAnsatte, registry.gauge(antallAnsatte, AtomicInteger(0))!!),
		Pair(loggetInnSisteTime, registry.gauge(loggetInnSisteTime, AtomicInteger(0))!!),
		Pair(loggetInnSisteDag, registry.gauge(loggetInnSisteDag, AtomicInteger(0))!!),
		Pair(loggetInnSisteUke, registry.gauge(loggetInnSisteUke, AtomicInteger(0))!!)
	)

	//Every 5 minutes
	@Scheduled(cron = "0 */5 * ? * *")
	open fun updateMetrics() {
		val metrics = arrangorAnsattRepository.getAnsattMetrics()

		simpleGauges[antallAnsatte]?.set(metrics.antallAnsatte)
		simpleGauges[loggetInnSisteTime]?.set(metrics.antallAnsatteInnloggetSisteTime)
		simpleGauges[loggetInnSisteDag]?.set(metrics.antallAnsatteInnloggetSisteDag)
		simpleGauges[loggetInnSisteUke]?.set(metrics.antallAnsatteInnloggetSisteUke)
	}

}

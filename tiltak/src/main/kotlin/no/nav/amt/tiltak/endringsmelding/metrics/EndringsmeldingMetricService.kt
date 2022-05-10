package no.nav.amt.tiltak.endringsmelding.metrics

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicInteger

private const val antallTotalEndringsmeldinger = "amt_tiltak_endringsmelding_totalt_antall"

private const val antallAktivEndringsmeldinger = "amt_tiltak_endringsmelding_aktiv_antall"

@Service
class EndringsmeldingMetricService(
	registry: MeterRegistry,
	private val endringsmeldingMetricRepository: EndringsmeldingMetricRepository
) {

	private val simpleGauges: Map<String, AtomicInteger> = mapOf(
		Pair(antallTotalEndringsmeldinger, registry.gauge(antallTotalEndringsmeldinger, AtomicInteger(0))!!),
		Pair(antallAktivEndringsmeldinger, registry.gauge(antallAktivEndringsmeldinger, AtomicInteger(0))!!),
	)

	fun oppdaterMetrikker() {
		simpleGauges.getValue(antallAktivEndringsmeldinger).set(endringsmeldingMetricRepository.antallAktiveEndringsmeldinger())
		simpleGauges.getValue(antallTotalEndringsmeldinger).set(endringsmeldingMetricRepository.totaltAntallEndringsmeldinger())
	}

}

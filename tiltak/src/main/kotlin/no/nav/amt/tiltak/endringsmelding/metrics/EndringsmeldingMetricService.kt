package no.nav.amt.tiltak.endringsmelding.metrics

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.nav.amt.tiltak.endringsmelding.EndringsmeldingDbo
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.roundToInt

private const val antallTotalEndringsmeldinger = "amt_tiltak_endringsmelding_totalt_antall"
private const val antallAktivEndringsmeldinger = "amt_tiltak_endringsmelding_aktiv_antall"
private const val antallManueltFerdigEndringsmeldinger = "amt_tiltak_endringsmelding_manuelt_ferdig_antall"
private const val antallAutomatiskFerdigEndringsmeldinger = "amt_tiltak_endringsmelding_automatisk_ferdig_antall"
private const val eldsteAktiveIMinutter = "amt_tiltak_endringsmelding_eldste_aktive_i_minutter"
private const val gjennomsnitteligTidIMinutter = "amt_tiltak_endringsmelding_gjennomsnittelig_tid_i_minutter"
private const val antallEndringsmeldingerPerType = "amt_tiltak_endringsmelding_per_type_antall"

@Service
class EndringsmeldingMetricService(
	registry: MeterRegistry,
	private val endringsmeldingMetricRepository: EndringsmeldingMetricRepository
) {

	private val simpleGauges: Map<String, AtomicInteger> = mapOf(
		Pair(
			antallTotalEndringsmeldinger,
			registry.gauge(antallTotalEndringsmeldinger, AtomicInteger(0))!!
		),

		Pair(
			antallAktivEndringsmeldinger,
			registry.gauge(antallAktivEndringsmeldinger, AtomicInteger(0))!!
		),

		Pair(
			antallManueltFerdigEndringsmeldinger,
			registry.gauge(antallManueltFerdigEndringsmeldinger, AtomicInteger(0))!!
		),

		Pair(
			antallAutomatiskFerdigEndringsmeldinger,
			registry.gauge(antallAutomatiskFerdigEndringsmeldinger, AtomicInteger(0))!!
		),

		Pair(
			eldsteAktiveIMinutter,
			registry.gauge(eldsteAktiveIMinutter, AtomicInteger(0))!!
		),

		Pair(
			gjennomsnitteligTidIMinutter,
			registry.gauge(gjennomsnitteligTidIMinutter, AtomicInteger(0))!!
		)
	)

	private val antallEndringsmeldingerPerTypeGauges: Map<String, AtomicInteger> =
		EndringsmeldingDbo.Type.values().associate {
			it.name to registry.gauge(antallEndringsmeldingerPerType, Tags.of("type", it.name), AtomicInteger(0))!!
		}

	fun oppdaterMetrikker() {
		val metrics = endringsmeldingMetricRepository.getMetrics()

		simpleGauges.getValue(antallAktivEndringsmeldinger)
			.set(metrics?.antallAktive ?: 0)

		simpleGauges.getValue(antallTotalEndringsmeldinger)
			.set(metrics?.antallTotalt ?: 0)

		simpleGauges.getValue(antallManueltFerdigEndringsmeldinger)
			.set(metrics?.manueltFerdige ?: 0)

		simpleGauges.getValue(antallAutomatiskFerdigEndringsmeldinger)
			.set(metrics?.automatiskFerdige ?: 0)

		if (metrics?.eldsteAktive != null) {
			val durationInMinutes = Duration.between(metrics.eldsteAktive, LocalDateTime.now()).toMinutes()
			simpleGauges.getValue(eldsteAktiveIMinutter)
				.set(durationInMinutes.toInt())
		}

		simpleGauges.getValue(gjennomsnitteligTidIMinutter)
			.set(metrics?.gjennomsnitteligTidIMinutter?.roundToInt() ?: 0)

		endringsmeldingMetricRepository.getAntallEndringsmeldingerPerType().forEach {
			antallEndringsmeldingerPerTypeGauges[it.type]?.set(it.antall)
		}

	}

}

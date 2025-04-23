package no.nav.amt.tiltak.endringsmelding.metrics

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.endringsmelding.EndringsmeldingDbo
import no.nav.amt.tiltak.endringsmelding.metrics.EndringsmeldingMetricRepository.EndringsmeldingMetricHolder
import org.springframework.stereotype.Service
import java.time.Duration
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicInteger
import kotlin.math.roundToInt

@Suppress("SpellCheckingInspection")
@Service
class EndringsmeldingMetricService(
    registry: MeterRegistry,
    private val endringsmeldingMetricRepository: EndringsmeldingMetricRepository
) {

    private val updaters = listOf(
        "amt_tiltak_endringsmelding_totalt_antall" update { antallAktive },
        "amt_tiltak_endringsmelding_aktiv_antall" update { antallAktive },
        "amt_tiltak_endringsmelding_manuelt_ferdig_antall" update { manueltFerdige },
        "amt_tiltak_endringsmelding_automatisk_ferdig_antall" update { automatiskFerdige },
        "amt_tiltak_endringsmelding_eldste_aktive_i_minutter" update {
            eldsteAktive?.let { minutesSince(it) }
        },
        "amt_tiltak_endringsmelding_gjennomsnittelig_tid_i_minutter" update {
            gjennomsnitteligTidIMinutter.roundToInt()
        }
    )

    private val simpleGauges: List<(EndringsmeldingMetricHolder?) -> Unit> = registry.add(updaters)

    fun oppdaterMetrikker() {
        endringsmeldingMetricRepository.getMetrics().let { metrics ->
            for (updater in simpleGauges) updater(metrics)
        }
        endringsmeldingMetricRepository.getAntallEndringsmeldingerPerType().forEach {
            antallEndringsmeldingerPerTypeGauges[it.type]?.set(it.antall)
        }
        endringsmeldingMetricRepository.getAntallEndringsmeldingerPerStatus().forEach {
            antallEndringsmeldingerPerStatusGauges[it.key.name]?.set(it.value)
        }
    }

    private val antallEndringsmeldingerPerTypeGauges: Map<String, AtomicInteger> =
        EndringsmeldingDbo.Type.values().associate {
            it.name to registry.gauge(
                "amt_tiltak_endringsmelding_per_type_antall",
                Tags.of("type", it.name),
                AtomicInteger()
            )!!
        }

    private val antallEndringsmeldingerPerStatusGauges: Map<String, AtomicInteger> =
        Endringsmelding.Status.values().associate {
            it.name to registry.gauge(
                "amt_tiltak_endringsmelding_per_status_antall",
                Tags.of("type", it.name),
                AtomicInteger()
            )!!
        }

    private fun minutesSince(time: LocalDateTime): Int = Duration.between(time, LocalDateTime.now()).toMinutes().toInt()

    private infix fun String.update(updater: EndringsmeldingMetricHolder.() -> Int?) =
        this to updater

    private fun <T> MeterRegistry.add(pairs: List<Pair<String, T.() -> Int?>>) =
        run {
            pairs.map { (name, updater) ->
                gauge(name, updater)
            }
        }

    private fun <T> MeterRegistry.gauge(name: String, updater: T.() -> Int?): (T?) -> Unit =
        gauge(name, AtomicInteger())?.let { gauge ->
            { it?.let(updater)?.run(gauge::set) }
        }!!
}

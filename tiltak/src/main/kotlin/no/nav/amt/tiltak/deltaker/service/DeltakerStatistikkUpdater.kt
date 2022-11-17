package no.nav.amt.tiltak.deltaker.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.MultiGauge
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Tags
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.deltaker.repositories.DeltakerStatistikkRepository
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger


private const val deltakere = "amt.tiltak.antall.deltakere"
private const val deltakerePrStatus = "amt.tiltak.antall.deltakere.pr.status"
private const val arrangorer = "amt.tiltak.antall.arrangorer"
private const val arrangorerMedBrukere = "amt.tiltak.antall.arrangorer.med.brukere"
private const val aktiveArrangorer = "amt.tiltak.antall.aktive.arrangorer"
private const val aktiveArrangorerMedBrukere = "amt.tiltak.antall.aktive.arrangorer.med.brukere"
private const val gjennomforing = "amt.tiltak.antall.gjennomforing"
private const val gjennomforinger = "amt.tiltak.antall.gjennomforinger"
private const val gjennomforingerPrStatus = "amt.tiltak.antall.gjennomforinger.pr.status"
private const val eksponerteBrukere = "amt.tiltak.antall.brukere.eksponerte" // erstattes av eksponerteBrukerePrStatus
private const val eksponerteBrukerePrStatus = "amt.tiltak.antall.brukere.eksponerte.pr.status"

@Component
class DeltakerStatistikkUpdater(
	registry: MeterRegistry,
	private val deltakerStatistikkRepository: DeltakerStatistikkRepository
) {

	private val simpleGauges: Map<String, AtomicInteger> = mapOf(
		Pair(deltakere, registry.gauge(deltakere, AtomicInteger(0))!!),
		Pair(eksponerteBrukere, registry.gauge(eksponerteBrukere, AtomicInteger(0))!!),
		Pair(arrangorer, registry.gauge(arrangorer, AtomicInteger(0))!!),
		Pair(arrangorerMedBrukere, registry.gauge(arrangorerMedBrukere, AtomicInteger(0))!!),
		Pair(gjennomforinger, registry.gauge(gjennomforinger, AtomicInteger(0))!!),
		Pair(aktiveArrangorer, registry.gauge(aktiveArrangorer, AtomicInteger(0))!!),
		Pair(aktiveArrangorerMedBrukere, registry.gauge(aktiveArrangorerMedBrukere, AtomicInteger(0))!!),
	)

	private val gjennomforingGauge = MultiGauge.builder(gjennomforing).register(registry)


	private val deltakerStatusGauges: Map<String, AtomicInteger> =
		DeltakerStatus.Type.values().associate {
			it.name to registry.gauge(deltakerePrStatus, Tags.of("status", it.name), AtomicInteger(0))!!
		}

	private val eksponterteBrukereStatusGauges: Map<String, AtomicInteger> =
		DeltakerStatus.Type.values().associate {
			it.name to registry.gauge(eksponerteBrukerePrStatus, Tags.of("status", it.name), AtomicInteger(0))!!
		}

	private val gjennomforingStatusGauges: Map<String, AtomicInteger> =
		Gjennomforing.Status.values().associate {
			it.name to registry.gauge(gjennomforingerPrStatus, Tags.of("status", it.name), AtomicInteger(0))!!
		}


	fun oppdater() {
		simpleGauges.getValue(deltakere).set(deltakerStatistikkRepository.antallDeltakere()!!)
		deltakerStatistikkRepository.antallDeltakerePerStatus().forEach {
			deltakerStatusGauges.getValue(it.first).set(it.second)
		}
		simpleGauges.getValue(arrangorer).set(deltakerStatistikkRepository.antallArrangorer()!!)
		simpleGauges.getValue(arrangorerMedBrukere).set(deltakerStatistikkRepository.antallArrangorerMedBrukere()!!)
		simpleGauges.getValue(aktiveArrangorer).set(deltakerStatistikkRepository.antallAktiveArrangorer()!!)
		simpleGauges.getValue(aktiveArrangorerMedBrukere).set(deltakerStatistikkRepository.antallAktiveArrangorerMedBrukere()!!)
		simpleGauges.getValue(gjennomforinger).set(deltakerStatistikkRepository.antallGjennomforinger()!!)
		deltakerStatistikkRepository.antallGjennomforingerPrStatus().forEach {
			gjennomforingStatusGauges.getValue(it.first).set(it.second)
		}
		simpleGauges.getValue(eksponerteBrukere).set(deltakerStatistikkRepository.eksponerteBrukere()!!)
		oppdaterGjennomforingMaalinger()
		oppdaterEksponterteBrukerePerStatus()
	}



	fun oppdaterGjennomforingMaalinger() {

		val rows = deltakerStatistikkRepository.antallGjennomforingerGruppert().map {
			MultiGauge.Row.of(
				Tags.of(Tag.of("status", it.status), Tag.of("synligHosArrangor", it.synligHosArrangor.toString())),
				it.antall
			)
		}

		gjennomforingGauge.register(rows)
	}

	fun oppdaterEksponterteBrukerePerStatus() {

		val rows = deltakerStatistikkRepository.eksponerteBrukerePrStatus().map {
			eksponterteBrukereStatusGauges.getValue(it.first).set(it.second)
		}
	}
}

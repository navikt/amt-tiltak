package no.nav.amt.tiltak.deltaker.service

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.deltaker.repositories.DeltakerStatistikkRepository
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicInteger


private const val deltakere = "amt_tiltak_antall_deltakere"
private const val deltakerePrStatus = "amt_tiltak_antall_deltakere_pr_status"
private const val arrangorer = "amt_tiltak_antall_arrangorer"
private const val arrangorerMedBrukere = "amt_tiltak_antall_arrangorer_med_brukere"
private const val aktiveArrangorer = "amt_tiltak_antall_aktive_arrangorer"
private const val aktiveArrangorerMedBrukere = "amt_tiltak_antall_aktive_arrangorer_med_brukere"
private const val eksponerteBrukerePrStatus = "amt_tiltak_antall_brukere_eksponerte_pr_status"

@Component
class DeltakerStatistikkUpdater(
	registry: MeterRegistry,
	private val deltakerStatistikkRepository: DeltakerStatistikkRepository
) {

	private val simpleGauges: Map<String, AtomicInteger> = mapOf(
		Pair(deltakere, registry.gauge(deltakere, AtomicInteger(0))!!),
		Pair(arrangorer, registry.gauge(arrangorer, AtomicInteger(0))!!),
		Pair(arrangorerMedBrukere, registry.gauge(arrangorerMedBrukere, AtomicInteger(0))!!),
		Pair(aktiveArrangorer, registry.gauge(aktiveArrangorer, AtomicInteger(0))!!),
		Pair(aktiveArrangorerMedBrukere, registry.gauge(aktiveArrangorerMedBrukere, AtomicInteger(0))!!),
	)



	private val deltakerStatusGauges: Map<String, AtomicInteger> =
		DeltakerStatus.Type.values().associate {
			it.name to registry.gauge(deltakerePrStatus, Tags.of("status", it.name), AtomicInteger(0))!!
		}

	private val eksponterteBrukereStatusGauges: Map<String, AtomicInteger> =
		DeltakerStatus.Type.values().associate {
			it.name to registry.gauge(eksponerteBrukerePrStatus, Tags.of("status", it.name), AtomicInteger(0))!!
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
		oppdaterEksponterteBrukerePerStatus()
	}




	fun oppdaterEksponterteBrukerePerStatus() {
		deltakerStatistikkRepository.eksponerteBrukerePrStatus().map {
			eksponterteBrukereStatusGauges.getValue(it.first).set(it.second)
		}
	}
}

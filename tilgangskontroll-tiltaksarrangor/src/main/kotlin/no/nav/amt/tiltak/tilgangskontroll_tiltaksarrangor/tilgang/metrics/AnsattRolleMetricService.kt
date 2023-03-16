package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.tilgang.metrics

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicInteger

private const val antallAnsatteMedRoller = "amt_tiltak_antall_ansatte_med_roller"


@Service
class AnsattRolleMetricService(
	registry: MeterRegistry,
	private val ansattRolleMetricRepository: AnsattRolleMetricRepository,
) {

	private val antallRolletypeGauges: Map<RollePermutasjon, AtomicInteger>  = mapOf(
		RollePermutasjon.KOORDINATOR to registry.gauge(
			antallAnsatteMedRoller, Tags.of("rolle", RollePermutasjon.KOORDINATOR.name), AtomicInteger(0)
		)!!,
		RollePermutasjon.VEILEDER to registry.gauge(
			antallAnsatteMedRoller, Tags.of("rolle", RollePermutasjon.VEILEDER.name), AtomicInteger(0)
		)!!,
		RollePermutasjon.KOORDINATOR_OG_VEILEDER to registry.gauge(
			antallAnsatteMedRoller, Tags.of("rolle", RollePermutasjon.KOORDINATOR_OG_VEILEDER.name), AtomicInteger(0)
		)!!,
	)

	fun oppdaterMetrikker() {
		val metrikker = ansattRolleMetricRepository.getMetrikker()
		antallRolletypeGauges[RollePermutasjon.KOORDINATOR]?.set(metrikker.antallKoordinatorer)
		antallRolletypeGauges[RollePermutasjon.VEILEDER]?.set(metrikker.antallVeiledere)
		antallRolletypeGauges[RollePermutasjon.KOORDINATOR_OG_VEILEDER]?.set(metrikker.antallMedBeggeRoller)
	}

	private enum class RollePermutasjon {
		KOORDINATOR, VEILEDER, KOORDINATOR_OG_VEILEDER
	}
}

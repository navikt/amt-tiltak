package no.nav.amt.tiltak.tilgangskontroll_tiltaksansvarlig.metrics

import io.micrometer.core.instrument.MeterRegistry
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicInteger

private const val antallGjennomforingerMedMeldingUtenTilgang =
	"amt_tiltak_antall_gjennomforinger_med_meldinger_uten_tiltaksansvarlig_tilgang"

@Service
class TiltaksansvarligGjennomforingTilgangMetricService(
	registry: MeterRegistry,
	private val tilgangMetricRepository: TiltaksansvarligGjennomforingTilgangMetricRepository,
) {

	private val antallGjennomforingerMedMeldingUtenTilgangGauge = registry.gauge(
		antallGjennomforingerMedMeldingUtenTilgang, AtomicInteger(0)
	)


	fun oppdaterMetrikker() {
		antallGjennomforingerMedMeldingUtenTilgangGauge?.set(
			tilgangMetricRepository.antallGjennomforingerUtenTilgangerMedMeldinger()
		)
	}
}

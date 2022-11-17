package no.nav.amt.tiltak.tiltak.metrics

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.MultiGauge
import io.micrometer.core.instrument.Tags
import org.springframework.stereotype.Service
import java.util.concurrent.atomic.AtomicInteger

private const val antallGjennomforingerPerType = "amt_tiltak_gjennomforinger_per_type_antall"

@Service
class GjennomforingMetricService(
	registry: MeterRegistry,
	private val gjennomforingMetricRepository: GjennomforingMetricRepository,
) {

	private val antallGjennomforingerPerTypeGauge = MultiGauge
		.builder(antallGjennomforingerPerType)
		.baseUnit("gjennomføringer")
		.description("Antall gjennomføringer fordelt på typer, hvor det er minst en gyldig tilgang hos arrangøren.")
		.register(registry)

	private fun oppdaterAntallGjennomforingerPerTypeMetric() {
		val antallOgTyper = gjennomforingMetricRepository.antallGjennomforingerPerType()
		antallGjennomforingerPerTypeGauge.register(
			antallOgTyper.map { MultiGauge.Row.of(Tags.of("type", it.type), it.antall) }
		)
	}

	fun oppdaterMetrikker() {
		oppdaterAntallGjennomforingerPerTypeMetric()
	}


}

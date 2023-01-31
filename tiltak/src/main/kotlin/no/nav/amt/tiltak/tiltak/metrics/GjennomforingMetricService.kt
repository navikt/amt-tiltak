package no.nav.amt.tiltak.tiltak.metrics

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.MultiGauge
import io.micrometer.core.instrument.Tag
import io.micrometer.core.instrument.Tags
import org.springframework.stereotype.Service

private const val antallGjennomforingerPerType = "amt_tiltak_gjennomforinger_per_type_antall"
private const val gjennomforing = "amt_tiltak_antall_gjennomforing"

@Service
class GjennomforingMetricService(
	registry: MeterRegistry,
	private val gjennomforingMetricRepository: GjennomforingMetricRepository,
) {

	private val gjennomforingGauge = MultiGauge.builder(gjennomforing).register(registry)

	private val antallGjennomforingerPerTypeGauge = MultiGauge
		.builder(antallGjennomforingerPerType)
		.description("Antall gjennomføringer fordelt på typer, hvor det er minst en gyldig tilgang hos er arrangør.")
		.register(registry)

	private fun oppdaterAntallGjennomforingerPerTypeMetric() {
		val antallOgTyper = gjennomforingMetricRepository.antallGjennomforingerPerType()
		antallGjennomforingerPerTypeGauge.register(
			antallOgTyper.map { MultiGauge.Row.of(Tags.of(Tag.of("type", it.type)), it.antall) }
		)
	}

	fun oppdaterGjennomforingMaalinger() {
		val rows = gjennomforingMetricRepository.antallGjennomforingerGruppert().map {
			MultiGauge.Row.of(
				Tags.of(Tag.of("status", it.status), Tag.of("synligHosArrangor", it.synligHosArrangor.toString())),
				it.antall
			)
		}
		gjennomforingGauge.register(rows)
	}

	fun oppdaterMetrikker() {
		oppdaterAntallGjennomforingerPerTypeMetric()
		oppdaterGjennomforingMaalinger()
	}


}

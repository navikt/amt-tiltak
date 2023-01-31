package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.tilgang.jobs

import io.micrometer.core.instrument.MeterRegistry
import io.micrometer.core.instrument.Tags
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.tilgang.ArrangorAnsattGjennomforingTilgangRepository
import no.nav.common.job.JobRunner
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

private const val gjennomforingerPerAnsatt = "amt_tiltak_antall_gjennomforinger_pr_ansatt"

@Configuration
open class ArrangorAnsattGjennomforingTiltakStatistikkUpdater(
	private val repository: ArrangorAnsattGjennomforingTilgangRepository,
	private val registry: MeterRegistry
) {

	private val logger = LoggerFactory.getLogger(javaClass)

	private val gjennomforingerPerAnsattGauges: MutableMap<UUID, AtomicInteger> = mutableMapOf()

	@Scheduled(cron = "@hourly")
	@SchedulerLock(name = "arrangorer_med_tilgang_til_gjennomforing_updater", lockAtMostFor = "120m")
	open fun update() {
		logger.info("Henter statistikk for antall gjennomføringer per arrangør...")
		JobRunner.run("arrangorer_med_tilgang_til_gjennomforing_updater") { runner() }
	}

	private fun runner() {
		val data = repository.getAntallGjennomforingerPerAnsatt()
		data.forEach { (ansattId, numberOfGjennomforinger) ->
			val gauge = gjennomforingerPerAnsattGauges[ansattId]

			if (gauge == null) {
				gjennomforingerPerAnsattGauges[ansattId] =
					registry.gauge(
						gjennomforingerPerAnsatt,
						Tags.of("ansattId", ansattId.toString()),
						AtomicInteger(numberOfGjennomforinger)
					)
			} else {
				gauge.set(numberOfGjennomforinger)
			}
		}
	}

}

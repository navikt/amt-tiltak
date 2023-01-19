package no.nav.amt.tiltak.tilgangskontroll_tiltaksansvarlig.metrics

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.common.job.JobRunner
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled

@Configuration
open class TiltaksansvarligGjennomforingTilgangMetricJob(
	val tiltaksansvarligGjennomforingTilgangMetricService: TiltaksansvarligGjennomforingTilgangMetricService,
) {

	@Scheduled(cron = "@hourly")
	@SchedulerLock(name = "tiltaksansvarlig_gjennomforing_tilgang_metric_job", lockAtMostFor = "120m")
	open fun update() {
		JobRunner.run(
			"tiltaksansvarlig_gjennomforing_tilgang_metric_job",
			tiltaksansvarligGjennomforingTilgangMetricService::oppdaterMetrikker
		)
	}
}

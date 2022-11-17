package no.nav.amt.tiltak.tiltak.metrics

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.common.job.JobRunner
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
open class GjennomforingMetricJob(
	private val gjennomforingMetricService: GjennomforingMetricService,
) {

	/* En time etter forrige kjøring */
	@Scheduled(fixedDelay = 60 * 60 * 1000L)
	@SchedulerLock(name = "GjennomforingMetricJob", lockAtMostFor = "120m")
	open fun oppdaterMetrikker() {
		JobRunner.runAsync("oppdater_gjennomforing_metrikker", gjennomforingMetricService::oppdaterMetrikker)
	}

}

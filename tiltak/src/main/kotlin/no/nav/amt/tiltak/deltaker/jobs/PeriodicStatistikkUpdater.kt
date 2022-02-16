package no.nav.amt.tiltak.deltaker.jobs

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.amt.tiltak.deltaker.service.DeltakerStatistikkUpdater
import no.nav.common.job.JobRunner
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class PeriodicStatistikkUpdater(
	private val deltakerStatistikkUpdater: DeltakerStatistikkUpdater
) {

	/* En time etter forrige kjøring */
	@Scheduled(fixedDelay = 60 * 60 * 1000L)
	@SchedulerLock(name = "statusUpdater", lockAtMostFor = "120m")
	open fun update() {
		JobRunner.run("oppdater_deltaker_statuser", deltakerStatistikkUpdater::oppdater)
	}

}

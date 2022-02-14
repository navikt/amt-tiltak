package no.nav.amt.tiltak.deltaker.jobs

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.common.job.JobRunner
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
open class PeriodicStatusUpdater(
	private val deltakerService: DeltakerService,
) {

	/* En time etter forrige kjøring, start 1 minutt etter applikasjonen har startet */
	@Scheduled(fixedDelay = 60 * 60 * 1000L, initialDelay = 60 * 1000L)
	@SchedulerLock(name = "statusUpdater", lockAtMostFor = "120m")
	open fun update() {
		JobRunner.run("oppdater_deltaker_statuser", deltakerService::oppdaterStatuser)
	}

}

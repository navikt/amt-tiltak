package no.nav.amt.tiltak.nav_enhet

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.common.job.JobRunner
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
internal open class PeriodicNavEnhetUpdater(
	private val publiserNavEnhetService: PubliserNavEnhetService
) {

	@Scheduled(cron = "0 0 1 * * *") // Hver natt klokken 01:00
	@SchedulerLock(name = "navAnsattUpdater", lockAtMostFor = "5m")
	open fun update() {
		JobRunner.runAsync("publiser_alle_nav_enheter", publiserNavEnhetService::publiserAlleNavEnheter)
	}
}

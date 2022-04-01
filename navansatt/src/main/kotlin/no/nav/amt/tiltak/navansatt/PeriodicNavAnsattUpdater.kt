package no.nav.amt.tiltak.navansatt

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.common.job.JobRunner
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
internal open class PeriodicNavAnsattUpdater(
	private val navAnsattUpdater: NavAnsattUpdater
) {

	@Scheduled(fixedRate = 5 * 60 * 1000L)
	@SchedulerLock(name = "navAnsattUpdater", lockAtMostFor = "5m")
	open fun update() {
		JobRunner.run("oppdater_deltaker_statuser", navAnsattUpdater::oppdaterBatch)
	}
}

package no.nav.amt.tiltak.deltaker.jobs

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.amt.tiltak.core.port.DeltakerService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class PeriodicStatusUpdater(
	private val deltakerService: DeltakerService,
) {

	companion object {
		private val log = LoggerFactory.getLogger(PeriodicStatusUpdater::class.java)
	}

	/* Klokken 2 hver natt */
	@Scheduled(cron = "0 0 2 * * *")
	@SchedulerLock(name = "statusUpdater", lockAtMostFor = "120m")
	fun update() {
		try {
			deltakerService.oppdaterStatuser()
		} catch (e: Exception) {
			log.error("Feil under oppdatering av statuser", e)
		}
	}

}

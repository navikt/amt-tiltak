package no.nav.amt.tiltak.deltaker.jobs

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.amt.tiltak.core.port.DeltakerService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
open class PeriodicStatusUpdater(
	private val deltakerService: DeltakerService,
) {

	companion object {
		private val log = LoggerFactory.getLogger(PeriodicStatusUpdater::class.java)
	}

	/* En time etter forrige kjøring */
	@Scheduled(fixedDelay = 60 * 60 * 1000L)
	@SchedulerLock(name = "statusUpdater", lockAtMostFor = "120m")
	fun update() {
		try {
			log.info("Oppdaterer statuser")
			deltakerService.oppdaterStatuser()
			log.info("Statuser oppdatert")
		} catch (e: Exception) {
			log.error("Feil under oppdatering av statuser", e)
		}
	}

}

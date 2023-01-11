package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.tilgang.jobs

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.amt.tiltak.core.port.ArrangorAnsattService
import no.nav.amt.tiltak.core.port.ArrangorAnsattTilgangService
import no.nav.common.job.JobRunner
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled
import java.time.LocalDateTime

@Configuration
open class ArrangorAnsattTilgangUpdater(
	private val arrangorAnsattService: ArrangorAnsattService,
	private val arrangorAnsattTilgangService: ArrangorAnsattTilgangService,
	@Value("\${arrangoransatt.tilgang.updater.number-to-check}") private val numberToCheck: Int,
) {

	@Scheduled(cron = "@hourly")
	@SchedulerLock(name = "arrangor_ansatt_tilgang_updater", lockAtMostFor = "120m")
	open fun update() {
		JobRunner.run("arrangor_ansatt_tilgang_updater") {
			val aWeekAgo = LocalDateTime.now().minusWeeks(1)

			arrangorAnsattService.getAnsatteSistSynkronisertEldreEnn(aWeekAgo, numberToCheck)
				.forEach { arrangorAnsattTilgangService.synkroniserRettigheterMedAltinn(it.personligIdent) }
		}
	}
}

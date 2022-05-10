package no.nav.amt.tiltak.endringsmelding.metrics

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.common.job.JobRunner
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component


@Component
open class EndringsmeldingMetricJob(
	private val endringsmeldingMetricService: EndringsmeldingMetricService
) {

	/* En time etter forrige kjøring */
	@Scheduled(fixedDelay = 60 * 60 * 1000L)
	@SchedulerLock(name = "EndringsmeldingMetricJob", lockAtMostFor = "120m")
	open fun oppdaterMetrikker() {
		JobRunner.runAsync("oppdater_endringsmelding_metrikker", endringsmeldingMetricService::oppdaterMetrikker)
	}

}

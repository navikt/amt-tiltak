package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.tilgang.metrics

import no.nav.common.job.JobRunner
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
open class AnsattRolleMetricJob(
	private val ansattRolleMetricService: AnsattRolleMetricService,
) {

	@Scheduled(fixedDelay = 60 * 60 * 1000L)
	open fun oppdaterMetrikker() {
		JobRunner.run("oppdater_ansatt_rolle_metrikker", ansattRolleMetricService::oppdaterMetrikker)
	}
}

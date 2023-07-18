package no.nav.amt.tiltak.data_publisher.configuration

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.amt.tiltak.data_publisher.DataPublisherService
import no.nav.common.job.JobRunner
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled

@Configuration
open class DataPublishScheduler(
	@Value("\${publish.enabled}") private val enabled: Boolean,
	private val dataPublisherService: DataPublisherService
) {
	private val logger = LoggerFactory.getLogger(javaClass)

	@Scheduled(cron = "@midnight")
	@SchedulerLock(name = "data_publisher_job", lockAtMostFor = "120m")
	open fun publish() {
		if (enabled) {
			JobRunner.run(
				"data_publisher_job",
				dataPublisherService::publishAll
			)

		} else {
			logger.info("Datapublisering er deaktivert!")
		}

	}
}

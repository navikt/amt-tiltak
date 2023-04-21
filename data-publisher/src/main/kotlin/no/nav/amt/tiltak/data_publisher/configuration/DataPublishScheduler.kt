package no.nav.amt.tiltak.data_publisher.configuration

import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.amt.tiltak.data_publisher.DataPublisherService
import no.nav.common.job.JobRunner
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.Scheduled

@Configuration
open class DataPublishScheduler(
	private val dataPublisherService: DataPublisherService
) {

	@Scheduled(cron = "@midnight")
	@SchedulerLock(name = "data_publisher_job", lockAtMostFor = "120m")
	open fun publish() {
		JobRunner.run(
			"data_publisher_job",
			dataPublisherService::publishAll
		)
	}
}

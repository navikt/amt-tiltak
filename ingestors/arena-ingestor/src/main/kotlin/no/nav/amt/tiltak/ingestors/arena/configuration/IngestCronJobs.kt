package no.nav.amt.tiltak.ingestors.arena.configuration

import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock
import no.nav.amt.tiltak.ingestors.arena.ArenaDataProcessor
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled
import javax.annotation.PostConstruct
import javax.sql.DataSource

@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT60M")
open class IngestCronJobs(
	private val processor: ArenaDataProcessor
) {

	companion object {
		private val logger = LoggerFactory.getLogger(IngestCronJobs::class.java)
	}

	@Bean
	open fun lockProvider(datasource: DataSource): LockProvider {
		return JdbcTemplateLockProvider(datasource)
	}


	//TODO DELETEME
	@SchedulerLock(
		name = "arena-ingest",
		lockAtMostFor = "PT60M"
	)
	@PostConstruct
	open fun onStartup() {
		logger.info("Starting processing job for uningested Arena Data")
		processor.processUningestedMessages()
	}

	@Scheduled(cron = "0 * * * * *")
	@SchedulerLock(
		name = "arena-ingest",
		lockAtMostFor = "PT60M"
	)
	open fun processUningestedArenaData() {
		logger.info("Starting processing job for uningested Arena Data")
		processor.processUningestedMessages()
	}

	@Scheduled(cron = "0 0 0 * * *")
	@SchedulerLock(
		name = "arena-ingest-failed-retry",
		lockAtMostFor = "PT60M"
	)
	open fun processFailedArenaData() {
		logger.info("Starting processing job for failed Arena Data")
		processor.processFailedMessages()
	}

}

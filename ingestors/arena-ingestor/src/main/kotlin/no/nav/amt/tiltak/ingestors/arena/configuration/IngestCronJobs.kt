package no.nav.amt.tiltak.ingestors.arena.configuration

import no.nav.amt.tiltak.ingestors.arena.ArenaDataProcessor
import no.nav.common.job.leader_election.LeaderElectionClient
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.Scheduled

@Configuration
@EnableScheduling
internal open class IngestCronJobs(
	private val leaderElectionClient: LeaderElectionClient,
	private val processor: ArenaDataProcessor
) {

	companion object {
		private val logger = LoggerFactory.getLogger(IngestCronJobs::class.java)
	}

	@Scheduled(cron = "0 * * * * *")
	open fun processUningestedArenaData() {
		if (leaderElectionClient.isLeader) {
			logger.debug("Starting processing job for uningested Arena Data...")
			processor.processUningestedMessages()
			logger.debug("Finished processing job for uningested Arena Data!")
		}
	}

	@Scheduled(cron = "0 0 0 * * *")
	open fun processFailedArenaData() {
		if (leaderElectionClient.isLeader) {
			logger.debug("Starting processing job for failed Arena Data...")
			processor.processFailedMessages()
			logger.debug("Finished processing job for failed Arena Data!")
		}
	}

}

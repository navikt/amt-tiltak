package no.nav.amt.tiltak.application

import net.javacrumbs.shedlock.core.LockProvider
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider
import no.nav.common.job.leader_election.LeaderElectionClient
import no.nav.common.job.leader_election.ShedLockLeaderElectionClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.jdbc.core.JdbcTemplate

@Configuration
open class LeaderElectionConfig {

	@Bean
	open fun leaderElectionClient(lockProvider: LockProvider): LeaderElectionClient {
		return ShedLockLeaderElectionClient(lockProvider)
	}

	@Bean
	open fun lockProvider(jdbcTemplate: JdbcTemplate): LockProvider {
		return JdbcTemplateLockProvider(
			JdbcTemplateLockProvider.Configuration.builder()
				.withJdbcTemplate(jdbcTemplate)
				.usingDbTime()
				.build()
		)
	}

}

package no.nav.amt.tiltak

import no.nav.amt.tiltak.test.database.DbTestDataUtils
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Configuration
import org.springframework.context.event.ContextRefreshedEvent
import org.springframework.context.event.EventListener
import javax.sql.DataSource

@Configuration
open class DbSeedConfig(
	private val dataSource: DataSource
) {

	private val log = LoggerFactory.getLogger(this::class.java)

	@EventListener
	open fun onApplicationEvent(_event: ContextRefreshedEvent?) {
		log.info("Seeding database with test data...")
		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

}

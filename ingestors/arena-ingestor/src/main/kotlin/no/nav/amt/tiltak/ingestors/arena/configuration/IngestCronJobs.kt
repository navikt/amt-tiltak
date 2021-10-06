package no.nav.amt.tiltak.ingestors.arena.configuration

import no.nav.amt.tiltak.ingestors.arena.ArenaDataProcessor
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling

@Configuration
@EnableScheduling
open class IngestCronJobs(
    private val processor: ArenaDataProcessor
) {

    // TODO Schedule locked (https://www.baeldung.com/shedlock-spring)

    // TODO Every (minute?)
    fun processUningestedArenaData() {
        processor.processUningestedMessages()
    }

    // TODO Run midnight
    fun processAllUningestedArenaData() {
        processor.processFailedMessages()
    }

}

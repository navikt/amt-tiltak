package no.nav.amt.tiltak.ingestors.arena.repository

import no.nav.amt.tiltak.ingestors.arena.domain.ArenaData
import org.springframework.stereotype.Repository

@Repository
class ArenaDataRepository {

    fun insert(arenaData: ArenaData) {
        TODO("INSERT Arenadata")
    }

    fun getUningestedData(offset: Int = 0, limit: Int = 100): List<ArenaData> {
        TODO("Get ingest_status == NEW || RETRY, ordered by pos limit == limit. Paginering?")
    }

    fun getFailedData(offset: Int = 0, limit: Int = 100): List<ArenaData> {
        TODO("Get ingest_status == FAILED, ordered by pos limit == limit")
    }

    fun markAsIngested(id: Int) {
        TODO("Set ingest_status = INGESTED, ingested_timestamp = CURRENT_TIMESTAMP")
    }

    fun incrementRetry(id: Int, currentRetries: Int) {
        TODO("increment retry + set ingest_status ==RETRY")
    }

    fun markAsFailed(id: Int) {
        TODO("Set ingest_status = FAILED")
    }

}

package no.nav.amt.tiltak.ingestors.arena.processors

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.amt.tiltak.ingestors.arena.domain.ArenaData
import no.nav.amt.tiltak.ingestors.arena.domain.OperationType
import no.nav.amt.tiltak.ingestors.arena.repository.ArenaDataRepository

abstract class AbstractArenaProcessor(
    private val repository: ArenaDataRepository
) {

    companion object {
        private val MAX_INGEST_ATTEMPTS = 10
    }

    fun handle(data: ArenaData) {
        try {
            when (data.operationType) {
                OperationType.INSERT -> {
                    insert(data)
                    repository.markAsIngested(data.id)
                }
                OperationType.UPDATE -> {
                    update(data)
                    repository.markAsIngested(data.id)
                }
                OperationType.DELETE -> {
                    delete(data)
                    repository.markAsIngested(data.id)
                }
            }
        } catch (e: Exception) {
            if (data.ingestAttempts >= MAX_INGEST_ATTEMPTS) {
                repository.setFailed(data, e.message, e)
            } else {
            	repository.setRetry(data)
			}
        }
    }

    protected abstract fun insert(data: ArenaData)
    protected abstract fun update(data: ArenaData)
    protected abstract fun delete(data: ArenaData)

    protected fun <T> jsonObject(string: String?, clazz: Class<T>): T {
        if (string == null) {
            throw UnsupportedOperationException("Expected ${clazz.simpleName} not to be null!")
        }

        return ObjectMapper().readValue(string, clazz)
    }

}

package no.nav.amt.tiltak.ingestors.arena_acl_ingestor.processors

import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.MessageWrapper
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.Operation

abstract class GenericProcessor<T> {

	fun processMessage(message: MessageWrapper<T>) {
		when (message.operation) {
			Operation.CREATED -> processInsertMessage(message)
			Operation.MODIFIED -> processModifyMessage(message)
			Operation.DELETED -> processDeleteMessage(message)
		}
	}

	protected abstract fun processInsertMessage(message: MessageWrapper<T>)

	protected abstract fun processModifyMessage(message: MessageWrapper<T>)

	protected abstract fun processDeleteMessage(message: MessageWrapper<T>)

}

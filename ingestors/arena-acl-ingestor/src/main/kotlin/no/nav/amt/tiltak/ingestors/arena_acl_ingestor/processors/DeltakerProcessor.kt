package no.nav.amt.tiltak.ingestors.arena_acl_ingestor.processors

import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.Deltaker
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.MessageWrapper
import org.springframework.stereotype.Service

@Service
class DeltakerProcessor : GenericProcessor<Deltaker>() {

	override fun processInsertMessage(message: MessageWrapper<Deltaker>) {
		TODO("Not yet implemented")
	}

	override fun processModifyMessage(message: MessageWrapper<Deltaker>) {
		TODO("Not yet implemented")
	}

	override fun processDeleteMessage(message: MessageWrapper<Deltaker>) {
		TODO("Not yet implemented")
	}

}

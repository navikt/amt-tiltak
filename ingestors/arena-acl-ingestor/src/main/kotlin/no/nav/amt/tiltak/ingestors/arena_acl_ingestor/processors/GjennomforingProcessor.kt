package no.nav.amt.tiltak.ingestors.arena_acl_ingestor.processors

import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.Gjennomforing
import no.nav.amt.tiltak.ingestors.arena_acl_ingestor.dto.MessageWrapper
import org.springframework.stereotype.Service

@Service
class GjennomforingProcessor : GenericProcessor<Gjennomforing>() {

	override fun processInsertMessage(message: MessageWrapper<Gjennomforing>) {
		TODO("Not yet implemented")
	}

	override fun processModifyMessage(message: MessageWrapper<Gjennomforing>) {
		TODO("Not yet implemented")
	}

	override fun processDeleteMessage(message: MessageWrapper<Gjennomforing>) {
		TODO("Not yet implemented")
	}

}

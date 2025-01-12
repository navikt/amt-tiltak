package no.nav.amt.tiltak.deltaker.service

import java.util.UUID
import no.nav.amt.tiltak.core.domain.tiltak.VurderingDbo
import no.nav.amt.tiltak.core.port.VurderingService
import no.nav.amt.tiltak.deltaker.repositories.VurderingRepository
import org.springframework.stereotype.Service

@Service
class VurderingServiceImpl(
	private val vurderingRepository: VurderingRepository
) : VurderingService {
	override fun hentAktiveVurderingerForGjennomforing(gjennomforingId: UUID): List<VurderingDbo> {
		return vurderingRepository.getAktiveByGjennomforing(gjennomforingId)
	}
}

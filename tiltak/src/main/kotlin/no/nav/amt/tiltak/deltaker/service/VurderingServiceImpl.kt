package no.nav.amt.tiltak.deltaker.service

import no.nav.amt.tiltak.core.domain.tiltak.Vurdering
import no.nav.amt.tiltak.core.port.VurderingService
import no.nav.amt.tiltak.deltaker.repositories.VurderingRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class VurderingServiceImpl(
	private val vurderingRepository: VurderingRepository
) : VurderingService {
	override fun hentAktiveVurderingerForGjennomforing(gjennomforingId: UUID): List<Vurdering> {
		return vurderingRepository.getAktiveByGjennomforing(gjennomforingId)
	}
}

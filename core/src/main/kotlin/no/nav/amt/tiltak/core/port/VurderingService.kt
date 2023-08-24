package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.Vurdering
import java.util.UUID

interface VurderingService {
	fun hentAktiveVurderingerForGjennomforing(gjennomforingId: UUID): List<Vurdering>
}

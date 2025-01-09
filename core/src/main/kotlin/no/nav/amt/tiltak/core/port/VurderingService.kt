package no.nav.amt.tiltak.core.port

import java.util.UUID
import no.nav.amt.tiltak.core.domain.tiltak.VurderingDbo

interface VurderingService {
	fun hentAktiveVurderingerForGjennomforing(gjennomforingId: UUID): List<VurderingDbo>
}

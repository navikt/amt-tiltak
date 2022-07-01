package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import java.util.*

interface GjennomforingService {

	fun getGjennomforing(id: UUID): Gjennomforing

	fun getGjennomforinger(gjennomforingIder: List<UUID>): List<Gjennomforing>

	fun getKoordinatorerForGjennomforing(gjennomforingId: UUID): List<String>

	fun getKoordinatorerForGjennomforinger(gjennomforingIder: List<UUID>): Map<UUID, List<String>>

	fun upsert(gjennomforing: Gjennomforing): Gjennomforing

	fun slettGjennomforing(gjennomforingId: UUID)

}

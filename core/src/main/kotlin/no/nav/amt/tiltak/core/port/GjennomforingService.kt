package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import java.util.*

interface GjennomforingService {

	fun getGjennomforing(id: UUID): Gjennomforing

	fun getGjennomforinger(gjennomforingIder: List<UUID>): List<Gjennomforing>

	fun getKoordinatorerForGjennomforing(gjennomforingId: UUID): Set<Person>

	fun getKoordinatorerForGjennomforinger(gjennomforingIder: List<UUID>): Map<UUID, Set<Person>>

	fun getByArrangorId(arrangorId: UUID): List<Gjennomforing>

	fun upsert(gjennomforing: Gjennomforing): Gjennomforing

	fun slettGjennomforing(gjennomforingId: UUID)

}

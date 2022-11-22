package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import java.util.*

interface GjennomforingService {

	fun getGjennomforing(id: UUID): Gjennomforing

	fun getGjennomforinger(gjennomforingIder: List<UUID>): List<Gjennomforing>

	fun getByArrangorId(arrangorId: UUID): List<Gjennomforing>

	fun getAktiveByLopenr(lopenr: Int): List<Gjennomforing>

	fun upsert(gjennomforing: Gjennomforing): Gjennomforing

	fun slettGjennomforing(gjennomforingId: UUID)

}

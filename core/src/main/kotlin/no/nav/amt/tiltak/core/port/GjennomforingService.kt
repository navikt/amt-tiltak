package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.domain.tiltak.GjennomforingUpsert
import java.util.*

interface GjennomforingService {

	fun getGjennomforing(id: UUID): Gjennomforing

	fun getGjennomforingOrNull(id: UUID): Gjennomforing?

	fun getGjennomforinger(gjennomforingIder: List<UUID>): List<Gjennomforing>

	fun getByArrangorId(arrangorId: UUID): List<Gjennomforing>

	fun getByLopenr(lopenr: Int): List<Gjennomforing>

	fun upsert(gjennomforing: GjennomforingUpsert)

	fun slettGjennomforing(gjennomforingId: UUID)

	fun getArrangorId(gjennomforingId: UUID): UUID
}

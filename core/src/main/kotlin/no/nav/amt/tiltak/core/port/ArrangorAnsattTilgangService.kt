package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRoller
import java.util.*

interface ArrangorAnsattTilgangService {

	fun verifiserTilgangTilGjennomforing(ansattPersonligIdent: String, gjennomforingId: UUID)

	fun verifiserTilgangTilGjennomforing(ansattId: UUID, gjennomforingId: UUID)

	fun verifiserTilgangTilArrangor(ansattPersonligIdent: String, arrangorId: UUID)

	fun verifiserTilgangTilArrangor(ansattId: UUID, arrangorId: UUID)

	fun verifiserTilgangTilDeltaker(ansattPersonligIdent: String, deltakerId: UUID)

	fun verifiserTilgangTilDeltaker(ansattId: UUID, deltakerId: UUID)

	fun hentAnsattTilganger(ansattId: UUID): List<ArrangorAnsattRoller>

	fun hentGjennomforingIder(ansattPersonligIdent: String): List<UUID>

	fun hentAnsattId(ansattPersonligIdent: String): UUID

	fun opprettTilgang(ansattPersonligIdent: String, gjennomforingId: UUID)

	fun fjernTilgang(ansattPersonligIdent: String, gjennomforingId: UUID)

	fun synkroniserRettigheterMedAltinn(ansattPersonligIdent: String)

}

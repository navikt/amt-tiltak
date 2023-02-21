package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRoller
import java.util.*

interface ArrangorAnsattTilgangService {

	fun verifiserTilgangTilGjennomforing(
		ansattPersonligIdent: String,
		gjennomforingId: UUID,
		rolle: ArrangorAnsattRolle
	)

	fun verifiserTilgangTilGjennomforing(ansattId: UUID, gjennomforingId: UUID, rolle: ArrangorAnsattRolle)

	fun verifiserTilgangTilArrangor(ansattPersonligIdent: String, arrangorId: UUID, rolle: ArrangorAnsattRolle)

	fun verifiserTilgangTilArrangor(ansattId: UUID, arrangorId: UUID, rolle: ArrangorAnsattRolle)

	fun verifiserTilgangTilDeltaker(ansattPersonligIdent: String, deltakerId: UUID, rolle: ArrangorAnsattRolle)

	fun verifiserTilgangTilDeltaker(ansattId: UUID, deltakerId: UUID, rolle: ArrangorAnsattRolle)

	fun hentAnsattTilganger(ansattId: UUID): List<ArrangorAnsattRoller>

	fun hentRollerForAnsattTilknyttetDeltaker(ansattId: UUID, deltakerId: UUID): List<ArrangorAnsattRolle>

	fun hentGjennomforingIder(ansattPersonligIdent: String): List<UUID>

	fun hentAnsattId(ansattPersonligIdent: String): UUID

	fun opprettTilgang(ansattPersonligIdent: String, gjennomforingId: UUID)

	fun fjernTilgang(ansattPersonligIdent: String, gjennomforingId: UUID)

	fun synkroniserRettigheterMedAltinn(ansattPersonligIdent: String)

	fun shouldHaveRolle(personligIdent: String, rolle: ArrangorAnsattRolle)

	fun verifiserAnsatteHarRolleHosArrangorer(
		ansattIder: List<UUID>,
		arrangorIder: List<UUID>,
		rolle: ArrangorAnsattRolle
	)
}

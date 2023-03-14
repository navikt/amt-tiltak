package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRoller
import java.util.*

interface ArrangorAnsattTilgangService {

	fun verifiserTilgangTilGjennomforing(ansattId: UUID, gjennomforingId: UUID)

	fun verifiserTilgangTilDeltaker(ansattId: UUID, deltakerId: UUID)

	fun hentAnsattTilganger(ansattId: UUID): List<ArrangorAnsattRoller>

	fun synkroniserRettigheterMedAltinn(ansattPersonligIdent: String)

	fun verifiserRolleHosArrangor(ansattId: UUID, arrangorId: UUID, rolle: ArrangorAnsattRolle)

	fun harRolleHosArrangor(ansattId: UUID, arrangorId: UUID, rolle: ArrangorAnsattRolle): Boolean

	fun verifiserHarRolleAnywhere(ansattId: UUID, rolle: ArrangorAnsattRolle)
}

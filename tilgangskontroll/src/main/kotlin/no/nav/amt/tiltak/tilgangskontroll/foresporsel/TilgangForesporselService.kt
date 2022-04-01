package no.nav.amt.tiltak.tilgangskontroll.foresporsel

import no.nav.amt.tiltak.core.port.ArrangorAnsattService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.tilgangskontroll.tilgang.AnsattRolle
import no.nav.amt.tiltak.tilgangskontroll.tilgang.AnsattRolleService
import no.nav.amt.tiltak.tilgangskontroll.tilgang.GjennomforingTilgangService
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import java.util.*

@Service
class TilgangForesporselService(
	private val tilgangForesporselRepository: TilgangForesporselRepository,
	private val transactionTemplate: TransactionTemplate,
	private val gjennomforingTilgangService: GjennomforingTilgangService,
	private val arrangorAnsattService: ArrangorAnsattService,
	private val gjennomforingService: GjennomforingService,
	private val ansattRolleService: AnsattRolleService,
) {

	fun hentUbesluttedeForesporsler(gjennomforingId: UUID): List<TilgangForesporselDbo> {
		return tilgangForesporselRepository.hentUbesluttedeForesporsler(gjennomforingId)
	}

	fun opprettForesporsel(opprettForesporselCmd: OpprettForesporselCmd) {
		tilgangForesporselRepository.opprettForesporsel(opprettForesporselCmd)
	}

	fun godkjennForesporsel(foresporselId: UUID, beslutningAvNavAnsattId: UUID) {
		val foresporsel = tilgangForesporselRepository.hentForesporsel(foresporselId)

		val nyGjennomforingTilgangId = UUID.randomUUID()

		val gjennomforing = gjennomforingService.getGjennomforing(foresporsel.gjennomforingId)

		transactionTemplate.executeWithoutResult {
			val nyAnsatt = arrangorAnsattService.opprettAnsattHvisIkkeFinnes(foresporsel.personligIdent)

			ansattRolleService.opprettRolleHvisIkkeFinnes(nyAnsatt.id, gjennomforing.arrangor.id, AnsattRolle.KOORDINATOR)

			gjennomforingTilgangService.opprettTilgang(
				id = nyGjennomforingTilgangId,
				ansattId = nyAnsatt.id,
				gjennomforingId = foresporsel.gjennomforingId
			)

			tilgangForesporselRepository.godkjennForesporsel(
				foresporselId = foresporselId,
				beslutningAvNavAnsattId = beslutningAvNavAnsattId,
				gjennomforingTilgangId = nyGjennomforingTilgangId
			)
		}
	}

	fun avvisForesporsel(foresporselId: UUID, avvistAvNavAnsattId: UUID) {
		tilgangForesporselRepository.avvisForesporsel(foresporselId, avvistAvNavAnsattId)
	}

}

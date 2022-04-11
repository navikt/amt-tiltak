package no.nav.amt.tiltak.tilgangskontroll.invitasjon

import no.nav.amt.tiltak.core.port.PersonService
import no.nav.amt.tiltak.tilgangskontroll.foresporsel.OpprettForesporselInput
import no.nav.amt.tiltak.tilgangskontroll.foresporsel.TilgangForesporselService
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.transaction.support.TransactionTemplate
import org.springframework.web.server.ResponseStatusException
import java.time.ZonedDateTime
import java.util.*

@Service
open class TilgangInvitasjonService(
	private val hentInvitasjonInfoQuery: HentInvitasjonInfoQuery,
	private val hentUbrukteInvitasjonerQuery: HentUbrukteInvitasjonerQuery,
	private val tilgangInvitasjonRepository: TilgangInvitasjonRepository,
	private val transactionTemplate: TransactionTemplate,
	private val tilgangForesporselService: TilgangForesporselService,
	private val personService: PersonService,
) {

	open fun hentInvitasjonInfo(invitasjonId: UUID): InvitasjonInfoDbo {
		return hentInvitasjonInfoQuery.query(invitasjonId)
	}

	open fun hentUbrukteInvitasjoner(gjennomforingId: UUID): List<UbruktInvitasjonDbo> {
		return hentUbrukteInvitasjonerQuery.query(gjennomforingId)
	}

	open fun opprettInvitasjon(gjennomforingId: UUID, opprettetAvNavAnsattId: UUID) {
		val nyInvitasjonId = UUID.randomUUID()
		val invitasjonGyldigTil = ZonedDateTime.now().plusDays(5)

		tilgangInvitasjonRepository.opprettInvitasjon(
			id = nyInvitasjonId,
			gjennomforingId = gjennomforingId,
			opprettetAvNavAnsattId = opprettetAvNavAnsattId,
			gyldigTil = invitasjonGyldigTil
		)
	}

	open fun aksepterInvitasjon(invitasjonId: UUID, arrangorAnsattPersonligIdent: String) {
		val invitasjon = tilgangInvitasjonRepository.get(invitasjonId)

		if (invitasjon.erBrukt) {
			// Bruk heller custom exception og map med controller advice
			throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Kan ikke akseptere invitasjon som er brukt")
		}

		if (invitasjon.gyldigTil.isAfter(ZonedDateTime.now())) {
			// Bruk heller custom exception og map med controller advice
			throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Kan ikke akseptere invitasjon som er utgått")
		}

		val person = personService.hentPerson(arrangorAnsattPersonligIdent)

		val foresporselId = UUID.randomUUID()

		transactionTemplate.executeWithoutResult {
			tilgangInvitasjonRepository.settTilBrukt(invitasjonId, foresporselId)

			tilgangForesporselService.opprettForesporsel(
				OpprettForesporselInput(
					id = foresporselId,
					personligIdent = arrangorAnsattPersonligIdent,
					fornavn = person.fornavn,
					mellomnavn = person.mellomnavn,
					etternavn = person.etternavn,
					gjennomforingId = invitasjon.gjennomforingId,
				)
			)
		}
	}

	open fun slettInvitasjon(invitasjonId: UUID) {
		val invitasjon = tilgangInvitasjonRepository.get(invitasjonId)

		if (invitasjon.erBrukt) {
			// Bruk heller custom exception og map med controller advice
			throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Kan ikke slette invitasjon som er brukt")
		}

		tilgangInvitasjonRepository.slettInvitasjon(invitasjonId)
	}

}

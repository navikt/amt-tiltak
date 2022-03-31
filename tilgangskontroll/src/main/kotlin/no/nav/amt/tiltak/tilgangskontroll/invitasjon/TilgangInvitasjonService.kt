package no.nav.amt.tiltak.tilgangskontroll.invitasjon

import org.springframework.stereotype.Service
import java.time.ZonedDateTime
import java.util.*

@Service
class TilgangInvitasjonService(
	private val tilgangInvitasjonRepository: TilgangInvitasjonRepository
) {

	fun hentUbrukteInvitasjoner(gjennomforingId: UUID): List<TilgangInvitasjonDbo> {
		return tilgangInvitasjonRepository.hentUbrukteInvitasjoner(gjennomforingId)
	}

	fun opprettInvitasjon(gjennomforingId: UUID, opprettetAvNavAnsattId: UUID) {
		val nyInvitasjonId = UUID.randomUUID()
		val invitasjonGyldigTil = ZonedDateTime.now().plusDays(5)

		tilgangInvitasjonRepository.opprettInvitasjon(
			id = nyInvitasjonId,
			gjennomforingId = gjennomforingId,
			opprettetAvNavAnsattId = opprettetAvNavAnsattId,
			gydligTil = invitasjonGyldigTil
		)
	}

	fun aksepterInvitasjon(invitasjonId: UUID, arrangorAnsattPersonligIdent: String) {

	}

	fun avbrytInvitasjon(invitasjonId: UUID) {
		tilgangInvitasjonRepository.avbrytInvitasjon(invitasjonId)
	}

}

package no.nav.amt.tiltak.deltaker.service

import no.nav.amt.tiltak.clients.amt_person.AmtPersonClient
import no.nav.amt.tiltak.core.domain.tiltak.Bruker
import no.nav.amt.tiltak.core.port.BrukerService
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.core.port.NavEnhetService
import no.nav.amt.tiltak.deltaker.repositories.BrukerRepository
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class BrukerServiceImpl(
	private val brukerRepository: BrukerRepository,
	private val navEnhetService: NavEnhetService,
	private val navAnsattService: NavAnsattService,
	private val amtPersonClient: AmtPersonClient,
) : BrukerService {

	override fun slettBruker(id: UUID) {
		brukerRepository.slettBruker(id)
	}

	override fun slettBruker(personIdent: String) {
		brukerRepository.slettBruker(personIdent)
	}

	override fun getIdOrCreate(fodselsnummer: String): UUID {
		return brukerRepository.get(fodselsnummer)?.id ?: createBruker(fodselsnummer)
	}

	override fun upsert(bruker: Bruker) {
		brukerRepository.upsert(bruker)
	}

	private fun createBruker(personident: String): UUID {
		val navBruker = amtPersonClient.hentNavBruker(personident).getOrThrow()

		navBruker.navEnhet?.let { navEnhetService.upsert(it) }
		navBruker.navVeilederId?.let { navAnsattService.opprettNavAnsattHvisIkkeFinnes(it) }

		brukerRepository.upsert(navBruker.toBruker())

		return navBruker.personId
	}

}

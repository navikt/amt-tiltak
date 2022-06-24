package no.nav.amt.tiltak.tiltak.services

import no.nav.amt.tiltak.core.domain.tiltak.Bruker
import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet
import no.nav.amt.tiltak.core.port.BrukerService
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.core.port.NavEnhetService
import no.nav.amt.tiltak.core.port.PersonService
import no.nav.amt.tiltak.deltaker.dbo.BrukerDbo
import no.nav.amt.tiltak.deltaker.dbo.BrukerInsertDbo
import no.nav.amt.tiltak.deltaker.repositories.BrukerRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class BrukerServiceImpl(
	private val brukerRepository: BrukerRepository,
	private val personService: PersonService,
	private val navAnsattService: NavAnsattService,
	private val navEnhetService: NavEnhetService
) : BrukerService {

	override fun getBruker(fodselsnummer: String): Bruker? {
		return brukerRepository.get(fodselsnummer)?.let {
			val navEnhet = it.navEnhetId?.let(navEnhetService::getNavEnhet)
			it.toBruker(navEnhet)
		}
	}

	override fun getOrCreate(fodselsnummer: String): UUID {
		val bruker = brukerRepository.get(fodselsnummer) ?: createBruker(fodselsnummer)
		return bruker.id
	}

	override fun finnesBruker(fodselsnummer: String): Boolean {
		return brukerRepository.get(fodselsnummer) != null
	}

	override fun oppdaterAnsvarligVeileder(fodselsnummer: String, navAnsattId: UUID) {
		brukerRepository.oppdaterVeileder(fodselsnummer, navAnsattId)
	}

	override fun oppdaterNavEnhet(fodselsnummer: String, navEnhet: NavEnhet?) {
		brukerRepository.oppdaterNavEnhet(fodselsnummer, navEnhet?.id)
	}

	private fun createBruker(fodselsnummer: String): BrukerDbo {
		val tildeltVeilederNavIdent = personService.hentTildeltVeilederNavIdent(fodselsnummer)

		val veileder = tildeltVeilederNavIdent?.let { navAnsattService.getNavAnsatt(it) }

		val navEnhet = navEnhetService.getNavEnhetForBruker(fodselsnummer)

		val personKontaktinformasjon = personService.hentPersonKontaktinformasjon(fodselsnummer)

		val person = personService.hentPerson(fodselsnummer)

		val bruker = BrukerInsertDbo(
			fodselsnummer = fodselsnummer,
			fornavn = person.fornavn,
			mellomnavn = person.mellomnavn,
			etternavn = person.etternavn,
			telefonnummer = person.telefonnummer ?: personKontaktinformasjon.telefonnummer,
			epost = personKontaktinformasjon.epost,
			ansvarligVeilederId = veileder?.id,
			navEnhetId = navEnhet?.id
		)

		return brukerRepository.insert(bruker)
	}

}

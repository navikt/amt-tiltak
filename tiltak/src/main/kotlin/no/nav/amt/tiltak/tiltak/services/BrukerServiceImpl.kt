package no.nav.amt.tiltak.tiltak.services

import no.nav.amt.tiltak.core.port.BrukerService
import no.nav.amt.tiltak.core.port.NavKontorService
import no.nav.amt.tiltak.core.port.PersonService
import no.nav.amt.tiltak.core.port.VeilederService
import no.nav.amt.tiltak.deltaker.dbo.BrukerDbo
import no.nav.amt.tiltak.deltaker.dbo.BrukerInsertDbo
import no.nav.amt.tiltak.deltaker.dbo.NavKontorDbo
import no.nav.amt.tiltak.deltaker.repositories.BrukerRepository
import no.nav.amt.tiltak.deltaker.repositories.NavKontorRepository
import org.springframework.stereotype.Service
import java.util.*

@Service
class BrukerServiceImpl(
	private val brukerRepository: BrukerRepository,
	private val navKontorRepository: NavKontorRepository,
	private val navKontorService: NavKontorService,
	private val personService: PersonService,
	private val veilederService: VeilederService
) : BrukerService {

	override fun getOrCreate(fodselsnummer: String): UUID {
		val bruker = brukerRepository.get(fodselsnummer) ?: createBruker(fodselsnummer)
		return bruker.id
	}

	override fun finnesBruker(fodselsnummer: String): Boolean {
		return brukerRepository.get(fodselsnummer) != null
	}

	override fun oppdaterAnsvarligVeileder(brukerPersonligIdent: String, veilederId: UUID) {
		brukerRepository.oppdaterVeileder(brukerPersonligIdent, veilederId)
	}

	private fun createBruker(fodselsnummer: String): BrukerDbo {
		val veilederId = upsertVeileder(fodselsnummer)

		val navKontor = getNavKontor(fodselsnummer)

		val personKontaktinformasjon = personService.hentPersonKontaktinformasjon(fodselsnummer)

		val person = personService.hentPerson(fodselsnummer)

		val bruker = BrukerInsertDbo(
			fodselsnummer = fodselsnummer,
			fornavn = person.fornavn,
			mellomnavn = person.mellomnavn,
			etternavn = person.etternavn,
			telefonnummer = person.telefonnummer ?: personKontaktinformasjon.telefonnummer,
			epost = personKontaktinformasjon.epost,
			ansvarligVeilederId = veilederId,
			navKontorId = navKontor?.id
		)

		return brukerRepository.insert(bruker)
	}

	private fun upsertVeileder(fodselsnummer: String): UUID? {
		return personService.hentTildeltVeileder(fodselsnummer)?.let { veileder ->
			return veilederService.upsertVeileder(veileder)
		}
	}

	private fun getNavKontor(fodselsnummer: String): NavKontorDbo? {
		return navKontorService.hentNavKontorForBruker(fodselsnummer)?.let {
			navKontorRepository.upsert(it.enhetId, it.navn)
		}
	}
}

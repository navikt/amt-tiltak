package no.nav.amt.tiltak.deltaker.service

import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.amt.tiltak.core.port.NavEnhetService
import no.nav.amt.tiltak.core.port.PersonService
import no.nav.amt.tiltak.deltaker.dbo.BrukerDbo
import no.nav.amt.tiltak.deltaker.dbo.BrukerUpsertDbo
import no.nav.amt.tiltak.deltaker.repositories.BrukerRepository
import no.nav.amt.tiltak.log.SecureLog.secureLog
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class BrukerService(
	private val brukerRepository: BrukerRepository,
	private val personService: PersonService,
	private val navAnsattService: NavAnsattService,
	private val navEnhetService: NavEnhetService,
) {

	private val log = LoggerFactory.getLogger(javaClass)

	fun getOrCreate(fodselsnummer: String): UUID {
		val bruker = brukerRepository.get(fodselsnummer) ?: createBruker(fodselsnummer)
		return bruker.id
	}

	fun finnesBruker(fodselsnummer: String): Boolean {
		return brukerRepository.get(fodselsnummer) != null
	}

	fun oppdaterAnsvarligVeileder(fodselsnummer: String, navAnsattId: UUID) {
		brukerRepository.oppdaterVeileder(fodselsnummer, navAnsattId)
	}

	fun oppdaterNavEnhet(fodselsnummer: String, navEnhet: NavEnhet?) {
		val bruker = brukerRepository.get(fodselsnummer)
			?: throw IllegalStateException("Kan ikke oppdatere nav enhet. Fant ikke bruker")
		if (bruker.navEnhetId == navEnhet?.id) return
		brukerRepository.oppdaterNavEnhet(fodselsnummer, navEnhet?.id)
	}

	fun settErSkjermet(personIdent: String, erSkjermet: Boolean) {
		val erBrukerSkjermet = erSkjermet(personIdent)
		if (erSkjermet == erBrukerSkjermet) return
		brukerRepository.settSkjermet(personIdent, erSkjermet)
	}

	fun erSkjermet(personIdent: String): Boolean {
		val bruker = brukerRepository.get(personIdent)

		if (bruker == null) {
			secureLog.warn("Kan ikke sjekke om bruker er skjermet. Fant ikke bruker med personIdent=$personIdent")
			throw IllegalStateException("Kan ikke sjekke om bruker er skjermet. Fant ikke bruker")
		}

		return bruker.erSkjermet
	}

	fun logSkjermedeBrukere() {
		var offset = 0
		var brukere: List<BrukerDbo>
		log.info("--- Logger Brukere START ---")
		do {
			brukere = brukerRepository.getBrukere(offset)

			brukere.forEach { bruker ->
				val person = personService.hentPerson(bruker.personIdent)

				if(person.diskresjonskode != null) {
					log.info("BrukerId har diskresjonskode: ${bruker.id}")
				}
			}

			log.info("Sjekket brukere mellom $offset og ${offset+brukere.size}")
			offset += brukere.size
		} while (brukere.isNotEmpty())
		log.info("---- Logger Brukere END ----")
	}

	private fun createBruker(fodselsnummer: String): BrukerDbo {
		val tildeltVeilederNavIdent = personService.hentTildeltVeilederNavIdent(fodselsnummer)

		val veileder = tildeltVeilederNavIdent?.let { navAnsattService.getNavAnsatt(it) }

		val navEnhet = navEnhetService.getNavEnhetForBruker(fodselsnummer)

		val personKontaktinformasjon = personService.hentPersonKontaktinformasjon(fodselsnummer)

		val person = personService.hentPerson(fodselsnummer)

		val erSkjermet = personService.erSkjermet(fodselsnummer)

		val bruker = BrukerUpsertDbo(
			personIdent = fodselsnummer,
			fornavn = person.fornavn,
			mellomnavn = person.mellomnavn,
			etternavn = person.etternavn,
			telefonnummer = person.telefonnummer ?: personKontaktinformasjon.telefonnummer,
			epost = personKontaktinformasjon.epost,
			ansvarligVeilederId = veileder?.id,
			navEnhetId = navEnhet?.id,
			erSkjermet = erSkjermet
		)

		return brukerRepository.upsert(bruker)
	}
}

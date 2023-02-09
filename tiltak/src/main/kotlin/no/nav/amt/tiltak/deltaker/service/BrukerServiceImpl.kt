package no.nav.amt.tiltak.deltaker.service

import no.nav.amt.tiltak.core.domain.tiltak.NavEnhet
import no.nav.amt.tiltak.core.port.BrukerService
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
class BrukerServiceImpl(
	private val brukerRepository: BrukerRepository,
	private val personService: PersonService,
	private val navAnsattService: NavAnsattService,
	private val navEnhetService: NavEnhetService,
) : BrukerService {

	private val log = LoggerFactory.getLogger(javaClass)

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
		val bruker = brukerRepository.get(fodselsnummer)
			?: throw IllegalStateException("Kan ikke oppdatere nav enhet. Fant ikke bruker")
		if (bruker.navEnhetId == navEnhet?.id) return
		brukerRepository.oppdaterNavEnhet(fodselsnummer, navEnhet?.id)
	}

	override fun settErSkjermet(personIdent: String, erSkjermet: Boolean) {
		val erBrukerSkjermet = erSkjermet(personIdent)
		if (erSkjermet == erBrukerSkjermet) return
		brukerRepository.settSkjermet(personIdent, erSkjermet)
	}

	override fun erSkjermet(personIdent: String): Boolean {
		val bruker = brukerRepository.get(personIdent)

		if (bruker == null) {
			secureLog.warn("Kan ikke sjekke om bruker er skjermet. Fant ikke bruker med personIdent=$personIdent")
			throw IllegalStateException("Kan ikke sjekke om bruker er skjermet. Fant ikke bruker")
		}

		return bruker.erSkjermet
	}

	override fun updateBrukerByPersonIdent(personIdent: String, fornavn: String, mellomnavn: String?, etternavn: String) {
		val bruker = brukerRepository.get(personIdent)

		if(bruker != null) {
			val kontaktinformasjon = personService.hentPersonKontaktinformasjon(bruker.personIdent)

			val newBruker = bruker.copy(
				fornavn = fornavn,
				mellomnavn = mellomnavn,
				etternavn = etternavn,
				telefonnummer = kontaktinformasjon.telefonnummer,
				epost = kontaktinformasjon.epost
			)

			updateBrukerData(bruker, newBruker)
		}

	}

	override fun logSkjermedeBrukere() {
		var offset = 0
		var brukere: List<BrukerDbo>
		log.info("--- Logger Brukere START ---")
		do {
			brukere = brukerRepository.getBrukere(offset)

			brukere.forEach { bruker ->
				val person = personService.hentPerson(bruker.personIdent)

				if (person.diskresjonskode != null) {
					log.info("BrukerId har diskresjonskode: ${bruker.id}")
				}
			}

			log.info("Sjekket brukere mellom $offset og ${offset + brukere.size}")
			offset += brukere.size
		} while (brukere.isNotEmpty())
		log.info("---- Logger Brukere END ----")
	}

	override fun updateBrukerByPersonIdent(brukerId: UUID): Boolean {
		return brukerRepository.get(brukerId)
			?.let { getAndUpdateBrukerData(it) }
			?: throw NoSuchElementException("Bruker med id $brukerId eksisterer ikke")
	}

	override fun updateAllBrukere() {
		var brukereOppdatert = 0
		var offset = 0
		var brukere: List<BrukerDbo>

		log.info("Oppdaterer brukerinformasjon")
		do {
			brukere = brukerRepository.getBrukere(offset)

			brukere.forEach { bruker -> getAndUpdateBrukerData(bruker).let { if (it) brukereOppdatert++ } }

			log.info("Sjekket brukere mellom $offset og ${offset + brukere.size}")
			offset += brukere.size
		} while (brukere.isNotEmpty())

		log.info("Brukere oppdatert. Det var $brukereOppdatert oppdateringer.")
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


	private fun getAndUpdateBrukerData(bruker: BrukerDbo): Boolean {
		val person = personService.hentPerson(bruker.personIdent)
		val kontaktinformasjon = personService.hentPersonKontaktinformasjon(bruker.personIdent)

		val nyinnhentetBruker = bruker.copy(
			fornavn = person.fornavn,
			mellomnavn = person.mellomnavn,
			etternavn = person.etternavn,
			telefonnummer = person.telefonnummer ?: kontaktinformasjon.telefonnummer,
			epost = kontaktinformasjon.epost,
		)

		return updateBrukerData(bruker, nyinnhentetBruker)
	}

	private fun updateBrukerData(b1: BrukerDbo, b2: BrukerDbo): Boolean {
		val hasChanges = fun(b1: BrukerDbo, b2: BrukerDbo): Boolean {
			return b1.fornavn != b2.fornavn
				|| b1.mellomnavn != b2.mellomnavn
				|| b1.etternavn != b2.etternavn
				|| b1.telefonnummer != b2.telefonnummer
				|| b1.epost != b2.epost
		}

		if (hasChanges(b1, b2)) {
			log.info("Bruker er blitt oppdatert")
			secureLog.info("Bruker med ident ${b2.personIdent} er blitt oppdatert")
			brukerRepository.upsert(b2.upsert())
			return true
		}

		return false

	}


}

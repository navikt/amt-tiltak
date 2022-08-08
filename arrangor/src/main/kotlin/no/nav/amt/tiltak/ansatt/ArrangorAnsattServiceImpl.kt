package no.nav.amt.tiltak.ansatt

import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.arrangor.TilknyttetArrangor
import no.nav.amt.tiltak.core.port.ArrangorAnsattService
import no.nav.amt.tiltak.core.port.PersonService
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import java.util.*

@Service
class ArrangorAnsattServiceImpl(
	private val arrangorAnsattRepository: ArrangorAnsattRepository,
	private val personService: PersonService,
	private val template: NamedParameterJdbcTemplate
) : ArrangorAnsattService {

	override fun opprettAnsattHvisIkkeFinnes(personIdent: String): Ansatt {
		return getAnsattByPersonligIdent(personIdent)
			?: createAnsatt(personIdent)
	}

	override fun getAnsatt(ansattId: UUID): Ansatt {
		val ansattDbo = arrangorAnsattRepository.get(ansattId) ?: throw NoSuchElementException("Ansatt ikke funnet")

		val ansattesVirksomheter = mapTilknyttedeArrangorerTilAnsatt(
			ArrangorerForAnsattQuery(template).query(ansattDbo.personligIdent)
		)

		return ansattDbo.toAnsatt(ansattesVirksomheter)
	}

	override fun getAnsattByPersonligIdent(personIdent: String): Ansatt? {
		val ansattDbo = arrangorAnsattRepository.getByPersonligIdent(personIdent) ?: return null

		val ansattesVirksomheter = mapTilknyttedeArrangorerTilAnsatt(
			ArrangorerForAnsattQuery(template).query(personIdent)
		)

		return ansattDbo.toAnsatt(ansattesVirksomheter)
	}

	private fun createAnsatt(ansattPersonIdent: String): Ansatt {
		val person = personService.hentPerson(ansattPersonIdent)

		val nyAnsattId = UUID.randomUUID()

		arrangorAnsattRepository.opprettAnsatt(
			id = nyAnsattId,
			personligIdent = ansattPersonIdent,
			fornavn = person.fornavn,
			mellomnavn = person.mellomnavn,
			etternavn = person.etternavn
		)

		return getAnsatt(nyAnsattId)
	}

	private fun mapTilknyttedeArrangorerTilAnsatt(ansattesVirksomheter: List<ArrangorForAnsattDbo>): List<TilknyttetArrangor> {
		return ansattesVirksomheter
			.distinctBy { it.id }
			.map { arrangor ->
				val roller = ansattesVirksomheter.filter { arrangor.id == it.id }
					.map { it.rolle }

				TilknyttetArrangor(
					id = arrangor.id,
					navn = arrangor.navn,
					organisasjonsnummer = arrangor.organisasjonsnummer,
					overordnetEnhetNavn = arrangor.overordnetEnhetNavn,
					overordnetEnhetOrganisasjonsnummer = arrangor.overordnetEnhetOrganisasjonsnummer,
					roller = roller
				)
			}
	}
}

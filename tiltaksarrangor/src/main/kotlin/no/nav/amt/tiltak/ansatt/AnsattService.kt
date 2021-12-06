package no.nav.amt.tiltak.ansatt

import no.nav.amt.tiltak.core.domain.tiltaksarrangor.Ansatt
import no.nav.amt.tiltak.core.domain.tiltaksarrangor.TilknyttetArrangor
import no.nav.amt.tiltak.tiltaksarrangor.ansatt.ArrangorerForAnsattRepository
import no.nav.amt.tiltak.tiltaksarrangor.ansatt.ArrangorForAnsattDbo
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import java.util.*

@Service
class AnsattService(
	val ansattRepository: AnsattRepository,
	val template: NamedParameterJdbcTemplate
) {

	fun getAnsatt(ansattId: UUID): Ansatt {
		throw NotImplementedError("getAnsatt in AnsattService is not yet implemented")
	}

	fun getAnsattByPersonligIdent(personIdent: String): Ansatt {
		val ansattDbo =
			ansattRepository.getByPersonligIdent(personIdent) ?: throw NoSuchElementException("Ansatt ikke funnet")

		val ansattesVirksomheter = mapTilknyttedeArrangorerTilAnsatt(
			ArrangorerForAnsattRepository(template).query(personIdent)
		)

		return ansattDbo.toAnsatt(ansattesVirksomheter)
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

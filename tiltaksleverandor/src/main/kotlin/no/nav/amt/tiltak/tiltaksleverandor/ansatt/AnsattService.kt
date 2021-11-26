package no.nav.amt.tiltak.tiltaksleverandor.ansatt

import no.nav.amt.tiltak.core.domain.tiltaksleverandor.Ansatt
import no.nav.amt.tiltak.core.domain.tiltaksleverandor.TilknyttetLeverandor
import no.nav.amt.tiltak.tiltaksleverandor.ansatt.queries.GetLeverandorerForAnsattQuery
import no.nav.amt.tiltak.tiltaksleverandor.ansatt.queries.LeverandorForAnsattDbo
import no.nav.amt.tiltak.tiltaksleverandor.ansatt.repositories.AnsattRepository
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

		val ansattesVirksomheter = mapTilknyttedeLeverandorerTilAnsatt(
			GetLeverandorerForAnsattQuery(template).query(personIdent)
		)

		return ansattDbo.toAnsatt(ansattesVirksomheter)
	}


	private fun mapTilknyttedeLeverandorerTilAnsatt(ansattesVirksomheter: List<LeverandorForAnsattDbo>): List<TilknyttetLeverandor> {
		return ansattesVirksomheter
			.distinctBy { it.id }
			.map { leverandor ->
				val roller = ansattesVirksomheter.filter { leverandor.id == it.id }
					.map { it.rolle }

				TilknyttetLeverandor(
					id = leverandor.id,
					navn = leverandor.navn,
					organisasjonsnummer = leverandor.organisasjonsnummer,
					overordnetEnhetNavn = leverandor.overordnetEnhetNavn,
					overordnetEnhetOrganisasjonsnummer = leverandor.overordnetEnhetOrganisasjonsnummer,
					roller = roller
				)
			}
	}
}

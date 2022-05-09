package no.nav.amt.tiltak.endringsmelding

import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.time.LocalDate
import java.util.*

@Service
open class EndringsmeldingService(
	private val endringsmeldingRepository: EndringsmeldingRepository,
	private val endringsmeldingQuery: EndringsmeldingForGjennomforingQuery,
) {

	open fun hentEndringsmelding(id: UUID): EndringsmeldingDbo {
		return endringsmeldingRepository.get(id)
	}

	open fun opprettMedStartDato(deltakerId: UUID, startDato: LocalDate, ansattId: UUID): EndringsmeldingDbo {
		return endringsmeldingRepository.insertOgInaktiverStartDato(startDato, deltakerId, ansattId)
	}

	open fun markerSomFerdig(endringsmeldingId: UUID, navAnsattId: UUID) {
		val endringsmelding = endringsmeldingRepository.get(endringsmeldingId)

		if (!endringsmelding.aktiv) {
			// Kast custom exception og map til 400
			throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Endringsmelding er ikke aktiv")
		}

		endringsmeldingRepository.markerSomFerdig(endringsmeldingId, navAnsattId)
	}

	open fun hentEndringsmeldinger(gjennomforingId: UUID) : List<Endringsmelding> {
		return endringsmeldingQuery
			.query(gjennomforingId)
			.map { it.toEndringsmelding()}
	}

}

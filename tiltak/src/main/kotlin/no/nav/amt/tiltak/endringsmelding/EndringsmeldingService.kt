package no.nav.amt.tiltak.endringsmelding

import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.*

@Service
open class EndringsmeldingService(
	private val endringsmeldingRepository: EndringsmeldingRepository,
	private val endringsmeldingQuery: EndringsmeldingForGjennomforingQuery,
) {

	open fun opprettMedStartDato(deltakerId: UUID, startDato: LocalDate, ansattId: UUID): EndringsmeldingDbo {
		return endringsmeldingRepository.insertOgInaktiverStartDato(startDato, deltakerId, ansattId)
	}

	open fun hentEndringsmeldinger(gjennomforingId: UUID) : List<Endringsmelding> {
		return endringsmeldingQuery
			.query(gjennomforingId)
			.map { it.toEndringsmelding()}
	}

}

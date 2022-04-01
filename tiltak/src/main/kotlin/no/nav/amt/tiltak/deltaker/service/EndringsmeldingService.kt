package no.nav.amt.tiltak.deltaker.service

import no.nav.amt.tiltak.deltaker.dbo.EndringsmeldingDbo
import no.nav.amt.tiltak.deltaker.repositories.EndringsmeldingRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.util.*

@Service
open class EndringsmeldingService(
	private val repository: EndringsmeldingRepository,
) {

	fun opprettMedStartDato(deltakerId: UUID, startDato: LocalDate, ansattId: UUID): EndringsmeldingDbo{
		return repository.insertOgInaktiverStartDato(startDato, deltakerId, ansattId)
	}

}

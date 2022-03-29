package no.nav.amt.tiltak.deltaker.service

import no.nav.amt.tiltak.deltaker.repositories.EndringsmeldingRepository
import org.springframework.stereotype.Service

@Service
class EndringsmeldingService(
	private val repository: EndringsmeldingRepository
) {

}

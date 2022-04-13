package no.nav.amt.tiltak.deltaker.service

import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.core.port.ArrangorAnsattService
import no.nav.amt.tiltak.deltaker.dbo.EndringsmeldingDbo
import no.nav.amt.tiltak.deltaker.repositories.BrukerRepository
import no.nav.amt.tiltak.deltaker.repositories.EndringsmeldingRepository
import no.nav.amt.tiltak.deltaker.repositories.NavKontorRepository
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.*

@Service
open class EndringsmeldingService(
	private val repository: EndringsmeldingRepository,
	private val brukerRepository: BrukerRepository,
	private val navKontorRepository: NavKontorRepository,
	private val arrangorAnsattService: ArrangorAnsattService
) {

	fun opprettMedStartDato(deltakerId: UUID, startDato: LocalDate, ansattId: UUID): EndringsmeldingDbo {
		return repository.insertOgInaktiverStartDato(startDato, deltakerId, ansattId)
	}

	fun hentEndringsmeldinger(gjennomforingId: UUID) : List<Endringsmelding> {
		return repository
			.getByGjennomforing(gjennomforingId)
			.map {
				val bruker = brukerRepository.getByDeltakerId(it.deltakerId)?: throw Exception("Fant ikke bruker med deltakerid:${it.deltakerId} endringsmelding:${it.id}")
				val navKontor = bruker.navKontorId?.let { navKontorRepository.get(it).toNavKontor() }
				val opprettetAv = arrangorAnsattService.getAnsatt(it.opprettetAvId)
				it.toEndringsmelding(bruker.toBruker(navKontor), opprettetAv)
			}
	}

}

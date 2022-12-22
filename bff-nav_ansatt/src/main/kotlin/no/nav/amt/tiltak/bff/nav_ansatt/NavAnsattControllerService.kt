package no.nav.amt.tiltak.bff.nav_ansatt

import no.nav.amt.tiltak.bff.nav_ansatt.dto.DeltakerDto
import no.nav.amt.tiltak.bff.nav_ansatt.dto.EndringsmeldingDto
import no.nav.amt.tiltak.bff.nav_ansatt.dto.toDto
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.Endringsmelding
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.EndringsmeldingService
import org.springframework.stereotype.Service
import java.util.NoSuchElementException
import java.util.UUID

@Service
class NavAnsattControllerService(
	private val endringsmeldingService: EndringsmeldingService,
	private val deltakerService: DeltakerService
) {

	fun hentEndringsmeldinger (gjennomforingId: UUID, tilgangTilSkjermede: Boolean): List<EndringsmeldingDto> {
		val endringsmeldinger = endringsmeldingService.hentEndringsmeldingerForGjennomforing(gjennomforingId)
		val deltakerMap = deltakerService.hentDeltakerMap(endringsmeldinger.map { it.deltakerId })

		return endringsmeldinger.map { endringsmelding ->
			val deltaker = deltakerMap[endringsmelding.deltakerId]
				?: throw NoSuchElementException("Fant ikke deltaker med id ${endringsmelding.deltakerId}")

			if(deltaker.erSkjermet && !tilgangTilSkjermede) {
				return@map endringsmelding.toDto(DeltakerDto(erSkjermet = true))
			}

			return@map endringsmelding.toDto(deltaker.toDto())
		}
	}

	private fun Endringsmelding.toDto(deltakerDto: DeltakerDto) = EndringsmeldingDto(
		id = id,
		deltaker = deltakerDto,
		status = status.toDto(),
		innhold = innhold.toDto(),
		opprettetDato = opprettet,
	)

	private fun Deltaker.toDto() = DeltakerDto(
		fornavn = fornavn,
		mellomnavn = mellomnavn,
		etternavn = etternavn,
		fodselsnummer = fodselsnummer,
		erSkjermet = erSkjermet,
	)
}

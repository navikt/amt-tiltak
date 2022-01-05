package no.nav.amt.tiltak.deltaker.controllers

import no.nav.amt.tiltak.deltaker.dbo.DeltakerDetaljerDbo
import no.nav.amt.tiltak.deltaker.repositories.GetDeltakerDetaljerQuery
import no.nav.amt.tiltak.tiltak.dto.*
import org.springframework.http.HttpStatus
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.util.*

@Service
open class TiltakDeltakerPresentationService(
	private val template: NamedParameterJdbcTemplate
) {

	open fun getDeltakerDetaljerById(deltakerId: UUID): TiltakDeltakerDetaljerDto {
		return GetDeltakerDetaljerQuery(template).query(deltakerId)?.toDto()
			?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Deltaker med id $deltakerId finnes ikke")
	}

	private fun DeltakerDetaljerDbo.toDto(): TiltakDeltakerDetaljerDto {
		val hasVeileder = veilederNavn != null

		val veileder: NavVeilederDTO? = if (hasVeileder) {
			NavVeilederDTO(
				veilederNavn!!,
				veilederTelefonnummer,
				veilederEpost
			)
		} else null

		return TiltakDeltakerDetaljerDto(
			id = deltakerId,
			fornavn = fornavn,
			mellomnavn = mellomnavn,
			etternavn = etternavn,
			fodselsnummer = fodselsnummer,
			telefonnummer = telefonnummer,
			epost = epost,
			navKontor = NavKontorDTO(
				"Test Kontor",
				"Test gata 1337, 1337 Oslo"
			),
			navVeileder = veileder,
			oppstartdato = oppstartDato,
			sluttdato = sluttDato,
			registrertDato = registrertDato,
			status = status,
			gjennomforing = GjennomforingDto(
				id = gjennomforingId,
				navn = gjennomforingNavn,
				oppstartdato = gjennomforingOppstartDato,
				sluttdato = gjennomforingSluttDato,
				status = gjennomforingStatus,
				tiltak = TiltakDto(
					tiltaksnavn = tiltakNavn,
					tiltakskode = tiltakKode
				)
			)
		)
	}

}

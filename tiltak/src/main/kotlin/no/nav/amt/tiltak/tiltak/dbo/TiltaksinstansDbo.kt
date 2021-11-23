package no.nav.amt.tiltak.tiltak.dbo

import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import no.nav.amt.tiltak.tiltak.controllers.dto.TiltakInstansDto
import no.nav.amt.tiltak.tiltak.utils.UpdateCheck
import no.nav.amt.tiltak.tiltak.utils.UpdateStatus
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

data class TiltaksinstansDbo(
	val internalId: Int,
	val externalId: UUID,
	val arenaId: Int,
	val tiltaksleverandorInternalId: Int,
	val tiltaksleverandorExternalId: UUID,
	val tiltakInternalId: Int,
	val tiltakExternalId: UUID,
	val navn: String,
	val status: TiltakInstans.Status?,
	val oppstartDato: LocalDate?,
	val sluttDato: LocalDate?,
	val registrertDato: LocalDateTime?,
	val fremmoteDato: LocalDateTime?,
	val createdAt: LocalDateTime,
	val modifiedAt: LocalDateTime
) {

	fun toTiltakInstans(tiltak: Tiltak): TiltakInstans {
		return TiltakInstans(
			id = externalId,
			tiltak = tiltak,
			tiltaksarrangorId = tiltaksleverandorExternalId,
			navn = navn,
			status = status,
			oppstartDato = oppstartDato,
			sluttDato = sluttDato,
			registrertDato = registrertDato,
			fremmoteDato = fremmoteDato
		)
	}

	fun update(other: TiltaksinstansDbo): UpdateCheck<TiltaksinstansDbo> {
		if (this != other) {
			val updated = this.copy(
				navn = other.navn,
				status = other.status,
				oppstartDato = other.oppstartDato,
				sluttDato = other.sluttDato,
				registrertDato = other.registrertDato,
				fremmoteDato = other.fremmoteDato,
				modifiedAt = LocalDateTime.now()
			)

			return UpdateCheck(UpdateStatus.UPDATED, updated)
		}

		return UpdateCheck(UpdateStatus.NO_CHANGE)
	}
}

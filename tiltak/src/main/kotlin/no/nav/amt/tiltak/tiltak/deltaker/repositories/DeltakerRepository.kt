package no.nav.amt.tiltak.tiltak.deltaker.repositories

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.tiltak.deltaker.dbo.DeltakerDbo
import no.nav.amt.tiltak.tiltak.repositories.statements.insert.DeltakerInsertStatement
import no.nav.amt.tiltak.tiltak.repositories.statements.parts.bruker.BrukerFodselsnummerEqualsQueryPart
import no.nav.amt.tiltak.tiltak.repositories.statements.parts.bruker.BrukerInternalIdEqualsQueryPart
import no.nav.amt.tiltak.tiltak.repositories.statements.parts.deltaker.DeltakerExternalIdEqualsQueryPart
import no.nav.amt.tiltak.tiltak.repositories.statements.parts.tiltaksinstans.TiltaksinstansExternalIdEqualQueryPart
import no.nav.amt.tiltak.tiltak.repositories.statements.queries.GetDeltakerQueryStatement
import no.nav.amt.tiltak.tiltak.repositories.statements.update.DeltakerUpdateStatement
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.util.*

@Repository
class DeltakerRepository(
	private val template: NamedParameterJdbcTemplate
) {

	fun insert(
		brukerId: Int,
		tiltaksgjennomforing: UUID,
		oppstartDato: LocalDate?,
		sluttDato: LocalDate?,
		status: Deltaker.Status
	): DeltakerDbo {
		val externalId = DeltakerInsertStatement(
			template = template,
			brukerId = brukerId,
			tiltaksinstansId = tiltaksgjennomforing,
			oppstartsdato = oppstartDato,
			sluttdato = sluttDato,
			status = status
		).execute()

		return get(externalId)
			?: throw NoSuchElementException("Deltaker $brukerId finnes ikke på tiltaksgjennomføring $tiltaksgjennomforing")

	}

	fun update(deltaker: DeltakerDbo): DeltakerDbo {
		DeltakerUpdateStatement(template, deltaker).execute()

		return get(deltaker.externalId)
			?: throw NoSuchElementException("Deltaker ${deltaker.externalId} finnes ikke på tiltaksgjennomføring ${deltaker.tiltaksinstansInternalId}")
	}


	fun get(externalId: UUID): DeltakerDbo? {
		return GetDeltakerQueryStatement(template)
			.addPart(DeltakerExternalIdEqualsQueryPart(externalId))
			.execute()
			.firstOrNull()
	}

	fun get(brukerId: Int, tiltaksinstans: UUID): DeltakerDbo? {
		return GetDeltakerQueryStatement(template)
			.addPart(BrukerInternalIdEqualsQueryPart(brukerId))
			.addPart(TiltaksinstansExternalIdEqualQueryPart(tiltaksinstans))
			.execute()
			.firstOrNull()
	}

	fun get(fodselsnummer: String, tiltaksinstans: UUID): DeltakerDbo? {
		return GetDeltakerQueryStatement(template)
			.addPart(BrukerFodselsnummerEqualsQueryPart(fodselsnummer))
			.addPart(TiltaksinstansExternalIdEqualQueryPart(tiltaksinstans))
			.execute()
			.firstOrNull()
	}
}

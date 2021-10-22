package no.nav.amt.tiltak.tiltak.repositories

import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import no.nav.amt.tiltak.tiltak.dbo.TiltaksinstansDbo
import no.nav.amt.tiltak.tiltak.repositories.statements.insert.TiltaksinstansInsertStatement
import no.nav.amt.tiltak.tiltak.repositories.statements.parts.tiltaksinstans.TiltaksinstansArenaIdEqualsQueryPart
import no.nav.amt.tiltak.tiltak.repositories.statements.parts.tiltaksinstans.TiltaksinstansExternalIdEqualQueryPart
import no.nav.amt.tiltak.tiltak.repositories.statements.queries.GetTiltaksinstansQueryStatement
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.*

@Repository
open class TiltaksinstansRepository(private val template: NamedParameterJdbcTemplate) {

	fun insert(
		arenaId: Int,
		tiltakId: UUID,
		tiltaksleverandorId: UUID,
		navn: String,
		status: TiltakInstans.Status?,
		oppstartDato: LocalDate?,
		sluttDato: LocalDate?,
		registrertDato: LocalDateTime?,
		fremmoteDato: LocalDateTime?
	): TiltaksinstansDbo {
		val externalId = TiltaksinstansInsertStatement(
			template = template,
			arenaId = arenaId,
			tiltakId = tiltakId,
			tiltaksleverandorId = tiltaksleverandorId,
			navn = navn,
			status = status,
			oppstartDato = oppstartDato,
			sluttDato = sluttDato,
			registrertDato = registrertDato,
			fremmoteDato = fremmoteDato
		).execute()

		return get(externalId)
			?: throw NoSuchElementException("Tiltak med id $externalId finnes ikke")
	}

	fun get(id: UUID): TiltaksinstansDbo? {
		return GetTiltaksinstansQueryStatement(template)
			.addPart(TiltaksinstansExternalIdEqualQueryPart(id))
			.execute()
			.firstOrNull()
	}

	fun getByArenaId(arenaId: Int): TiltaksinstansDbo? {
		return GetTiltaksinstansQueryStatement(template)
			.addPart(TiltaksinstansArenaIdEqualsQueryPart(arenaId))
			.execute()
			.firstOrNull()
	}
}

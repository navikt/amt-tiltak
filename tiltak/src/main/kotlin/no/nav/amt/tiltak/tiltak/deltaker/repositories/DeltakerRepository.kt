package no.nav.amt.tiltak.tiltak.deltaker.repositories

import no.nav.amt.tiltak.tiltak.deltaker.dbo.DeltakerDbo
import no.nav.amt.tiltak.tiltak.repositories.statements.parts.bruker.BrukerFodselsnummerEqualsQueryPart
import no.nav.amt.tiltak.tiltak.repositories.statements.parts.tiltaksinstans.TiltaksinstansExternalIdEqualQueryPart
import no.nav.amt.tiltak.tiltak.repositories.statements.queries.GetDeltakerQueryStatement
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class DeltakerRepository(
	private val template: NamedParameterJdbcTemplate
) {

	fun get(fodselsnummer: String, tiltaksinstans: UUID): DeltakerDbo? {
		return GetDeltakerQueryStatement(template)
			.addPart(BrukerFodselsnummerEqualsQueryPart(fodselsnummer))
			.addPart(TiltaksinstansExternalIdEqualQueryPart(tiltaksinstans))
			.execute()
			.firstOrNull()
	}

}

package no.nav.amt.tiltak.tiltaksleverandor.repositories

import no.nav.amt.tiltak.tiltaksleverandor.dbo.TiltaksleverandorDbo
import no.nav.amt.tiltak.tiltaksleverandor.repositories.statements.insert.TiltaksleverandorInsertStatement
import no.nav.amt.tiltak.tiltaksleverandor.repositories.statements.parts.TiltaksleverandorVirksomhetsnummerEqualsQueryPart
import no.nav.amt.tiltak.tiltaksleverandor.repositories.statements.query.GetTiltaksleverandorQueryStatement
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
open class TiltaksleverandorRepository(
	private val template: NamedParameterJdbcTemplate,
) {

	fun insert(
		organisasjonsnavn: String,
		organisasjonsnummer: String,
		virksomhetsnummer: String,
		virksomhetsnavn: String
	): TiltaksleverandorDbo {
		val savedTiltaksleverandor = getByVirksomhetsnummer(virksomhetsnummer)

		if (savedTiltaksleverandor != null) {
			return savedTiltaksleverandor
		}

		TiltaksleverandorInsertStatement(
			template = template,
			organisasjonsnummer = organisasjonsnummer,
			organisasjonsnavn = organisasjonsnavn,
			virksomhetsnummer = virksomhetsnummer,
			virksomhetsnavn = virksomhetsnavn
		).execute()

		return getByVirksomhetsnummer(virksomhetsnummer)
			?: throw NoSuchElementException("Virksomhet med virksomhetsnummer $virksomhetsnummer finnes ikke")
	}

	fun getByVirksomhetsnummer(virksomhetsnummer: String): TiltaksleverandorDbo? {
		return GetTiltaksleverandorQueryStatement(template)
			.addPart(TiltaksleverandorVirksomhetsnummerEqualsQueryPart(virksomhetsnummer))
			.execute()
			.firstOrNull()
	}

}

package no.nav.amt.tiltak.tiltak.deltaker.repositories

import no.nav.amt.tiltak.tiltak.deltaker.dbo.BrukerDbo
import no.nav.amt.tiltak.tiltak.repositories.statements.insert.BrukerInsertStatement
import no.nav.amt.tiltak.tiltak.repositories.statements.parts.bruker.BrukerFodselsnummerEqualsQueryPart
import no.nav.amt.tiltak.tiltak.repositories.statements.queries.GetBrukerQueryStatement
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Repository

@Repository
class BrukerRepository(
	private val template: NamedParameterJdbcTemplate
) {

	fun insert(
		fodselsnummer: String,
		fornavn: String,
		etternavn: String,
		telefonnummer: String?,
		epost: String?,
		ansvarligVeilederId: Int?
	): BrukerDbo {
		val id = BrukerInsertStatement(
			template = template,
			fodselsnummer = fodselsnummer,
			fornavn = fornavn,
			etternavn = etternavn,
			telefonnummer = telefonnummer,
			epost = epost,
			ansvarligVeilederId = ansvarligVeilederId
		).execute()

		return get(id)
			?: throw NoSuchElementException("Bruker med id $id finnes ikke")
	}

	fun get(fodselsnummer: String): BrukerDbo? {
		return GetBrukerQueryStatement(template)
			.addPart(BrukerFodselsnummerEqualsQueryPart(fodselsnummer))
			.execute()
			.firstOrNull()
	}

}

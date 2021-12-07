package no.nav.amt.tiltak.connectors.person

import no.nav.amt.tiltak.connectors.dkif.DkifConnector
import no.nav.amt.tiltak.connectors.pdl.PdlClient
import no.nav.amt.tiltak.core.domain.veileder.Veileder
import no.nav.amt.tiltak.core.port.*
import org.springframework.stereotype.Service

@Service
class PersonFacade(
	private val pdlClient: PdlClient,
	private val dkifConnector: DkifConnector,
	private val veilarboppfolgingConnector: VeilarboppfolgingConnector,
	private val veilederService: VeilederService
) : PersonService {

	override fun hentPersonKontaktinformasjon(fnr: String): Kontaktinformasjon {
		val kontaktinformasjon = dkifConnector.hentBrukerKontaktinformasjon(fnr)

		return Kontaktinformasjon(
			epost = kontaktinformasjon.epost,
			telefonnummer = kontaktinformasjon.telefonnummer
		)
	}

	override fun hentPerson(fnr: String): Person {
		val bruker = pdlClient.hentBruker(fnr)

		return Person(
			fornavn = bruker.fornavn,
			mellomnavn = bruker.mellomnavn,
			etternavn = bruker.etternavn,
			telefonnummer = bruker.telefonnummer
		)
	}

	override fun hentTildeltVeileder(fnr: String): Veileder? {
		return veilarboppfolgingConnector.hentVeilederIdent(fnr)?.let { ident ->
			veilederService.hentVeileder(ident)
		}
	}

	override fun hentFnr(aktorId: String): String {
		return pdlClient.hentFnr(aktorId)
	}

}

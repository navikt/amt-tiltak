package no.nav.amt.tiltak.connectors.person

import no.nav.amt.tiltak.connectors.dkif.DkifConnector
import no.nav.amt.tiltak.connectors.pdl.AdressebeskyttelseGradering
import no.nav.amt.tiltak.connectors.pdl.PdlConnector
import no.nav.amt.tiltak.connectors.veilarboppfolging.VeilarboppfolgingClient
import no.nav.amt.tiltak.core.domain.veileder.Veileder
import no.nav.amt.tiltak.core.port.*
import org.springframework.stereotype.Service

@Service
class PersonFacade(
	private val pdlConnector: PdlConnector,
	private val dkifConnector: DkifConnector,
	private val veilarboppfolgingClient: VeilarboppfolgingClient,
	private val veilederConnector: VeilederConnector
) : PersonService {

	override fun hentPersonKontaktinformasjon(fnr: String): Kontaktinformasjon {
		val kontaktinformasjon = dkifConnector.hentBrukerKontaktinformasjon(fnr)

		return Kontaktinformasjon(
			epost = kontaktinformasjon.epost,
			telefonnummer = kontaktinformasjon.telefonnummer
		)
	}

	override fun hentPerson(fnr: String): Person {
		val bruker = pdlConnector.hentBruker(fnr)

		return Person(
			fornavn = bruker.fornavn,
			mellomnavn = bruker.mellomnavn,
			etternavn = bruker.etternavn,
			telefonnummer = bruker.telefonnummer,
			diskresjonskode = when(bruker.adressebeskyttelseGradering) {
				AdressebeskyttelseGradering.FORTROLIG -> Diskresjonskode.KODE_7
				AdressebeskyttelseGradering.STRENGT_FORTROLIG -> Diskresjonskode.KODE_6
				AdressebeskyttelseGradering.STRENGT_FORTROLIG_UTLAND -> Diskresjonskode.KODE_19
				else -> null
			}
		)
	}

	override fun hentTildeltVeileder(fnr: String): Veileder? {
		return veilarboppfolgingClient.hentVeilederIdent(fnr)?.let { ident ->
			veilederConnector.hentVeileder(ident)
		}
	}

	override fun hentGjeldendePersonligIdent(ident: String): String {
		return pdlConnector.hentGjeldendePersonligIdent(ident)
	}

}

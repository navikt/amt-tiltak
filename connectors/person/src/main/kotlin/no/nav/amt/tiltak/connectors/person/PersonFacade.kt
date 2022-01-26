package no.nav.amt.tiltak.connectors.person

import no.nav.amt.tiltak.clients.dkif.DkifClient
import no.nav.amt.tiltak.clients.pdl.AdressebeskyttelseGradering
import no.nav.amt.tiltak.clients.pdl.PdlClient
import no.nav.amt.tiltak.clients.veilarboppfolging.VeilarboppfolgingClient
import no.nav.amt.tiltak.core.domain.veileder.Veileder
import no.nav.amt.tiltak.core.port.*
import org.springframework.stereotype.Service

@Service
class PersonFacade(
	private val pdlClient: PdlClient,
	private val dkifClient: DkifClient,
	private val veilarboppfolgingClient: VeilarboppfolgingClient,
	private val veilederConnector: VeilederConnector
) : PersonService {

	override fun hentPersonKontaktinformasjon(fnr: String): Kontaktinformasjon {
		val kontaktinformasjon = dkifClient.hentBrukerKontaktinformasjon(fnr)

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
		return pdlClient.hentGjeldendePersonligIdent(ident)
	}

}

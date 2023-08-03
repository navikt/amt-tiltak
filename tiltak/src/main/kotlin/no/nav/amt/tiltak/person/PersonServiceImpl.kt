package no.nav.amt.tiltak.person

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.MeterRegistry
import no.nav.amt.tiltak.clients.dkif.DkifClient
import no.nav.amt.tiltak.clients.pdl.AdressebeskyttelseGradering
import no.nav.amt.tiltak.clients.pdl.PdlClient
import no.nav.amt.tiltak.core.port.Diskresjonskode
import no.nav.amt.tiltak.core.port.Kontaktinformasjon
import no.nav.amt.tiltak.core.port.Person
import no.nav.amt.tiltak.core.port.PersonService
import no.nav.poao_tilgang.client.PoaoTilgangClient
import org.springframework.stereotype.Service

@Service
class PersonServiceImpl(
	private val pdlClient: PdlClient,
	private val dkifClient: DkifClient,
	private val meterRegistry: MeterRegistry,
	private val poaoTilgangClient: PoaoTilgangClient
) : PersonService {

	private val diskresjonskodeCounters = mapOf<Diskresjonskode, Counter>()

	override fun hentPersonKontaktinformasjon(fnr: String) : Kontaktinformasjon {
		val kontaktinformasjon = dkifClient.hentBrukerKontaktinformasjon(fnr)

		return Kontaktinformasjon(
			epost = kontaktinformasjon.epost,
			telefonnummer = kontaktinformasjon.telefonnummer
		)
	}

	override fun hentPerson(fnr: String) : Person {
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
		).also { person -> person.diskresjonskode?.let { incrementCounter(it) } }
	}

	override fun hentGjeldendePersonligIdent(ident: String) : String = pdlClient.hentGjeldendePersonligIdent(ident)

	override fun erSkjermet(norskIdent: String): Boolean {
		return poaoTilgangClient.erSkjermetPerson(norskIdent).getOrThrow()
	}

	private fun incrementCounter(kode: Diskresjonskode) = diskresjonskodeCounters.getOrElse(kode) {
		meterRegistry.counter("amt_tiltak_connector_person_counter", "diskresjonskode", kode.name)
	}.increment()


}

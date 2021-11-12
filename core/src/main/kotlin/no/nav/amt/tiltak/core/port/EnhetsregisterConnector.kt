package no.nav.amt.tiltak.core.port

import no.nav.amt.tiltak.core.domain.enhetsregister.Virksomhet


interface EnhetsregisterConnector {

	fun hentVirksomhet(organisasjonsnummer: String): Virksomhet

}

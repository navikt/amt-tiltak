package no.nav.amt.tiltak.clients.amt_person.model

enum class AdressebeskyttelseGradering {
	STRENGT_FORTROLIG,
	FORTROLIG,
	STRENGT_FORTROLIG_UTLAND,
	UGRADERT,
}

fun AdressebeskyttelseGradering?.erBeskyttet(): Boolean {
	return this != AdressebeskyttelseGradering.UGRADERT && this != null
}

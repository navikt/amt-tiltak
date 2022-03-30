package no.nav.amt.tiltak.core.domain.navansatt

interface AnsattTilgang {

	//fun getKode(): String
	//fun getNavn(): String
	//fun getTemaer(): List<String>

	fun harTilgang(enhetId: String, tema: String): Boolean

	companion object {
		val aldriTilgang = object: AnsattTilgang {
			override fun harTilgang(enhet: String, tema: String) = false
		}
		val alltidTilgang = object: AnsattTilgang {
			override fun harTilgang(enhet: String, tema: String) = true
		}
	}

}



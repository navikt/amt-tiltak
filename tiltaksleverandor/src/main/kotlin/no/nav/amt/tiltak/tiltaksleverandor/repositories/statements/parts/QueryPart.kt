package no.nav.amt.tiltak.tiltaksleverandor.repositories.statements.parts

interface QueryPart {
    fun getTemplate(): String
    fun getParameters(): Map<String, Any>
}

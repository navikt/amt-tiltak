package no.nav.amt.tiltak.tiltak.repositories.statements.parts

interface QueryPart {
    fun getTemplate(): String
    fun getParameters(): Map<String, Any>
}

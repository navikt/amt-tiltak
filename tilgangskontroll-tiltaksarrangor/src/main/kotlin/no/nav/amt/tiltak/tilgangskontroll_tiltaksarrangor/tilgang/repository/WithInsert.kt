package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.tilgang.repository

import org.springframework.data.jdbc.core.JdbcAggregateTemplate

// https://github.com/spring-projects/spring-data-examples/blob/main/jdbc/basics/src/main/java/example/springdata/jdbc/basics/simpleentity/WithInsert.java

fun interface WithInsert<T : Any> {
	/**
	 * Custom insert method.
	 *
	 * @param t
	 * @return
	 */
	fun insert(t: T): T
}

@Suppress("unused")
class WithInsertImpl<T : Any>(
	private val template: JdbcAggregateTemplate
) : WithInsert<T> {

	override fun insert(t: T): T = template.insert(t)
}

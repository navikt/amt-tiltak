package no.nav.amt.tiltak.tiltak.repositories.statements.queries

import no.nav.amt.tiltak.tiltak.repositories.statements.parts.QueryPart
import org.slf4j.Logger
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.Duration
import java.time.Instant

abstract class QueryStatement<T>(
    val logger: Logger,
    val template: NamedParameterJdbcTemplate,
    val addWhere: Boolean = true
) {

    private val queryParts = ArrayList<QueryPart>()
    private val parameters = MapSqlParameterSource()

    abstract fun getSqlString(): String
    abstract fun getMapper(): RowMapper<T>

    open fun getGroupClause(): String? {
        return null
    }

    open fun getOrderByClause(): String? {
        return null
    }

    fun addPart(part: QueryPart): QueryStatement<T> {
        queryParts.add(part)
        return this
    }

    fun execute(): List<T> {
        val start = Instant.now()

        val query = buildQuery().trim().replace(" +", " ")

        queryParts.forEach { part -> parameters.addValues(part.getParameters()) }

        try {
            val data: List<T> = template.query(query, parameters, getMapper())

            val duration = Duration.between(start, Instant.now()).toMillis()
            logger.info("Query [Execution time: $duration ms] [Query: $query]")

            return data
        } catch (e: Exception) {
            logger.error("Exception happened while executing query: $query")
            throw e
        }

    }

    private fun buildQuery(): String {
        val builder = StringBuilder(getSqlString())
        var where = addWhere

        queryParts.forEach { part ->
            if (where) {
                builder.append(" WHERE ")
                where = false
            } else {
                builder.append(" AND ")
            }

            builder.append(part.getTemplate())
        }

        if (getGroupClause() != null) builder.append(" ").append(getGroupClause())
        if (getOrderByClause() != null) builder.append(" ").append(getOrderByClause())

        return builder.toString()
    }

}

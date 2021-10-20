package no.nav.amt.tiltak.tiltak.repositories.statements.insert

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

class TiltakInsertStatement(
    val template: NamedParameterJdbcTemplate,
    val arenaId: String,
    val navn: String,
    val kode: String,
) {

    //language=PostgreSQL
    private val sql = """
            insert into tiltak(external_id, arena_id, navn, type)
            values (:externalId,
                    :arenaId,
                    :navn,
                    :kode)
    """.trimIndent()

    fun execute(): UUID {
        val externalId = UUID.randomUUID()

        val parameters = MapSqlParameterSource().addValues(
            mapOf(
                "externalId" to externalId,
                "arenaId" to arenaId,
                "navn" to navn,
                "kode" to kode
            )
        )

        template.update(sql, parameters)

        return externalId
    }

}

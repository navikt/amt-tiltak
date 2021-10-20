package no.nav.amt.tiltak.tiltak.repositories.statements.insert

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

class TiltakInsertStatement(
    val template: NamedParameterJdbcTemplate,
    val arenaId: String,
    val tiltaksleverandorId: UUID,
    val navn: String,
    val kode: String,
) {

    //language=PostgreSQL
    private val sql = """
            insert into tiltak(external_id, arena_id, tiltaksleverandor_id, navn, type)
            values (:externalId,
                    :arenaId,
                    (select id from tiltaksleverandor where external_id = :tiltaksleverandorId),
                    :navn,
                    :kode)
    """.trimIndent()

    fun execute(): UUID {
        val externalId = UUID.randomUUID()

        val parameters = MapSqlParameterSource().addValues(
            mapOf(
                "externalId" to externalId,
                "arenaId" to arenaId,
                "tiltaksleverandorId" to tiltaksleverandorId,
                "navn" to navn,
                "kode" to kode
            )
        )

        template.update(sql, parameters)

        return externalId
    }

}

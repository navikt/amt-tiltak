package no.nav.amt.tiltak.tiltak.repositories.statements.queries

import no.nav.amt.tiltak.tiltak.dbo.TiltakDbo
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

class GetTiltakQueryStatement(
    template: NamedParameterJdbcTemplate
) : QueryStatement<TiltakDbo>(
    LoggerFactory.getLogger(GetTiltakQueryStatement::class.java),
    template,
    true
) {

    override fun getSqlString(): String {
        //language=PostgreSQL
        return "select tiltak.id                     as tiltak_internal_id, " +
                "       tiltak.external_id            as tiltak_external_id, " +
                "       tiltak.arena_id               as tiltak_arena_id, " +
                "       tiltak.tiltaksleverandor_id   as tiltaksleverandor_internal_id, " +
                "       tiltaksleverandor.external_id as tiltaksleverandor_external_id, " +
                "       tiltak.navn                   as tiltak_navn, " +
                "       tiltak.type                   as tiltak_type, " +
                "       tiltak.created_at             as created_at, " +
                "       tiltak.modified_at            as modified_at " +
                "from tiltak " +
                "         inner join tiltaksleverandor on tiltak.tiltaksleverandor_id = tiltaksleverandor.id "
    }

    override fun getMapper(): RowMapper<TiltakDbo> {
        return RowMapper { rs, _ ->
            TiltakDbo(
                internalId = rs.getInt("tiltak_internal_id"),
                externalId = UUID.fromString(rs.getString("tiltak_external_id")),
                arenaId = rs.getString("tiltak_arena_id"),
                tiltaksleverandorInternalId = rs.getInt("tiltaksleverandor_internal_id"),
                tiltaksleverandorExternalId = UUID.fromString(rs.getString("tiltaksleverandor_external_id")),
                navn = rs.getString("tiltak_navn"),
                type = rs.getString("tiltak_type"),
                createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
                modifiedAt = rs.getTimestamp("modified_at").toLocalDateTime()
            )
        }
    }
}

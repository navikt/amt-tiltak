package no.nav.amt.tiltak.tiltak.repositories.statements.queries

import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import no.nav.amt.tiltak.tiltak.dbo.TiltaksinstansDbo
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

class GetTiltaksinstansQueryStatement(
	template: NamedParameterJdbcTemplate
) : QueryStatement<TiltaksinstansDbo>(
	template = template,
	logger = LoggerFactory.getLogger(GetTiltaksinstansQueryStatement::class.java)
) {

	override fun getSqlString(): String {
		//language=PostgreSQL
		return """
			SELECT tiltaksinstans.id                                                           as tiltaksinstans_internal_id,
			       tiltaksinstans.external_id                                                  as tiltaksinstans_external_id,
			       tiltaksinstans.arena_id                                                     as tiltaksinstans_arena_id,
			       tiltaksinstans.tiltaksleverandor_id                                         as tiltaksleverandor_internal_id,
			       (SELECT external_id
			        FROM tiltaksleverandor
			        WHERE tiltaksleverandor.id = tiltaksinstans.tiltaksleverandor_id)          as tiltaksleverandor_external_id,
			       tiltaksinstans.tiltak_id                                                    as tiltak_internal_id,
			       (SELECT external_id FROM tiltak WHERE tiltak.id = tiltaksinstans.tiltak_id) as tiltak_external_id,
			       tiltaksinstans.navn                                                         as navn,
			       tiltaksinstans.status                                                       as status,
			       tiltaksinstans.oppstart_dato                                                as oppstart_dato,
			       tiltaksinstans.slutt_dato                                                   as slutt_dato,
			       tiltaksinstans.registrert_dato                                              as registrert_dato,
			       tiltaksinstans.fremmote_dato                                                as fremmote_dato,
				   tiltaksinstans.created_at 												   as created_at,
				   tiltaksinstans.modified_at                                                  as modified_at
			FROM tiltaksinstans
			WHERE arena_id = :arenaId
		""".trimIndent()
	}

	override fun getMapper(): RowMapper<TiltaksinstansDbo> {
		return RowMapper { rs, _ ->
			val statusString = rs.getString("status")

			TiltaksinstansDbo(
				id = rs.getInt("tiltaksinstans_internal_id"),
				externalId = UUID.fromString(rs.getString("tiltaksinstans_external_id")),
				arenaId = rs.getInt("tiltaksinstans_arena_id"),
				tiltaksleverandorId = rs.getInt("tiltaksleverandor_internal_id"),
				tiltaksleverandorExternalId = UUID.fromString(rs.getString("tiltaksleverandor_external_id")),
				tiltakId = rs.getInt("tiltak_internal_id"),
				tiltakExternalId = UUID.fromString(rs.getString("tiltak_external_id")),
				navn = rs.getString("navn"),
				status = if (statusString != null) TiltakInstans.Status.valueOf(statusString) else null,
				oppstartDato = rs.getDate("oppstart_dato")?.toLocalDate(),
				sluttDato = rs.getDate("slutt_dato")?.toLocalDate(),
				registrertDato = rs.getTimestamp("registrert_dato")?.toLocalDateTime(),
				fremmoteDato = rs.getTimestamp("fremmote_dato")?.toLocalDateTime(),
				createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
				modifiedAt = rs.getTimestamp("modified_at").toLocalDateTime()
			)
		}
	}
}

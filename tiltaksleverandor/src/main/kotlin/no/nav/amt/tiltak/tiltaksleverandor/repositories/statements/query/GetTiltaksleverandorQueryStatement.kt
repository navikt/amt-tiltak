package no.nav.amt.tiltak.tiltaksleverandor.repositories.statements.query

import no.nav.amt.tiltak.tiltaksleverandor.dbo.TiltaksleverandorDbo
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

class GetTiltaksleverandorQueryStatement(
	template: NamedParameterJdbcTemplate
) : QueryStatement<TiltaksleverandorDbo>(
	template = template,
	logger = LoggerFactory.getLogger(GetTiltaksleverandorQueryStatement::class.java)
) {

	override fun getSqlString(): String {
		//language=PostgreSQL
		return """
			SELECT id                  as tiltaksleverandor_internal_id,
			       external_id         as tiltaksleverandor_external_id,
			       organisasjonsnummer as organisasjonsnummer,
			       organisasjonsnavn   as organsisasjonsnavn,
			       virksomhetsnummer   as virksomhetsnummer,
			       virksomhetsnavn     as virksomhetsnavn,
			       created_at          as created_at,
			       modified_at         as modified_at
			FROM tiltaksleverandor
		""".trimIndent()
	}

	override fun getMapper(): RowMapper<TiltaksleverandorDbo> {
		return RowMapper { rs, _ ->
			TiltaksleverandorDbo(
				internalId = rs.getInt("tiltaksleverandor_internal_id"),
				externalId = UUID.fromString(rs.getString("tiltaksleverandor_external_id")),
				organisasjonsnummer = rs.getString("organisasjonsnummer"),
				organisasjonsnavn = rs.getString("organsisasjonsnavn"),
				virksomhetsnummer = rs.getString("virksomhetsnummer"),
				virksomhetsnavn = rs.getString("virksomhetsnavn"),
				createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
				modifiedAt = rs.getTimestamp("modified_at").toLocalDateTime()
			)
		}
	}
}

package no.nav.amt.tiltak.tiltak.repositories.statements.queries

import no.nav.amt.tiltak.tiltak.deltaker.dbo.BrukerDbo
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class GetBrukerQueryStatement(
	template: NamedParameterJdbcTemplate
) : QueryStatement<BrukerDbo>(
	logger = LoggerFactory.getLogger(GetBrukerQueryStatement::class.java),
	template = template
) {

	override fun getSqlString(): String {
		//language=PostgreSQL
		return """
			SELECT id                    as bruker_internal_id,
			       fodselsnummer         as fodselsnummer,
			       fornavn               as fornavn,
			       etternavn             as etternavn,
			       telefonnummer         as telefonnummer,
			       epost                 as epost,
			       ansvarlig_veileder_id as ansvarlig_veileder_internal_id,
			       created_at            as created_at,
			       modified_at           as modified_at
			FROM bruker
		""".trimIndent()
	}

	override fun getMapper(): RowMapper<BrukerDbo> {
		return RowMapper { rs, _ ->
			BrukerDbo(
				internalId = rs.getInt("bruker_internal_id"),
				fodselsnummer = rs.getString("fodselsnummer"),
				fornavn = rs.getString("fornavn"),
				etternavn = rs.getString("etternavn"),
				telefonnummer = rs.getString("telefonnummer"),
				epost = rs.getString("epost"),
				ansvarligVeilederInternalId = rs.getInt("ansvarlig_veileder_internal_id"),
				createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
				modifiedAt = rs.getTimestamp("modified_at").toLocalDateTime()
			)
		}
	}
}

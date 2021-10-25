package no.nav.amt.tiltak.tiltak.repositories.statements.queries

import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.tiltak.deltaker.dbo.DeltakerDbo
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

class GetDeltakerQueryStatement(
	template: NamedParameterJdbcTemplate
) : QueryStatement<DeltakerDbo>(
	logger = LoggerFactory.getLogger(GetDeltakerQueryStatement::class.java),
	template = template
) {
	override fun getSqlString(): String {
		//language=PostgreSQL
		return """
			SELECT deltaker.id                as deltaker_internal_id,
				   deltaker.external_id       as deltaker_external_id,
				   deltaker.bruker_id         as bruker_internal_id,
				   bruker.personlig_ident     as bruker_fodselsnummer,
				   bruker.fornavn             as bruker_fornavn,
				   bruker.etternavn           as bruker_etternavn,
				   deltaker.oppstart_dato     as deltaker_oppstartsdato,
				   deltaker.slutt_dato        as deltaker_sluttdato,
				   deltaker.tiltaksinstans_id as tiltaksinstans_internal_id,
				   tiltaksinstans.external_id as tiltaksinstans_external_id,
				   deltaker.status            as deltaker_status,
				   deltaker.created_at        as deltaker_created_at,
				   deltaker.modified_at       as deltaker_modified_at
			FROM deltaker
					 inner join bruker on bruker.id = deltaker.bruker_id
					 inner join tiltaksinstans on tiltaksinstans.id = deltaker.tiltaksinstans_id
		""".trimIndent()
	}

	override fun getMapper(): RowMapper<DeltakerDbo> {

		return RowMapper { rs, _ ->
			val statusString = rs.getString("deltaker_status")

			DeltakerDbo(
				internalId = rs.getInt("deltaker_internal_id"),
				externalId = UUID.fromString(rs.getString("deltaker_external_id")),
				brukerInternalId = rs.getInt("bruker_internal_id"),
				brukerFodselsnummer = rs.getString("bruker_fodselsnummer"),
				brukerFornavn = rs.getString("bruker_fornavn"),
				brukerEtternavn = rs.getString("bruker_etternavn"),
				deltakerOppstartsdato = rs.getDate("deltaker_oppstartsdato")?.toLocalDate(),
				deltakerSluttdato = rs.getDate("deltaker_sluttdato")?.toLocalDate(),
				tiltaksinstansInternalId = rs.getInt("tiltaksinstans_internal_id"),
				status = if (statusString != null) Deltaker.Status.valueOf(statusString) else null,
				createdAt = rs.getTimestamp("deltaker_created_at").toLocalDateTime(),
				modifiedAt = rs.getTimestamp("deltaker_modified_at").toLocalDateTime()
			)
		}
	}

}

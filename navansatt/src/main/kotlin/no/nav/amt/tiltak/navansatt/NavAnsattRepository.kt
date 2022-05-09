package no.nav.amt.tiltak.navansatt

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.core.domain.nav_ansatt.Bucket
import no.nav.amt.tiltak.core.domain.nav_ansatt.UpsertNavAnsattInput
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
internal class NavAnsattRepository(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		NavAnsattDbo(
			id = UUID.fromString(rs.getString("id")),
			navIdent = rs.getString("nav_ident"),
			navn = rs.getString("navn"),
			telefonnummer = rs.getString("telefonnummer"),
			epost = rs.getString("epost")
		)
	}

	internal fun get(id: UUID): NavAnsattDbo {
		val sql = """
			SELECT * FROM nav_ansatt WHERE id = :id
		""".trimIndent()

		val parameters = sqlParameters("id" to id)

		return template.query(sql, parameters, rowMapper).firstOrNull()
			?: throw NoSuchElementException("Fant ikke NAV Ansatt med id=$id")
	}

	internal fun upsert(upsertCmd: UpsertNavAnsattInput) {
		val sql = """
			INSERT INTO nav_ansatt(id, nav_ident, navn, telefonnummer, epost, bucket)
			VALUES (:id,
					:navIdent,
					:navn,
					:telefonnummer,
					:epost,
					:bucket)
			ON CONFLICT (nav_ident) DO UPDATE SET navn       	  = :navn,
														telefonnummer = :telefonnummer,
														epost         = :epost,
														bucket        = :bucket
		""".trimIndent()

		val parameterSource = sqlParameters(
			"id" to upsertCmd.id,
			"navIdent" to upsertCmd.navIdent,
			"navn" to upsertCmd.navn,
			"telefonnummer" to upsertCmd.telefonnummer,
			"epost" to upsertCmd.epost,
			"bucket" to upsertCmd.bucket.id,
		)

		template.update(sql, parameterSource)
	}

	internal fun getNavAnsattWithIdent(navIdent: String): NavAnsattDbo? {
		return template.query(
			"SELECT * FROM nav_ansatt WHERE nav_ident = :navIdent",
			sqlParameters("navIdent" to navIdent),
			rowMapper
		).firstOrNull()
	}

	internal fun getNavAnsattInBucket(bucket: Bucket): List<NavAnsattDbo> = template.query(
		"SELECT * FROM nav_ansatt WHERE bucket = :bucket",
		sqlParameters("bucket" to bucket.id),
		rowMapper
	)

}

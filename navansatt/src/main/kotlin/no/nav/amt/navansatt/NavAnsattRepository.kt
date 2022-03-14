package no.nav.amt.navansatt

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
internal open class NavAnsattRepository(
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

	internal fun upsert(upsertCmd: NavAnsattDbo) {
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

		val parameterSource = MapSqlParameterSource().addValues(
			mapOf(
				"id" to upsertCmd.id,
				"navIdent" to upsertCmd.navIdent,
				"navn" to upsertCmd.navn,
				"telefonnummer" to upsertCmd.telefonnummer,
				"epost" to upsertCmd.epost,
				"bucket" to upsertCmd.bucket.id,
			)
		)

		template.update(sql, parameterSource)
	}

	internal fun getNavAnsattWithIdent(navIdent: String): NavAnsattDbo? {
		return template.query(
			"SELECT * FROM nav_ansatt WHERE nav_ident = :navIdent",
			MapSqlParameterSource().addValues(mapOf("navIdent" to navIdent)),
			rowMapper
		).firstOrNull()
	}

	internal fun getNavAnsattInBatch(bucket: Bucket): List<NavAnsattDbo> = template.query(
		"SELECT * FROM nav_ansatt WHERE bucket = :bucket",
		MapSqlParameterSource().addValues(mapOf("bucket" to bucket.id)),
		rowMapper
	)

}

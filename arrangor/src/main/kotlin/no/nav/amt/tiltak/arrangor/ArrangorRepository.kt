package no.nav.amt.tiltak.arrangor

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getLocalDateTime
import no.nav.amt.tiltak.common.db_utils.getUUID
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
open class ArrangorRepository(
    private val template: NamedParameterJdbcTemplate,
) {

    private val rowMapper = RowMapper { rs, _ ->
        ArrangorDbo(
            id = rs.getUUID("id"),
            navn = rs.getString("navn"),
            organisasjonsnummer = rs.getString("organisasjonsnummer"),
            overordnetEnhetNavn = rs.getString("overordnet_enhet_navn"),
            overordnetEnhetOrganisasjonsnummer = rs.getString("overordnet_enhet_organisasjonsnummer"),
            createdAt = rs.getLocalDateTime("created_at"),
            modifiedAt = rs.getLocalDateTime("modified_at")
        )
    }


    fun upsert(
		navn: String,
		organisasjonsnummer: String,
		overordnetEnhetNavn: String?,
		overordnetEnhetOrganisasjonsnummer: String?,
    ): ArrangorDbo {
        val savedArrangor = getByOrganisasjonsnummer(organisasjonsnummer)

        if (savedArrangor != null) {
            return savedArrangor
        }

        val sql = """
			INSERT INTO arrangor(id, overordnet_enhet_organisasjonsnummer, overordnet_enhet_navn, organisasjonsnummer, navn)
			VALUES (:id,
					:overordnetEnhetOrganisasjonsnummer,
					:overordnetEnhetNavn,
					:organisasjonsnummer,
					:navn)
		""".trimIndent()

        val id = UUID.randomUUID()

        val parameters = MapSqlParameterSource().addValues(
            mapOf(
                "id" to id,
                "navn" to navn,
                "organisasjonsnummer" to organisasjonsnummer,
                "overordnetEnhetOrganisasjonsnummer" to overordnetEnhetOrganisasjonsnummer,
                "overordnetEnhetNavn" to overordnetEnhetNavn,
            )
        )

        template.update(sql, parameters)

        return getByOrganisasjonsnummer(organisasjonsnummer)
            ?: throw NoSuchElementException("Virksomhet med organisasjonsnummer $organisasjonsnummer finnes ikke")
    }

	open fun insert(
		id: UUID,
		navn: String,
		organisasjonsnummer: String,
		overordnetEnhetNavn: String?,
		overordnetEnhetOrganisasjonsnummer: String?,
	) {
		val sql = """
			INSERT INTO arrangor(id, overordnet_enhet_organisasjonsnummer, overordnet_enhet_navn, organisasjonsnummer, navn)
			VALUES (:id,
					:overordnetEnhetOrganisasjonsnummer,
					:overordnetEnhetNavn,
					:organisasjonsnummer,
					:navn)
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"id" to id,
				"navn" to navn,
				"organisasjonsnummer" to organisasjonsnummer,
				"overordnetEnhetOrganisasjonsnummer" to overordnetEnhetOrganisasjonsnummer,
				"overordnetEnhetNavn" to overordnetEnhetNavn,
			)
		)

		template.update(sql, parameters)
	}

    fun getByOrganisasjonsnummer(organisasjonsnummer: String): ArrangorDbo? {
        val sql = """
			SELECT id,
				   overordnet_enhet_organisasjonsnummer,
				   overordnet_enhet_navn,
				   organisasjonsnummer,
				   navn,
				   created_at,
				   modified_at
			FROM arrangor
			WHERE organisasjonsnummer = :organisasjonsnummer
		""".trimIndent()

        val parameters = MapSqlParameterSource().addValues(
            mapOf(
                "organisasjonsnummer" to organisasjonsnummer
            )
        )

        return template.query(sql, parameters, rowMapper)
            .firstOrNull()
    }

	fun getById(id: UUID): ArrangorDbo {
		val sql = """
			SELECT * FROM arrangor WHERE id = :id
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"id" to id
			)
		)

		return template.query(sql, parameters, rowMapper).firstOrNull()
			?: throw NoSuchElementException("Fant ikke arrangor med id: $id")
	}

	fun getByIder(arrangorIder: List<UUID>): List<ArrangorDbo> {
		if (arrangorIder.isEmpty())
			return emptyList()

		val sql = """
			SELECT * FROM arrangor WHERE id in(:ider)
		""".trimIndent()

		val parameters = sqlParameters("ider" to arrangorIder)

		return template.query(sql, parameters, rowMapper)
	}

	fun update(arrangorUpdateDbo: ArrangorUpdateDbo) {
		val sql = """
			UPDATE arrangor
			SET
				navn = :navn,
				overordnet_enhet_navn = :overordnetEnhetNavn,
				overordnet_enhet_organisasjonsnummer = :overordnetEnhetOrganisasjonsnummer,
				modified_at = :modifiedAt
			WHERE id = :id
		""".trimIndent()

		val parameters = sqlParameters(
			"id" to arrangorUpdateDbo.id,
			"navn" to arrangorUpdateDbo.navn,
			"overordnetEnhetNavn" to arrangorUpdateDbo.overordnetEnhetNavn,
			"overordnetEnhetOrganisasjonsnummer" to arrangorUpdateDbo.overordnetEnhetOrganisasjonsnummer,
			"modifiedAt" to arrangorUpdateDbo.modifiedAt,
		)

		template.update(sql, parameters)
	}

	fun updateUnderenheter(organisasjonsnummer: String, navn: String) {
		val sql = """
			UPDATE arrangor
			SET overordnet_enhet_navn = :navn, modified_at = CURRENT_TIMESTAMP
			WHERE overordnet_enhet_organisasjonsnummer = :organisasjonsnummer
		""".trimIndent()

		val parameters = sqlParameters(
			"navn" to navn,
			"organisasjonsnummer" to organisasjonsnummer,
		)

		template.update(sql, parameters)
	}

}

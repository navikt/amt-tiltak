package no.nav.amt.tiltak.tiltaksleverandor.repositories

import no.nav.amt.tiltak.tiltaksleverandor.dbo.TiltaksleverandorDbo
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
open class TiltaksleverandorRepository(
    private val template: NamedParameterJdbcTemplate,
) {

    private val rowMapper = RowMapper { rs, _ ->
        TiltaksleverandorDbo(
            id = UUID.fromString(rs.getString("id")),
            navn = rs.getString("navn"),
            organisasjonsnummer = rs.getString("organisasjonsnummer"),
            overordnetEnhetNavn = rs.getString("overordnet_enhet_navn"),
            overordnetEnhetOrganisasjonsnummer = rs.getString("overordnet_enhet_organisasjonsnummer"),
            createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
            modifiedAt = rs.getTimestamp("modified_at").toLocalDateTime()
        )
    }


    fun insert(
		navn: String,
		organisasjonsnummer: String,
		overordnetEnhetNavn: String?,
		overordnetEnhetOrganisasjonsnummer: String?,
    ): TiltaksleverandorDbo {
        val savedTiltaksleverandor = getByOrganisasjonsnummer(organisasjonsnummer)

        if (savedTiltaksleverandor != null) {
            return savedTiltaksleverandor
        }

        val sql = """
			INSERT INTO tiltaksleverandor(id, overordnet_enhet_organisasjonsnummer, overordnet_enhet_navn, organisasjonsnummer, navn)
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

    fun getByOrganisasjonsnummer(organisasjonsnummer: String): TiltaksleverandorDbo? {
        val sql = """
			SELECT id,
				   overordnet_enhet_organisasjonsnummer,
				   overordnet_enhet_navn,
				   organisasjonsnummer,
				   navn,
				   created_at,
				   modified_at
			FROM tiltaksleverandor
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

}

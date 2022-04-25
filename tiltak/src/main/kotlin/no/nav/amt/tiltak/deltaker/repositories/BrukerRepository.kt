package no.nav.amt.tiltak.deltaker.repositories

import no.nav.amt.tiltak.deltaker.dbo.BrukerDbo
import no.nav.amt.tiltak.deltaker.dbo.BrukerInsertDbo
import no.nav.amt.tiltak.utils.getNullableUUID
import no.nav.amt.tiltak.utils.getUUID
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
open class BrukerRepository(
    private val template: NamedParameterJdbcTemplate
) {

    private val rowMapper = RowMapper { rs, _ ->
        BrukerDbo(
			id = rs.getUUID("id"),
            fodselsnummer = rs.getString("fodselsnummer"),
            fornavn = rs.getString("fornavn"),
			mellomnavn = rs.getString("mellomnavn"),
            etternavn = rs.getString("etternavn"),
            telefonnummer = rs.getString("telefonnummer"),
            epost = rs.getString("epost"),
            ansvarligVeilederId = rs.getNullableUUID("ansvarlig_veileder_id"),
			navEnhetId = rs.getNullableUUID("nav_enhet_id"),
            createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
            modifiedAt = rs.getTimestamp("modified_at").toLocalDateTime()
        )
    }

    fun insert(bruker: BrukerInsertDbo): BrukerDbo {

		val sql = """
			INSERT INTO bruker(id, fodselsnummer, fornavn, mellomnavn, etternavn, telefonnummer, epost, ansvarlig_veileder_id, nav_enhet_id)
			VALUES (:id,
					:fodselsnummer,
					:fornavn,
					:mellomnavn,
					:etternavn,
					:telefonnummer,
					:epost,
					:veileder_id,
					:nav_enhet_id)
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
            mapOf(
				"id" to UUID.randomUUID(),
                "fodselsnummer" to bruker.fodselsnummer,
                "fornavn" to bruker.fornavn,
				"mellomnavn" to bruker.mellomnavn,
                "etternavn" to bruker.etternavn,
                "telefonnummer" to bruker.telefonnummer,
                "epost" to bruker.epost,
                "veileder_id" to bruker.ansvarligVeilederId,
				"nav_enhet_id" to bruker.navEnhetId
            )
        )

        template.update(sql, parameters)

        return get(bruker.fodselsnummer)
            ?: throw NoSuchElementException("Bruker med id ${bruker.fodselsnummer} finnes ikke")
    }

    fun get(fodselsnummer: String): BrukerDbo? {
        val sql = """
			SELECT *
			FROM bruker
			WHERE fodselsnummer = :fodselsnummer
		""".trimIndent()

        val parameters = MapSqlParameterSource().addValues(
            mapOf(
                "fodselsnummer" to fodselsnummer
            )
        )

        return template.query(sql, parameters, rowMapper)
            .firstOrNull()
    }

	fun oppdaterVeileder(fodselsnummer: String, veilederId: UUID) {
		val sql = """
			UPDATE bruker SET ansvarlig_veileder_id = :veilederId WHERE fodselsnummer = :fodselsnummer
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"veilederId" to veilederId,
				"fodselsnummer" to fodselsnummer,
			)
		)

		template.update(sql, parameters)
	}

	fun oppdaterNavEnhet(fodselsnummer: String, navEnhetId: UUID) {
		val sql = """
			UPDATE bruker SET nav_enhet_id = :navEnhetId WHERE fodselsnummer = :fodselsnummer
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"navEnhetId" to navEnhetId,
				"fodselsnummer" to fodselsnummer,
			)
		)

		template.update(sql, parameters)
	}
}

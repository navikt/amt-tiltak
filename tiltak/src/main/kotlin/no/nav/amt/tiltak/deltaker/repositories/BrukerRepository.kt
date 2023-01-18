package no.nav.amt.tiltak.deltaker.repositories

import no.nav.amt.tiltak.deltaker.dbo.BrukerDbo
import no.nav.amt.tiltak.deltaker.dbo.BrukerUpsertDbo
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
            personIdent = rs.getString("person_ident"),
            fornavn = rs.getString("fornavn"),
			mellomnavn = rs.getString("mellomnavn"),
            etternavn = rs.getString("etternavn"),
            telefonnummer = rs.getString("telefonnummer"),
            epost = rs.getString("epost"),
            ansvarligVeilederId = rs.getNullableUUID("ansvarlig_veileder_id"),
			navEnhetId = rs.getNullableUUID("nav_enhet_id"),
			erSkjermet = rs.getBoolean("er_skjermet"),
            createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
            modifiedAt = rs.getTimestamp("modified_at").toLocalDateTime()
        )
    }

    fun upsert(bruker: BrukerUpsertDbo): BrukerDbo {
		val sql = """
			INSERT INTO bruker(id, person_ident, fornavn, mellomnavn, etternavn, telefonnummer, epost, ansvarlig_veileder_id, nav_enhet_id, er_skjermet)
			VALUES (:id,
					:personIdent,
					:fornavn,
					:mellomnavn,
					:etternavn,
					:telefonnummer,
					:epost,
					:veileder_id,
					:nav_enhet_id,
					:er_skjermet)
			ON CONFLICT(person_ident) DO UPDATE SET
			 	fornavn = :fornavn,
				mellomnavn = :mellomnavn,
				etternavn = :etternavn,
				telefonnummer = :telefonnummer,
				epost = :epost,
				ansvarlig_veileder_id = :veileder_id,
				nav_enhet_id = :nav_enhet_id,
				modified_at = CURRENT_TIMESTAMP,
				er_skjermet = :er_skjermet
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
            mapOf(
				"id" to UUID.randomUUID(),
                "personIdent" to bruker.personIdent,
                "fornavn" to bruker.fornavn,
				"mellomnavn" to bruker.mellomnavn,
                "etternavn" to bruker.etternavn,
                "telefonnummer" to bruker.telefonnummer,
                "epost" to bruker.epost,
                "veileder_id" to bruker.ansvarligVeilederId,
				"nav_enhet_id" to bruker.navEnhetId,
				"er_skjermet" to bruker.erSkjermet
            )
        )

        template.update(sql, parameters)

        return get(bruker.personIdent)
            ?: throw NoSuchElementException("Bruker med id ${bruker.personIdent} finnes ikke")
    }

    fun get(personIdent: String): BrukerDbo? {
        val sql = """
			SELECT *
			FROM bruker
			WHERE person_ident = :personIdent
		""".trimIndent()

        val parameters = MapSqlParameterSource().addValues(
            mapOf(
                "personIdent" to personIdent
            )
        )

        return template.query(sql, parameters, rowMapper)
            .firstOrNull()
    }

	fun get(id: UUID): BrukerDbo? {
		val sql = """
			SELECT *
			FROM bruker
			WHERE id = :id
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"id" to id
			)
		)

		return template.query(sql, parameters, rowMapper)
			.firstOrNull()
	}
	fun oppdaterVeileder(personIdent: String, veilederId: UUID) {
		val sql = """
			UPDATE bruker SET ansvarlig_veileder_id = :veilederId, modified_at = CURRENT_TIMESTAMP
			WHERE person_ident = :personIdent
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"veilederId" to veilederId,
				"personIdent" to personIdent,
			)
		)

		template.update(sql, parameters)
	}

	fun oppdaterNavEnhet(personIdent: String, navEnhetId: UUID?) {
		val sql = """
			UPDATE bruker
			SET nav_enhet_id = :navEnhetId, modified_at = CURRENT_TIMESTAMP
			WHERE person_ident = :personIdent
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"navEnhetId" to navEnhetId,
				"personIdent" to personIdent,
			)
		)

		template.update(sql, parameters)
	}

	fun settSkjermet(personIdent: String, erSkjermet: Boolean) {
		val sql = """
			UPDATE bruker SET er_skjermet = :erSkjermet, modified_at = CURRENT_TIMESTAMP
			WHERE person_ident = :personIdent
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
			mapOf(
				"erSkjermet" to erSkjermet,
				"personIdent" to personIdent,
			)
		)

		template.update(sql, parameters)
	}
}

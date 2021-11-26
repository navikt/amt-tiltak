package no.nav.amt.tiltak.tiltak.deltaker.repositories

import no.nav.amt.tiltak.core.domain.tiltak.Fodselsnummer
import no.nav.amt.tiltak.tiltak.deltaker.dbo.BrukerDbo
import no.nav.amt.tiltak.tiltak.utils.getNullableUUID
import no.nav.amt.tiltak.tiltak.utils.getUUID
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
		val veileder = rs.getString("ansvarlig_veileder_internal_id")

        BrukerDbo(
			id = rs.getUUID("id"),
            fodselsnummer = Fodselsnummer(rs.getString("fodselsnummer")),
            fornavn = rs.getString("fornavn"),
			mellomnavn = rs.getString("mellomnavn"),
            etternavn = rs.getString("etternavn"),
            telefonnummer = rs.getString("telefonnummer"),
            epost = rs.getString("epost"),
            ansvarligVeilederId = rs.getNullableUUID("ansvarlig_veileder_internal_id"),
            createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
            modifiedAt = rs.getTimestamp("modified_at").toLocalDateTime()
        )

    }

    fun insert(
        fodselsnummer: String,
        fornavn: String,
		mellomnavn: String?,
        etternavn: String,
        telefonnummer: String?,
        epost: String?,
        ansvarligVeilederId: UUID?
    ): BrukerDbo {

        val sql = """
			INSERT INTO bruker(id, fodselsnummer, fornavn, mellomnavn, etternavn, telefonnummer, epost, ansvarlig_veileder_id)
			VALUES (:id,
					:fodselsnummer,
					:fornavn,
					:mellomnavn,
					:etternavn,
					:telefonnummer,
					:epost,
					:veileder_id)
		""".trimIndent()

		val parameters = MapSqlParameterSource().addValues(
            mapOf(
				"id" to UUID.randomUUID(),
                "fodselsnummer" to fodselsnummer,
                "fornavn" to fornavn,
				"mellomnavn" to mellomnavn,
                "etternavn" to etternavn,
                "telefonnummer" to telefonnummer,
                "epost" to epost,
                "veileder_id" to ansvarligVeilederId
            )
        )

        template.update(sql, parameters)

        return get(fodselsnummer)
            ?: throw NoSuchElementException("Bruker med id $fodselsnummer finnes ikke")
    }

    fun get(fodselsnummer: String): BrukerDbo? {
        val sql = """
			SELECT id                    as id,
				   fodselsnummer         as fodselsnummer,
				   fornavn               as fornavn,
				   mellomnavn            as mellomnavn,
				   etternavn             as etternavn,
				   telefonnummer         as telefonnummer,
				   epost                 as epost,
				   ansvarlig_veileder_id as ansvarlig_veileder_internal_id,
				   created_at            as created_at,
				   modified_at           as modified_at
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

}

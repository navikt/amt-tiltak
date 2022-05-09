package no.nav.amt.tiltak.endringsmelding

import no.nav.amt.tiltak.utils.getNullableUUID
import no.nav.amt.tiltak.utils.getUUID
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component
import java.util.*

@Component
class EndringsmeldingForGjennomforingQuery(
	private val template: NamedParameterJdbcTemplate
) {
	private val rowMapper = RowMapper { rs, _ ->
		EndringsmeldingForGjennomforingDbo(
			id = rs.getUUID("id"),
			deltakerId = rs.getUUID("deltaker_id"),

			brukerId = rs.getUUID("bruker_id"),
			brukerFornavn = rs.getString("fornavn"),
			brukerEtternavn = rs.getString("etternavn"),
			brukerMellomnavn = rs.getString("mellomnavn"),

			brukerFnr = rs.getString("fodselsnummer"),
			startDato = rs.getDate("start_dato")?.toLocalDate(),
			godkjentAvNavAnsatt = rs.getNullableUUID("godkjent_av_nav_ansatt"),
			aktiv = rs.getBoolean("aktiv"),

			opprettetAvId = rs.getUUID("opprettet_av_id"),
			opprettetAvFornavn = rs.getString("opprettet_av_fornavn"),
			opprettetAvEtternavn = rs.getString("opprettet_av_etternavn"),
			opprettetAvMellomnavn = rs.getString("opprettet_av_mellomnavn"),
			opprettetAvPersonligIdent = rs.getString("opprettet_av_ident"),

			navEnhetId = rs.getNullableUUID("nav_enhet_id"),
			navEnhetNorgId = rs.getString("nav_enhet_enhet_id"),
			navEnhetNavn = rs.getString("nav_enhet_navn"),

			createdAt = rs.getTimestamp("created_at").toLocalDateTime(),
			modifiedAt = rs.getTimestamp("modified_at").toLocalDateTime()
		)
	}

	fun query(gjennomforingId: UUID): List<EndringsmeldingForGjennomforingDbo> {
		val sql = """
			SELECT endringsmelding.*,
			bruker.fornavn,
			bruker.mellomnavn,
			bruker.etternavn,
			bruker.id as bruker_id,
			bruker.fodselsnummer,
			nav_enhet.id as nav_enhet_id,
			nav_enhet.navn as nav_enhet_navn,
			nav_enhet.enhet_id as nav_enhet_enhet_id,
			arrangor_ansatt.id as opprettet_av_id,
			arrangor_ansatt.fornavn as opprettet_av_fornavn,
			arrangor_ansatt.mellomnavn as opprettet_av_mellomnavn,
			arrangor_ansatt.etternavn as opprettet_av_etternavn,
			arrangor_ansatt.personlig_ident as opprettet_av_ident,
			deltaker.id as deltaker_id
			FROM endringsmelding
			JOIN deltaker on endringsmelding.deltaker_id = deltaker.id
			JOIN arrangor_ansatt on endringsmelding.opprettet_av = arrangor_ansatt.id
			JOIN bruker on deltaker.bruker_id = bruker.id
			LEFT JOIN nav_enhet on bruker.nav_enhet_id = nav_enhet.id
			WHERE deltaker.gjennomforing_id = :gjennomforing_id
		""".trimIndent()

		val param = MapSqlParameterSource().addValue("gjennomforing_id", gjennomforingId)
		return template.query(sql, param, rowMapper)
	}
}

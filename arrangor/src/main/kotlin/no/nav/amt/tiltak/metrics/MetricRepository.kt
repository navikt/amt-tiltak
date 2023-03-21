package no.nav.amt.tiltak.metrics

import no.nav.amt.tiltak.core.domain.tilgangskontroll.ArrangorAnsattRolle
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component

@Component
open class MetricRepository(
	private val template: NamedParameterJdbcTemplate
) {
	private val deltakerMedVeilederRowMapper = RowMapper { rs, _ ->
		DeltakereMedOgUtenVeileder(
			antallMedVeileder = rs.getInt("antall_med"),
			totalAntallDeltakere = rs.getInt("total_antall"),

		)
	}

	fun hentAndelAktiveDeltakereMedVeileder(): DeltakereMedOgUtenVeileder {
		//language=PostgreSQL
		val sql = """
			with
			aktive_deltakere as (
				select distinct(deltaker.id) as deltakerId
				from arrangor_ansatt_gjennomforing_tilgang tilgang
						 join deltaker on deltaker.gjennomforing_id = tilgang.gjennomforing_id
						 join deltaker_status on deltaker.id = deltaker_status.deltaker_id
						 join gjennomforing on deltaker.gjennomforing_id = gjennomforing.id
				where gjennomforing.status = 'GJENNOMFORES'
				  and deltaker_status.aktiv = true
				  and deltaker_status.status in('DELTAR', 'VENTER_PA_OPPSTART')
			),
			antall_aktive_deltakere as (
				select count(*) as total_antall
				from aktive_deltakere
			),
			antall_med_veileder as (
				select count(distinct deltaker_id) as antall_med
				from arrangor_veileder
						 join aktive_deltakere on arrangor_veileder.deltaker_id = aktive_deltakere.deltakerId
				where arrangor_veileder.gyldig_til > CURRENT_TIMESTAMP
			)
			select antall_aktive_deltakere.total_antall, antall_med_veileder.antall_med
			from antall_aktive_deltakere, antall_med_veileder;
		""".trimIndent()

		return template.query(sql, deltakerMedVeilederRowMapper).first()
	}


	fun getSistInnloggetMetrics(): SistInnlogget {
		val sql = """
			select
    		(select count(*) as antall_ansatte from arrangor_ansatt as antall_ansatte),
    		(select count(*) as logged_in_last_day from arrangor_ansatt where sist_velykkede_innlogging BETWEEN NOW() - INTERVAL '24 HOURS' AND NOW()),
    		(select count(*) as logged_in_last_week from arrangor_ansatt where sist_velykkede_innlogging BETWEEN NOW() - INTERVAL '7 DAYS' AND NOW())
		""".trimIndent()

		return template.query(sql) { rs, _ ->
			SistInnlogget(
				rs.getInt("antall_ansatte"),
				rs.getInt("logged_in_last_day"),
				rs.getInt("logged_in_last_week"),
			)
		}.first()
	}

	fun getRolleInnloggetSisteTime(): SistInnloggedeRoller {
		//language=PostgreSQL
		val sql = """
			with
				ansatte_logget_inn as (
					select distinct on (ansatt_id, rolle.rolle) ansatt_id, arrangor_ansatt.fornavn, rolle.rolle, sist_velykkede_innlogging
					from arrangor_ansatt join arrangor_ansatt_rolle rolle on arrangor_ansatt.id = rolle.ansatt_id
					where rolle.gyldig_til > current_timestamp
					and sist_velykkede_innlogging BETWEEN NOW() - INTERVAL '1 HOUR' AND NOW()
				),
				ansatte_veiledere as (
					select ansatt_id, sist_velykkede_innlogging
					from ansatte_logget_inn
					where rolle='${ArrangorAnsattRolle.VEILEDER.name}'
				),
				ansatte_koordinatorer as (
					select ansatt_id, sist_velykkede_innlogging
					from ansatte_logget_inn
					where rolle='${ArrangorAnsattRolle.KOORDINATOR.name}'
				),
				begge_roller as (
					select * from ansatte_veiledere intersect select * from ansatte_koordinatorer
				),
				bare_veiledere as (
					select *
					from ansatte_veiledere except select * from begge_roller
				),
				bare_koordinatorer as (
					select *
					from ansatte_koordinatorer except select * from begge_roller
				)
			select
				(select count(*) antall_koordinatorer from bare_koordinatorer),
				(select count(*) antall_veiledere from bare_veiledere),
				(select count(*) antall_begge from begge_roller),
				(select count(distinct ansatt_id) totalt_antall from ansatte_logget_inn)
		""".trimIndent()

		return template.query(sql) { rs, _ ->
			SistInnloggedeRoller(
				antallKoordinatorer = rs.getInt("antall_koordinatorer"),
				antallVeiledere = rs.getInt("antall_veiledere"),
				antallBegge = rs.getInt("antall_begge"),
				totaltAntallAnsatte = rs.getInt("totalt_antall")
			)
		}.first()


	}

	data class SistInnloggedeRoller (
		val antallKoordinatorer: Int,
		val antallVeiledere: Int,
		val antallBegge: Int,
		val totaltAntallAnsatte: Int
	)

	data class SistInnlogget(
		val antallAnsatte: Int,
		val antallAnsatteInnloggetSisteDag: Int,
		val antallAnsatteInnloggetSisteUke: Int
	)

	data class DeltakereMedOgUtenVeileder(
		val antallMedVeileder: Int,
		val totalAntallDeltakere: Int,
	)



}

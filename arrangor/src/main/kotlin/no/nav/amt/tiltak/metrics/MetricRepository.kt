package no.nav.amt.tiltak.metrics

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

	data class DeltakereMedOgUtenVeileder(
		val antallMedVeileder: Int,
		val totalAntallDeltakere: Int,
	)
}

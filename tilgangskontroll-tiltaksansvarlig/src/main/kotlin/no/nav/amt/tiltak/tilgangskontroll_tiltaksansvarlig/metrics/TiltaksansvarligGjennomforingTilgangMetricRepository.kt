package no.nav.amt.tiltak.tilgangskontroll_tiltaksansvarlig.metrics

import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.stereotype.Component

@Component
class TiltaksansvarligGjennomforingTilgangMetricRepository(
	val jdbcTemplate: NamedParameterJdbcTemplate,
) {
	fun antallGjennomforingerUtenTilgangerMedMeldinger(): Int {
		val sql = """
			SELECT COUNT(DISTINCT g.id) AS antall
			FROM gjennomforing g
					 JOIN deltaker d ON d.gjennomforing_id = g.id
					 JOIN endringsmelding e ON d.id = e.deltaker_id
			WHERE e.status = 'AKTIV' AND NOT EXISTS (
					SELECT gjennomforing_id
					FROM tiltaksansvarlig_gjennomforing_tilgang tgt
					WHERE d.gjennomforing_id = tgt.gjennomforing_id AND tgt.gyldig_til > current_timestamp
				);
		""".trimIndent()

		return jdbcTemplate.query(sql) { rs, _ -> rs.getInt("antall") }.first()
	}
}

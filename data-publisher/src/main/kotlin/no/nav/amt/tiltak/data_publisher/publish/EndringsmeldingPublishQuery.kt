package no.nav.amt.tiltak.data_publisher.publish

import no.nav.amt.tiltak.common.db_utils.DbUtils.sqlParameters
import no.nav.amt.tiltak.common.db_utils.getLocalDateTime
import no.nav.amt.tiltak.common.db_utils.getNullableLocalDateTime
import no.nav.amt.tiltak.common.db_utils.getNullableUUID
import no.nav.amt.tiltak.common.db_utils.getUUID
import no.nav.amt.tiltak.data_publisher.model.EndringsmeldingPublishDto
import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.util.*

class EndringsmeldingPublishQuery(
	private val template: NamedParameterJdbcTemplate
) {

	private val rowMapper = RowMapper { rs, _ ->
		EndringsmeldingPublishDto(
			id = rs.getUUID("id"),
			deltakerId = rs.getUUID("deltaker_id"),
			utfortAvNavAnsattId = rs.getNullableUUID("utfort_av_nav_ansatt_id"),
			opprettetAvArrangorAnsattId = rs.getUUID("opprettet_av_arrangor_ansatt_id"),
			utfortTidspunkt = rs.getNullableLocalDateTime("utfort_tidspunkt"),
			status = rs.getString("status"),
			type = rs.getString("type"),
			innhold = rs.getString("innhold"),
			createdAt = rs.getLocalDateTime("created_at")
		)
	}

	fun execute(id: UUID): EndringsmeldingPublishDto {
		return template.query(
			"SELECT * FROM endringsmelding WHERE id = :id",
			sqlParameters("id" to id),
			rowMapper
		).first()
	}

}

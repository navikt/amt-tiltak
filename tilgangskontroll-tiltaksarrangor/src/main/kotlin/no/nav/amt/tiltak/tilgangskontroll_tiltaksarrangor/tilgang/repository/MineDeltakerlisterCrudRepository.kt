package no.nav.amt.tiltak.tilgangskontroll_tiltaksarrangor.tilgang.repository

import org.springframework.data.jdbc.repository.query.Modifying
import org.springframework.data.jdbc.repository.query.Query
import org.springframework.data.repository.CrudRepository
import java.util.UUID


interface MineDeltakerlisterCrudRepository
	: CrudRepository<ArrangorAnsattGjennomforingTilgangEntity, UUID>,
	WithInsert<ArrangorAnsattGjennomforingTilgangEntity> {

	@Modifying
	@Query(
		"""
		UPDATE arrangor_ansatt_gjennomforing_tilgang
		SET gyldig_til = current_timestamp
		WHERE
			ansatt_id = :arrangorAnsattId
			AND gjennomforing_id = :gjennomforingId
			AND gyldig_til > current_timestamp"""
	)
	fun fjern(arrangorAnsattId: UUID, gjennomforingId: UUID)

	@Query(
		"""
		SELECT *
		FROM arrangor_ansatt_gjennomforing_tilgang
		WHERE
			ansatt_id = :ansattId
			AND gyldig_fra < current_timestamp
			AND gyldig_til > current_timestamp"""
	)
	fun hent(ansattId: UUID): List<ArrangorAnsattGjennomforingTilgangEntity>

	@Query(
		"""
			SELECT
				ansatt_id,
				COUNT(gjennomforing_id) AS gjennomforinger
			FROM
				arrangor_ansatt_gjennomforing_tilgang
			WHERE
				gyldig_fra < current_timestamp
				AND gyldig_til > current_timestamp
			GROUP BY ansatt_id
			ORDER BY gjennomforinger DESC"""
	)
	fun hentAntallPerAnsatt(): List<AnsattGjennomforingCount>
}


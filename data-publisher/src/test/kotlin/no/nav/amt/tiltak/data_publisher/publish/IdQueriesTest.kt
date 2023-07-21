package no.nav.amt.tiltak.data_publisher.publish

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldNotContain
import no.nav.amt.tiltak.data_publisher.DatabaseTestDataHandler
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import java.time.LocalDateTime

class IdQueriesTest : FunSpec({
	val dataSource = SingletonPostgresContainer.getDataSource()
	val template = NamedParameterJdbcTemplate(dataSource)

	val idQueries = IdQueries(template)
	val db = DatabaseTestDataHandler(template)

	beforeEach {
		DbTestDataUtils.cleanDatabase(dataSource)
	}

	afterEach {
		DbTestDataUtils.cleanDatabase(dataSource)
	}

	test("hentDeltakerIds - henter alle som har modifiedAfter dato") {
		val oldDeltaker = db.createDeltaker(
			registrertDato = LocalDateTime.now().minusDays(30),
			endretDato = LocalDateTime.now().minusDays(30)
		)

		val newDeltaker1 = db.createDeltaker(
			registrertDato = LocalDateTime.now().minusDays(30),
			endretDato = LocalDateTime.now().minusDays(1)
		)

		val newDeltaker2 = db.createDeltaker()

		val ids = idQueries.hentDeltakerIds(0, 10, LocalDateTime.now().minusDays(2))

		ids shouldNotContain oldDeltaker.id
		ids shouldContain newDeltaker1.id
		ids shouldContain newDeltaker2.id
	}

	test("hentDeltakerlisteIds - henter alle som har modifiedAfter dato") {
		val oldDeltakerliste = db.createDeltakerliste(
			createdAt = LocalDateTime.now().minusDays(30),
			modifiedAt = LocalDateTime.now().minusDays(30)
		)

		val newDeltakerliste1 = db.createDeltakerliste(
			createdAt = LocalDateTime.now().minusDays(30),
			modifiedAt = LocalDateTime.now().minusDays(1)
		)

		val newDeltakerliste2 = db.createDeltakerliste()

		val ids = idQueries.hentDeltakerlisteIds(0, 10, LocalDateTime.now().minusDays(2))

		ids shouldNotContain oldDeltakerliste.id
		ids shouldContain newDeltakerliste1.id
		ids shouldContain newDeltakerliste2.id

	}

	test("hentEndringsmeldingIds - henter alle som har modifiedAfter dato") {
		val oldEndringsmelding = db.createEndringsmelding(
			createdAt = LocalDateTime.now().minusDays(30),
			modifiedAt = LocalDateTime.now().minusDays(30)
		)

		val newEndringsmelding1 = db.createEndringsmelding(
			createdAt = LocalDateTime.now().minusDays(30),
			modifiedAt = LocalDateTime.now().minusDays(1)
		)

		val newEndringsmelding2 = db.createEndringsmelding()

		val ids = idQueries.hentEndringsmeldingIds(0, 10, LocalDateTime.now().minusDays(2))

		ids shouldNotContain oldEndringsmelding.id
		ids shouldContain newEndringsmelding1.id
		ids shouldContain newEndringsmelding2.id
	}


})

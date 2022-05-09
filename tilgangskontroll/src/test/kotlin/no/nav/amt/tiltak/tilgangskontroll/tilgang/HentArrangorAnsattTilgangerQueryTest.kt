package no.nav.amt.tiltak.tilgangskontroll.tilgang

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import no.nav.amt.tiltak.test.database.DbTestDataUtils
import no.nav.amt.tiltak.test.database.SingletonPostgresContainer
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_TILGANG_1
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate

class HentArrangorAnsattTilgangerQueryTest : FunSpec({

	val dataSource = SingletonPostgresContainer.getDataSource()

	lateinit var query: HentArrangorAnsattTilgangerQuery

	beforeEach {
		val template = NamedParameterJdbcTemplate(dataSource)

		query = HentArrangorAnsattTilgangerQuery(template)

		DbTestDataUtils.cleanAndInitDatabaseWithTestData(dataSource)
	}

	test("Should query arrangor ansatt tilganger") {
		val tilganger = query.query(GJENNOMFORING_1.id)

		tilganger shouldHaveSize 1

		val tilgang = tilganger.first()

		tilgang.id shouldBe GJENNOMFORING_TILGANG_1.id
		tilgang.fornavn shouldBe "Ansatt 1 fornavn"
		tilgang.mellomnavn shouldBe "Ansatt 1 mellomnavn"
		tilgang.etternavn shouldBe "Ansatt 1 etternavn"
		tilgang.opprettetDato shouldNotBe null
		tilgang.opprettetAvNavIdent shouldBe null
	}

})


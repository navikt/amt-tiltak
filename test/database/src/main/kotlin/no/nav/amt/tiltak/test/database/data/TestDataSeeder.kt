package no.nav.amt.tiltak.test.database.data

import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_2
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1_ROLLE_1
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_1_ROLLE_2
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_2
import no.nav.amt.tiltak.test.database.data.TestData.ARRANGOR_ANSATT_2_ROLLE_1
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_1
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_2
import no.nav.amt.tiltak.test.database.data.TestData.BRUKER_3
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1_STATUS_1
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_2
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_2_STATUS_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_2
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_TILGANG_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ANSATT_2
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ENHET_1
import no.nav.amt.tiltak.test.database.data.TestData.NAV_ENHET_2
import no.nav.amt.tiltak.test.database.data.TestData.TILTAK_1
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.sql.DataSource

object TestDataSeeder {

	fun insertDefaultTestData(repository: TestDataRepository) {
		repository.insertNavEnhet(NAV_ENHET_1)
		repository.insertNavEnhet(NAV_ENHET_2)

		repository.insertArrangor(ARRANGOR_1)
		repository.insertArrangor(ARRANGOR_2)

		repository.insertArrangorAnsatt(ARRANGOR_ANSATT_1)
		repository.insertArrangorAnsattRolle(ARRANGOR_ANSATT_1_ROLLE_1)
		repository.insertArrangorAnsattRolle(ARRANGOR_ANSATT_1_ROLLE_2)

		repository.insertArrangorAnsatt(ARRANGOR_ANSATT_2)
		repository.insertArrangorAnsattRolle(ARRANGOR_ANSATT_2_ROLLE_1)

		repository.insertTiltak(TILTAK_1)
		repository.insertGjennomforing(GJENNOMFORING_1)
		repository.insertGjennomforing(GJENNOMFORING_2)

		repository.insertArrangorAnsattGjennomforingTilgang(GJENNOMFORING_TILGANG_1)

		repository.insertNavAnsatt(NAV_ANSATT_1)
		repository.insertNavAnsatt(NAV_ANSATT_2)

		repository.insertBruker(BRUKER_1)
		repository.insertDeltaker(DELTAKER_1)
		repository.insertDeltakerStatus(DELTAKER_1_STATUS_1)

		repository.insertBruker(BRUKER_2)
		repository.insertDeltaker(DELTAKER_2)
		repository.insertDeltakerStatus(DELTAKER_2_STATUS_1)

		repository.insertBruker(BRUKER_3)
	}

	fun insertMinimum(repository: TestDataRepository){
		repository.insertNavEnhet(NAV_ENHET_1)

		repository.insertArrangor(ARRANGOR_1)

		repository.insertArrangorAnsatt(ARRANGOR_ANSATT_1)
		repository.insertArrangorAnsattRolle(ARRANGOR_ANSATT_1_ROLLE_1)

		repository.insertTiltak(TILTAK_1)
		repository.insertGjennomforing(GJENNOMFORING_1)

		repository.insertArrangorAnsattGjennomforingTilgang(GJENNOMFORING_TILGANG_1)

		repository.insertNavAnsatt(NAV_ANSATT_1)

		repository.insertBruker(BRUKER_1)

	}

	fun seed(dataSource: DataSource, testDataInserter: (TestDataRepository) -> Unit = {}) =
		testDataInserter(TestDataRepository(NamedParameterJdbcTemplate(dataSource)))

}

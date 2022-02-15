package no.nav.amt.tiltak.test.database.data

import no.nav.amt.tiltak.test.database.data.TestData1.ARRANGOR_1
import no.nav.amt.tiltak.test.database.data.TestData1.ARRANGOR_2
import no.nav.amt.tiltak.test.database.data.TestData1.ARRANGOR_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData1.ARRANGOR_ANSATT_1_ROLLE_1
import no.nav.amt.tiltak.test.database.data.TestData1.ARRANGOR_ANSATT_1_ROLLE_2
import no.nav.amt.tiltak.test.database.data.TestData1.ARRANGOR_ANSATT_2
import no.nav.amt.tiltak.test.database.data.TestData1.ARRANGOR_ANSATT_2_ROLLE_1
import no.nav.amt.tiltak.test.database.data.TestData1.BRUKER_1
import no.nav.amt.tiltak.test.database.data.TestData1.BRUKER_2
import no.nav.amt.tiltak.test.database.data.TestData1.BRUKER_3
import no.nav.amt.tiltak.test.database.data.TestData1.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData1.DELTAKER_1_STATUS_1
import no.nav.amt.tiltak.test.database.data.TestData1.DELTAKER_2
import no.nav.amt.tiltak.test.database.data.TestData1.DELTAKER_2_STATUS_1
import no.nav.amt.tiltak.test.database.data.TestData1.GJENNOMFORING_1
import no.nav.amt.tiltak.test.database.data.TestData1.NAV_ANSATT_1
import no.nav.amt.tiltak.test.database.data.TestData1.NAV_ANSATT_2
import no.nav.amt.tiltak.test.database.data.TestData1.NAV_KONTOR_1
import no.nav.amt.tiltak.test.database.data.TestData1.TILTAK_1
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import javax.sql.DataSource

object TestDataSeeder {

	fun seed(dataSource: DataSource) {
		val repository = TestDataRepository(NamedParameterJdbcTemplate(dataSource))

		repository.insertArrangor(ARRANGOR_1)
		repository.insertArrangor(ARRANGOR_2)

		repository.insertArrangorAnsatt(ARRANGOR_ANSATT_1)
		repository.insertArrangorAnsattRolle(ARRANGOR_ANSATT_1_ROLLE_1)
		repository.insertArrangorAnsattRolle(ARRANGOR_ANSATT_1_ROLLE_2)

		repository.insertArrangorAnsatt(ARRANGOR_ANSATT_2)
		repository.insertArrangorAnsattRolle(ARRANGOR_ANSATT_2_ROLLE_1)

		repository.insertTiltak(TILTAK_1)
		repository.insertGjennomforing(GJENNOMFORING_1)

		repository.insertNavAnsatt(NAV_ANSATT_1)
		repository.insertNavAnsatt(NAV_ANSATT_2)

		repository.insertNavKontor(NAV_KONTOR_1)

		repository.insertBruker(BRUKER_1)
		repository.insertDeltaker(DELTAKER_1)
		repository.insertDeltakerStatus(DELTAKER_1_STATUS_1)

		repository.insertBruker(BRUKER_2)
		repository.insertDeltaker(DELTAKER_2)
		repository.insertDeltakerStatus(DELTAKER_2_STATUS_1)

		repository.insertBruker(BRUKER_3)
	}

}

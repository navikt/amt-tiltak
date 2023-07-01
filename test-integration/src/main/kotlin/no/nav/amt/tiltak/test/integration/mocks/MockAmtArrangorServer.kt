package no.nav.amt.tiltak.test.integration.mocks

import no.nav.amt.tiltak.clients.amt_arrangor_client.AmtArrangorClient
import no.nav.amt.tiltak.common.json.JsonUtils
import no.nav.amt.tiltak.test.database.data.TestData
import no.nav.amt.tiltak.test.database.data.TestData.DELTAKER_1
import no.nav.amt.tiltak.test.database.data.TestData.GJENNOMFORING_1
import no.nav.amt.tiltak.test.integration.utils.MockHttpServer
import okhttp3.mockwebserver.MockResponse

class MockAmtArrangorServer : MockHttpServer("amt-arrangor-server") {

	fun reset() {
		resetHttpServer()
	}

	fun addDefaultData() {
		addAnsattResponse(
			ansattDto = AmtArrangorClient.AnsattDto(
				id = TestData.ARRANGOR_ANSATT_1.id,
				personalia = AmtArrangorClient.PersonaliaDto(
					TestData.ARRANGOR_ANSATT_1.personligIdent, null, AmtArrangorClient.Navn(
					TestData.ARRANGOR_ANSATT_1.fornavn, TestData.ARRANGOR_ANSATT_1.mellomnavn, TestData.ARRANGOR_ANSATT_1.etternavn)),
				arrangorer = listOf(
					AmtArrangorClient.TilknyttetArrangorDto(
						arrangorId = TestData.ARRANGOR_1.id,
						arrangor = AmtArrangorClient.Arrangor(TestData.ARRANGOR_1.id, TestData.ARRANGOR_1.navn, TestData.ARRANGOR_1.organisasjonsnummer),
						overordnetArrangor = null,
						roller = listOf(AmtArrangorClient.AnsattRolle.KOORDINATOR),
						veileder = listOf(AmtArrangorClient.VeilederDto(DELTAKER_1.id, AmtArrangorClient.VeilederType.VEILEDER)),
						koordinator = listOf(GJENNOMFORING_1.id)
					),
					AmtArrangorClient.TilknyttetArrangorDto(
						arrangorId = TestData.ARRANGOR_2.id,
						arrangor = AmtArrangorClient.Arrangor(TestData.ARRANGOR_2.id, TestData.ARRANGOR_2.navn, TestData.ARRANGOR_2.organisasjonsnummer),
						overordnetArrangor = null,
						roller = listOf(AmtArrangorClient.AnsattRolle.VEILEDER),
						veileder = emptyList(),
						koordinator = emptyList()
					)
				)
			)
		)
		addAnsattResponse(
			ansattDto = AmtArrangorClient.AnsattDto(
				id = TestData.ARRANGOR_ANSATT_2.id,
				personalia = AmtArrangorClient.PersonaliaDto(
					TestData.ARRANGOR_ANSATT_2.personligIdent, null, AmtArrangorClient.Navn(
					TestData.ARRANGOR_ANSATT_2.fornavn, TestData.ARRANGOR_ANSATT_2.mellomnavn, TestData.ARRANGOR_ANSATT_2.etternavn)),
				arrangorer = listOf(
					AmtArrangorClient.TilknyttetArrangorDto(
						arrangorId = TestData.ARRANGOR_1.id,
						arrangor = AmtArrangorClient.Arrangor(TestData.ARRANGOR_1.id, TestData.ARRANGOR_1.navn, TestData.ARRANGOR_1.organisasjonsnummer),
						overordnetArrangor = null,
						roller = listOf(AmtArrangorClient.AnsattRolle.VEILEDER),
						veileder = listOf(AmtArrangorClient.VeilederDto(DELTAKER_1.id, AmtArrangorClient.VeilederType.VEILEDER)),
						koordinator = emptyList()
					)
				)
			)
		)
		addAnsattResponse(
			ansattDto = AmtArrangorClient.AnsattDto(
				id = TestData.ARRANGOR_ANSATT_3.id,
				personalia = AmtArrangorClient.PersonaliaDto(
					TestData.ARRANGOR_ANSATT_3.personligIdent, null, AmtArrangorClient.Navn(
						TestData.ARRANGOR_ANSATT_3.fornavn, TestData.ARRANGOR_ANSATT_3.mellomnavn, TestData.ARRANGOR_ANSATT_3.etternavn)),
				arrangorer = emptyList()
			)
		)
	}

	fun addAnsattResponse(
		ansattDto: AmtArrangorClient.AnsattDto
	) {
		val response = MockResponse()
			.setResponseCode(200)
			.setBody(JsonUtils.toJsonString(ansattDto))

		addResponseHandler("/api/service/ansatt", response)
	}

	fun addArrangorResponse(
		arrangor: AmtArrangorClient.ArrangorMedOverordnetArrangor
	) {
		val response = MockResponse()
			.setResponseCode(200)
			.setBody(JsonUtils.toJsonString(arrangor))

		addResponseHandler("/api/service/arrangor/organisasjonsnummer/${arrangor.organisasjonsnummer}", response)
	}
}

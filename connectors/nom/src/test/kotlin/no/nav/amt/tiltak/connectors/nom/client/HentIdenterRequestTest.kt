package no.nav.amt.tiltak.connectors.nom.client

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.assertions.json.shouldContainJsonKeyValue
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldNotBe

class HentIdenterRequestTest : StringSpec({

	val identerJsonPath = "$.variables.identer"
	"HentIdenterRequest - inneholder rett ident som variabel" {
		HentIdenterRequest(listOf("A11111")).asJson()
			.shouldContainJsonKeyValue(identerJsonPath, listOf("A11111"))
	}

	"HentIdenterRequest - inneholder alle identer den mottar" {
		HentIdenterRequest(listOf("A11111", "B222")).asJson()
			.shouldContainJsonKeyValue(identerJsonPath, listOf("A11111", "B222"))
	}

	"HentIdenterRequest - inneholder ingen ident når den ikke mottar noen" {
		HentIdenterRequest(listOf()).asJson()
			.shouldContainJsonKeyValue(identerJsonPath, listOf<String>())
	}

	"HentIdenterRequest - skal produsere gyldig json" {
		ObjectMapper().readTree(HentIdenterRequest(listOf("A11111", "B222")).asJson()) shouldNotBe null
	}

})

package no.nav.amt.tools.mockdatagenerator

import no.nav.amt.tools.mockdatagenerator.person.GeneratePersonMockData
import no.nav.amt.tools.mockdatagenerator.tiltaksleverandor.GenerateTiltaksleverandorMockData

class MockDataGeneratorApplication {

	fun run() {
//		GeneratePersonMockData().generate()
		GenerateTiltaksleverandorMockData().generate()
	}

}

fun main() {
	MockDataGeneratorApplication().run()
}

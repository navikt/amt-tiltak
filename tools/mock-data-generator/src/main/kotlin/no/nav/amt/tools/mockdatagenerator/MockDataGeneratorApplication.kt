package no.nav.amt.tools.mockdatagenerator

import no.nav.amt.tools.mockdatagenerator.person.GeneratePersonMockData

class MockDataGeneratorApplication {

	fun run() {
		GeneratePersonMockData().generate()
	}

}

fun main() {
	MockDataGeneratorApplication().run()
}

package no.nav.amt.tools.mockdatagenerator

import no.nav.amt.tools.mockdatagenerator.tiltaksarrangor.GenerateTiltaksarrangorMockData

class MockDataGeneratorApplication {

	fun run() {
//		GeneratePersonMockData().generate()
		GenerateTiltaksarrangorMockData().generate()
	}

}

fun main() {
	MockDataGeneratorApplication().run()
}

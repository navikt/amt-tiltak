package no.nav.amt.tools.mockdatagenerator

import no.nav.amt.tools.mockdatagenerator.arrangor.GenerateArrangorMockData

class MockDataGeneratorApplication {

	fun run() {
//		GeneratePersonMockData().generate()
		GenerateArrangorMockData().generate()
	}

}

fun main() {
	MockDataGeneratorApplication().run()
}

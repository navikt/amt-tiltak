package no.nav.amt.tiltak.clients.pdl

object PdlClientImplTestData {

	val nullError = "- data i respons er null \n"
	val errorPrefix = "Feilmeldinger i respons fra pdl:\n"

	val minimalFeilRespons = """
				{
					"errors": [
						{
						  "message": "Ikke tilgang til å se person",
						  "locations": [
							{
							  "line": 2,
							  "column": 5
							}
						  ],
						  "path": [
							"hentPerson"
						  ],
						  "extensions": {
							"code": "unauthorized",
							"details": {
							  "type": "abac-deny",
							  "cause": "cause-0001-manglerrolle",
							  "policy": "adressebeskyttelse_strengt_fortrolig_adresse"
							},
							"classification": "ExecutionAborted"
						  }
						}
  					]
				}
			""".trimIndent()

	val flereFeilRespons = """
				{
					"errors": [
						{
						  "message": "Ikke tilgang til å se person",
						  "extensions": {
							"code": "unauthorized",
							"details": {
							  "type": "abac-deny",
							  "cause": "cause-0001-manglerrolle",
							  "policy": "adressebeskyttelse_strengt_fortrolig_adresse"
							}
						  }
						},

						{
						  "message": "Test",
						  "extensions": {
							"code": "unauthorized",
							"details": {
							  "type": "abac-deny",
							  "cause": "cause-0001-manglerrolle",
							  "policy": "adressebeskyttelse_strengt_fortrolig_adresse"
							}
						  }
						}
  					]
				}
			""".trimIndent()

	val gyldigRespons = """
		{
			"errors": null,
			"data": {
				"hentPerson": {
					"navn": [
						{
							"fornavn": "Tester",
							"mellomnavn": "Test",
							"etternavn": "Testersen"
						}
					],
					"telefonnummer": [
						{
							"landskode": "+47",
							"nummer": "98765432",
							"prioritet": 2
						},
						{
							"landskode": "+47",
							"nummer": "12345678",
							"prioritet": 1
						}
					],
					"adressebeskyttelse": [
						{
							"gradering": "FORTROLIG"
						}
					]
				}
			}
		}
	""".trimIndent()

}

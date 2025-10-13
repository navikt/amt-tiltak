package no.nav.amt.tiltak.core.port

import java.util.UUID

interface DataPublisherService {
	fun publishEnkeltplassDeltaker(id: UUID)
}

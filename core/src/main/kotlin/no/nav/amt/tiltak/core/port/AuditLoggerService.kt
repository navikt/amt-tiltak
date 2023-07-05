package no.nav.amt.tiltak.core.port

import java.util.UUID

interface AuditLoggerService {

	/**
	 * Log en hendelse som en NAV ansatt har utført på en deltaker
	 */
	fun navAnsattBehandletEndringsmeldingAuditLog(navAnsattId: UUID, deltakerId: UUID)
}

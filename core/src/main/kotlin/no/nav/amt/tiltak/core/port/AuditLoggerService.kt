package no.nav.amt.tiltak.core.port

import java.util.*

interface AuditLoggerService {

	/**
	 * Log en hendelse som en NAV ansatt har utført på en deltaker
	 */
	fun navAnsattAuditLog(
		navAnsattId: UUID,
		deltakerId: UUID,
		eventType: AuditEventType,
		eventSeverity: AuditEventSeverity,
		reason: String,
	)

}

enum class AuditEventType {
	CREATE,
	ACCESS,
	UPDATE,
	DELETE
}

enum class AuditEventSeverity {
	INFO,
	WARN,
}

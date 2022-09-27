package no.nav.amt.tiltak.common.auth

import no.nav.amt.tiltak.core.port.*
import no.nav.common.audit_log.cef.CefMessage
import no.nav.common.audit_log.cef.CefMessageEvent
import no.nav.common.audit_log.cef.CefMessageSeverity
import no.nav.common.audit_log.log.AuditLogger
import org.springframework.stereotype.Service
import java.util.*

@Service
class AuditLoggerServiceImpl(
	private val auditLogger: AuditLogger,
	private val navAnsattService: NavAnsattService,
	private val deltakerService: DeltakerService
) : AuditLoggerService {

	companion object {
		const val APPLICATION_NAME = "amt-tiltak"
		const val AUDIT_LOG_NAME = "Sporingslogg"
		const val MESSAGE_EXTENSION = "msg"
	}

	override fun navAnsattAuditLog(
		navAnsattId: UUID,
		deltakerId: UUID,
		eventType: AuditEventType,
		eventSeverity: AuditEventSeverity,
		reason: String
	) {
		sendAuditLog(
			sourceUserIdProvider = { navAnsattService.getNavAnsatt(navAnsattId).navIdent },
			destinationUserIdProvider = { deltakerService.hentDeltaker(deltakerId)?.bruker?.fodselsnummer?: throw NoSuchElementException("Fant ikke deltaker med id: $deltakerId") },
			eventType = eventType,
			eventSeverity = eventSeverity,
			reason = reason
		)
	}

	private fun sendAuditLog(
		sourceUserIdProvider: () -> String,
		destinationUserIdProvider: () -> String,
		reason: String,
		eventType: AuditEventType,
		eventSeverity: AuditEventSeverity
	) {
		val msg = CefMessage.builder()
			.applicationName(APPLICATION_NAME)
			.event(toCefMessageEvent(eventType))
			.name(AUDIT_LOG_NAME)
			.severity(toCefMessageSeverity(eventSeverity))
			.sourceUserId(sourceUserIdProvider.invoke())
			.destinationUserId(destinationUserIdProvider.invoke())
			.timeEnded(System.currentTimeMillis())
			.extension(MESSAGE_EXTENSION, reason)
			.build()

		auditLogger.log(msg)
	}

	private fun toCefMessageEvent(auditEventType: AuditEventType): CefMessageEvent {
		return when(auditEventType) {
			AuditEventType.CREATE -> CefMessageEvent.CREATE
			AuditEventType.ACCESS -> CefMessageEvent.ACCESS
			AuditEventType.UPDATE -> CefMessageEvent.UPDATE
			AuditEventType.DELETE -> CefMessageEvent.DELETE
		}
	}

	private fun toCefMessageSeverity(auditEventSeverity: AuditEventSeverity): CefMessageSeverity {
		return when(auditEventSeverity) {
			AuditEventSeverity.INFO -> CefMessageSeverity.INFO
			AuditEventSeverity.WARN -> CefMessageSeverity.WARN
		}
	}

}

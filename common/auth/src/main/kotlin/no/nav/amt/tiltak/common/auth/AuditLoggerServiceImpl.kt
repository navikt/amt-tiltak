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
	private val arrangorAnsattService: ArrangorAnsattService,
	private val navAnsattService: NavAnsattService,
	private val gjennomforingService: GjennomforingService,
	private val deltakerService: DeltakerService
) : AuditLoggerService {

	internal enum class AuditEventType {
		CREATE,
		ACCESS,
		UPDATE,
		DELETE
	}

	internal enum class AuditEventSeverity {
		INFO,
		WARN,
	}

	companion object {
		const val APPLICATION_NAME = "amt-tiltak"
		const val AUDIT_LOG_NAME = "Sporingslogg"
		const val MESSAGE_EXTENSION = "msg"

		const val NAV_ANSATT_ENDRINGSMELDING_AUDIT_LOG_REASON =
			"NAV-ansatt har lest melding fra tiltaksarrangoer om oppstartsdato paa tiltak for aa registrere dette."

		const val TILTAKSARRANGOR_ANSATT_DELTAKER_OPPSLAG_AUDIT_LOG_REASON =
			"Tiltaksarrangor ansatt har gjort oppslag paa deltaker."
	}

	override fun navAnsattBehandletEndringsmeldingAuditLog(navAnsattId: UUID, deltakerId: UUID) {
		sendAuditLog(
			sourceUserIdProvider = { navAnsattService.getNavAnsatt(navAnsattId).navIdent },
			destinationUserIdProvider = {
				deltakerService.hentDeltaker(deltakerId)?.fodselsnummer
					?: throw NoSuchElementException("Fant ikke deltaker med id: $deltakerId")
			},
			eventType = AuditEventType.ACCESS,
			eventSeverity = AuditEventSeverity.INFO,
			reason = NAV_ANSATT_ENDRINGSMELDING_AUDIT_LOG_REASON
		)
	}

	override fun tiltaksarrangorAnsattDeltakerOppslagAuditLog(arrangorAnsattId: UUID, deltakerId: UUID) {
		val deltaker = 	deltakerService.hentDeltaker(deltakerId)
			?: throw NoSuchElementException("Fant ikke deltaker med id: $deltakerId")

		val gjennomforing = gjennomforingService.getGjennomforing(deltaker.gjennomforingId)

		sendAuditLog(
			sourceUserIdProvider = { arrangorAnsattService.getAnsatt(arrangorAnsattId).personligIdent },
			destinationUserIdProvider = { deltaker.fodselsnummer },
			eventType = AuditEventType.ACCESS,
			eventSeverity = AuditEventSeverity.INFO,
			reason = TILTAKSARRANGOR_ANSATT_DELTAKER_OPPSLAG_AUDIT_LOG_REASON,
			extensions = mapOf("cn1" to gjennomforing.arrangor.organisasjonsnummer)
		)
	}

	private fun sendAuditLog(
		sourceUserIdProvider: () -> String,
		destinationUserIdProvider: () -> String,
		reason: String,
		eventType: AuditEventType,
		eventSeverity: AuditEventSeverity,
		extensions: Map<String, String> = emptyMap()
	) {
		val builder = CefMessage.builder()
			.applicationName(APPLICATION_NAME)
			.event(toCefMessageEvent(eventType))
			.name(AUDIT_LOG_NAME)
			.severity(toCefMessageSeverity(eventSeverity))
			.sourceUserId(sourceUserIdProvider.invoke())
			.destinationUserId(destinationUserIdProvider.invoke())
			.timeEnded(System.currentTimeMillis())
			.extension(MESSAGE_EXTENSION, reason)

		extensions.forEach {
			builder.extension(it.key, it.value)
		}

		val msg = builder.build()

		auditLogger.log(msg)
	}

	private fun toCefMessageEvent(auditEventType: AuditEventType): CefMessageEvent {
		return when (auditEventType) {
			AuditEventType.CREATE -> CefMessageEvent.CREATE
			AuditEventType.ACCESS -> CefMessageEvent.ACCESS
			AuditEventType.UPDATE -> CefMessageEvent.UPDATE
			AuditEventType.DELETE -> CefMessageEvent.DELETE
		}
	}

	private fun toCefMessageSeverity(auditEventSeverity: AuditEventSeverity): CefMessageSeverity {
		return when (auditEventSeverity) {
			AuditEventSeverity.INFO -> CefMessageSeverity.INFO
			AuditEventSeverity.WARN -> CefMessageSeverity.WARN
		}
	}
}

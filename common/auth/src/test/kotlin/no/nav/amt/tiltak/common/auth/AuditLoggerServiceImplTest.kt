package no.nav.amt.tiltak.common.auth

import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.amt.tiltak.core.domain.arrangor.Ansatt
import no.nav.amt.tiltak.core.domain.arrangor.Arrangor
import no.nav.amt.tiltak.core.domain.nav_ansatt.NavAnsatt
import no.nav.amt.tiltak.core.domain.tiltak.Deltaker
import no.nav.amt.tiltak.core.domain.tiltak.DeltakerStatus
import no.nav.amt.tiltak.core.domain.tiltak.Gjennomforing
import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.core.port.ArrangorAnsattService
import no.nav.amt.tiltak.core.port.DeltakerService
import no.nav.amt.tiltak.core.port.GjennomforingService
import no.nav.amt.tiltak.core.port.NavAnsattService
import no.nav.common.audit_log.cef.CefMessage
import no.nav.common.audit_log.log.AuditLogger
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.*

class AuditLoggerServiceImplTest {

	val navAnsattService: NavAnsattService = mockk()

	val arrangorAnsattService: ArrangorAnsattService = mockk()

	val deltakerService: DeltakerService = mockk()

	val gjennomforingService: GjennomforingService = mockk()

	val auditLogger: AuditLogger = mockk()

	lateinit var auditLoggerService: AuditLoggerServiceImpl

	@BeforeEach
	fun setup() {
		auditLoggerService = AuditLoggerServiceImpl(
			auditLogger,
			arrangorAnsattService,
			navAnsattService,
			gjennomforingService,
			deltakerService
		)
	}

	@Test
	fun `navAnsattBehandletEndringsmeldingAuditLog - skal lage logmelding med riktig data`() {
		val navAnsattId = UUID.randomUUID()
		val deltakerId = UUID.randomUUID()

		every {
			navAnsattService.getNavAnsatt(navAnsattId)
		} returns NavAnsatt(
			id = navAnsattId,
			navIdent = "Z1234",
			navn = ""
		)

		every {
			deltakerService.hentDeltaker(deltakerId)
		} returns Deltaker(
			id = deltakerId,
			gjennomforingId = UUID.randomUUID(),
			fornavn = "",
			mellomnavn = null,
			etternavn = "",
			fodselsnummer = "12345678900",
			navEnhetId = null,
			navVeilederId = null,
			epost = null,
			telefonnummer = null,
			startDato = null,
			sluttDato = null,
			status = DeltakerStatus(UUID.randomUUID(), DeltakerStatus.Type.DELTAR, null, LocalDateTime.now(), LocalDateTime.now(), true),
			registrertDato = LocalDateTime.now(),
		)

		val messageSlot = slot<CefMessage>()

		every {
			auditLogger.log(capture(messageSlot))
		} returns Unit

		auditLoggerService.navAnsattBehandletEndringsmeldingAuditLog(navAnsattId, deltakerId)

		val msg = messageSlot.captured

		msg.version shouldBe 0
		msg.deviceProduct shouldBe "AuditLogger"
		msg.deviceVendor shouldBe "amt-tiltak"
		msg.deviceVersion shouldBe "1.0"
		msg.name shouldBe "Sporingslogg"
		msg.severity shouldBe "INFO"
		msg.signatureId shouldBe "audit:access"
		msg.extension["msg"] shouldBe "NAV-ansatt har lest melding fra tiltaksarrangoer om oppstartsdato paa tiltak for aa registrere dette."
		msg.extension["suid"] shouldBe "Z1234"
		msg.extension["duid"] shouldBe "12345678900"
	}

	@Test
	fun `tiltaksarrangorAnsattDeltakerOppslagAuditLog - skal lage logmelding med riktig data`() {
		val arrangorAnsattId = UUID.randomUUID()
		val deltakerId = UUID.randomUUID()
		val gjennomforingId = UUID.randomUUID()

		every {
			arrangorAnsattService.getAnsatt(arrangorAnsattId)
		} returns Ansatt(arrangorAnsattId, "47645453534", "", null, "", emptyList())

		every {
			deltakerService.hentDeltaker(deltakerId)
		} returns Deltaker(
			id = deltakerId,
			gjennomforingId = gjennomforingId,
			fornavn = "",
			mellomnavn = null,
			etternavn = "",
			fodselsnummer = "12345678900",
			navEnhetId = null,
			navVeilederId = null,
			epost = null,
			telefonnummer = null,
			startDato = null,
			sluttDato = null,
			status = DeltakerStatus(UUID.randomUUID(), DeltakerStatus.Type.DELTAR, null, LocalDateTime.now(), LocalDateTime.now(), true),
			registrertDato = LocalDateTime.now(),
		)

		every {
			gjennomforingService.getGjennomforing(gjennomforingId)
		} returns Gjennomforing(
			id = gjennomforingId,
			tiltak = Tiltak(UUID.randomUUID(), "", ""),
			arrangor = Arrangor(UUID.randomUUID(), "", "123435643", "", ""),
			navn = "",
			status = Gjennomforing.Status.GJENNOMFORES,
			startDato = null,
			sluttDato = null,
			navEnhetId = null,
			opprettetAar = 1,
			lopenr = 1
		)

		val messageSlot = slot<CefMessage>()

		every {
			auditLogger.log(capture(messageSlot))
		} returns Unit

		auditLoggerService.tiltaksarrangorAnsattDeltakerOppslagAuditLog(arrangorAnsattId, deltakerId)

		val msg = messageSlot.captured

		msg.version shouldBe 0
		msg.deviceProduct shouldBe "AuditLogger"
		msg.deviceVendor shouldBe "amt-tiltak"
		msg.deviceVersion shouldBe "1.0"
		msg.name shouldBe "Sporingslogg"
		msg.severity shouldBe "INFO"
		msg.signatureId shouldBe "audit:access"
		msg.extension["msg"] shouldBe "Tiltaksarrangor ansatt har gjort oppslag paa deltaker."
		msg.extension["suid"] shouldBe "47645453534"
		msg.extension["duid"] shouldBe "12345678900"
		msg.extension["cn1"] shouldBe "123435643"
	}

}

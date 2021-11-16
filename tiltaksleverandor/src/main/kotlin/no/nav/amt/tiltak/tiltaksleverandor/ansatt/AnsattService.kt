package no.nav.amt.tiltak.tiltaksleverandor.ansatt

import no.nav.amt.tiltak.core.domain.tiltaksleverandor.Ansatt
import org.springframework.stereotype.Service
import java.util.*

@Service
class AnsattService {

    fun getAnsatt(ansattId: UUID): Ansatt {
        throw NotImplementedError("getAnsatt in AnsattService is not yet implemented")
    }
}

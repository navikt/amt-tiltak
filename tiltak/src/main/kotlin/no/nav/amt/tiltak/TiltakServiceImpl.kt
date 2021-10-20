package no.nav.amt.tiltak

import no.nav.amt.tiltak.core.domain.tiltak.Tiltak
import no.nav.amt.tiltak.core.domain.tiltak.TiltakInstans
import no.nav.amt.tiltak.core.port.TiltakService
import no.nav.amt.tiltak.tiltak.repositories.TiltakRepository
import no.nav.amt.tiltak.tiltak.repositories.TiltaksinstansRepository
import org.springframework.stereotype.Service

@Service
class TiltakServiceImpl(
    private val tiltakRepository: TiltakRepository,
    private val tiltaksinstansRepository: TiltaksinstansRepository
) : TiltakService {


    override fun getTiltakFromArenaId(arenaId: String): Tiltak? {
        return tiltakRepository.getByArenaId(arenaId)?.toTiltak()
    }

    override fun addUpdateTiltak(arenaId: String, navn: String, kode: String): Tiltak {
        val storedTiltak = tiltakRepository.getByArenaId(arenaId)

        if (storedTiltak != null) {
            throw NotImplementedError("Update is not yet implemented") //TODO
        }

        return tiltakRepository.insert(arenaId, navn, kode).toTiltak()
    }

    override fun addTiltaksinstans(arenaId: Int, instans: TiltakInstans): TiltakInstans {
        return tiltaksinstansRepository.insert(arenaId, instans).toTiltaksinstans(instans.tiltakId)
    }


}

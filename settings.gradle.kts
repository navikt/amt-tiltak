rootProject.name = "amt-tiltak"

// main modules
include(
    ":application",
    ":arrangor",
    ":bff-internal",
    ":bff-nav_ansatt",
    ":bff-tiltaksarrangor",
    ":clients",
    ":common",
    ":core",
    ":data-publisher",
    ":db-migrations",
    ":external-api",
    ":kafka",
    ":navansatt",
    ":test",
    ":test-integration",
    ":tilgangskontroll-tiltaksansvarlig",
    ":tilgangskontroll-tiltaksarrangor",
    ":tiltak"
)

// :clients submodules
include(
    ":clients:amt-arrangor-client",
    ":clients:mulighetsrommet-api-client",
    ":clients:amt-person"
)

// :common submodules
include(
    ":common:auth",
    ":common:json",
    ":common:db_utils",
    ":common:utils"
)

// :kafka submodules
include(
    ":kafka:arena-acl-ingestor",
    ":kafka:kafka-config",
    ":kafka:kafka-producer",
    ":kafka:gjennomforing-ingestor",
    ":kafka:arrangor-ingestor",
    ":kafka:ansatt-ingestor",
    ":kafka:nav-bruker-ingestor",
    ":kafka:nav-ansatt-ingestor",
    ":kafka:deltaker-ingestor"
)

// :test submodules
include(
    ":test:database",
    ":test:test-utils"
)

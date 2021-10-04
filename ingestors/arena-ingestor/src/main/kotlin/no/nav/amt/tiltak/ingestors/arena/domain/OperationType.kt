package no.nav.amt.tiltak.ingestors.arena.domain

enum class OperationType {
    INSERT,
    UPDATE,
    DELETE
}

fun OperationType.fromString(string: String): OperationType {
    return when (string) {
        "I" -> OperationType.INSERT
        "U" -> OperationType.UPDATE
        "D" -> OperationType.DELETE
        else -> throw IllegalArgumentException("$string is not a supported operation type")
    }
}

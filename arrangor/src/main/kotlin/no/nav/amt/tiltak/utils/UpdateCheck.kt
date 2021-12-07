package no.nav.amt.tiltak.utils

enum class UpdateStatus {
	UPDATED,
	NO_CHANGE
}

data class UpdateCheck<T>(
    val status: UpdateStatus,
    val updatedObject: T? = null
)

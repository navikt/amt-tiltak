import java.util.UUID

data class NavAnsattDto(
	val id: UUID,
	val navident: String,
	val navn: String,
	val telefon: String?,
	val epost: String?,
)

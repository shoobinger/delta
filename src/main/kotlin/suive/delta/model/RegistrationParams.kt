package suive.delta.model

data class RegistrationParams(
    val registrations: List<Registration>
)

data class Registration(
    val id: String,
    val method: String,
    val registerOptions: Any?
)

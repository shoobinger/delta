package suive.delta.model

data class Registration(
    val id: String,
    val method: String,
    val registerOptions: Any?
)

data class RegistrationParams(
    val registrations: List<Registration>
)

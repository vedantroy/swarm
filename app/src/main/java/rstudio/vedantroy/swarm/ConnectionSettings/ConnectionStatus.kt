package rstudio.vedantroy.swarm.ConnectionSettings

data class ConnectionStatus(val name: String, val status: StatusType)

enum class StatusType {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}
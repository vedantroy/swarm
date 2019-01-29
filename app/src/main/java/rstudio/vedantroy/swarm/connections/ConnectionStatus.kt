package rstudio.vedantroy.swarm.connections

data class ConnectionStatus(val name: String, var status: ConnectionType)

enum class ConnectionType {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}
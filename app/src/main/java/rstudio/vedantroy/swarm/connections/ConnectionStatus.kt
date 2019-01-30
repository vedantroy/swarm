package rstudio.vedantroy.swarm.connections

data class ConnectionStatus(val deviceId: String, var endpointId: String, var status: ConnectionType)

enum class ConnectionType {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}
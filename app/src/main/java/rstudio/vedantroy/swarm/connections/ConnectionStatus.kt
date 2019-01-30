package rstudio.vedantroy.swarm.connections

data class ConnectionStatus(val deviceID: DeviceID, var endpointID: EndpointID, var status: ConnectionType)

inline class DeviceID(val rawValue: String)
inline class EndpointID(val rawValue: String)

enum class ConnectionType {
    DISCONNECTED,
    CONNECTING,
    CONNECTED
}
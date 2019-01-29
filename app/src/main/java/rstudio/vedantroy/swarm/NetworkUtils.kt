package rstudio.vedantroy.swarm

import android.app.Application
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import rstudio.vedantroy.swarm.connections.ConnectionStatus
import rstudio.vedantroy.swarm.connections.ConnectionType
import rstudio.vedantroy.swarm.connections.StatusType
import rstudio.vedantroy.swarm.MainActivity.Companion.TAG


//Singleton class
class  NetworkUtils(val app: Application) {
    private val context = app.applicationContext
    private val deviceID  = android.os.Build.MANUFACTURER
    private val client : ConnectionsClient = Nearby
        .getConnectionsClient(context)

    private val advertisingOptions: AdvertisingOptions = AdvertisingOptions
        .Builder()
        .setStrategy(Strategy.P2P_CLUSTER)
        .build()

    private val discoveryOptions : DiscoveryOptions = DiscoveryOptions
        .Builder()
        .setStrategy(Strategy.P2P_CLUSTER)
        .build()

    val devices = mutableListOf<ConnectionStatus>()

    var onDeviceStatusUpdated : ((Int) -> Unit)? = null

    private val connectionCallback = object: ConnectionLifecycleCallback() {
        override fun onConnectionResult(endpointID: String, result: ConnectionResolution) {
            Log.d(TAG, "onConnectionResult")
            when(result.status.statusCode) {
                ConnectionsStatusCodes.STATUS_OK -> {
                    setDeviceStatus(endpointID, ConnectionType.CONNECTED)
                }
                ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                    setDeviceStatus(endpointID, ConnectionType.DISCONNECTED)
                }
                ConnectionsStatusCodes.STATUS_ERROR -> {
                    setDeviceStatus(endpointID, ConnectionType.DISCONNECTED)
                }
            }
        }

        override fun onDisconnected(endpointID: String) {
            setDeviceStatus(endpointID, ConnectionType.DISCONNECTED)
        }

        override fun onConnectionInitiated(endpointID: String, result: ConnectionInfo) {
            Log.d(TAG, "onConnectionInitiated")
            Nearby
                .getConnectionsClient(context)
                .acceptConnection(endpointID, payloadCallback)
        }
    }

    fun setDeviceStatus(deviceName: String, status: ConnectionType) {
        var deviceExists = false
        for((index, device) in devices.withIndex()) {
            if(device.name == deviceName) {
                Log.d(TAG,"setDeviceStatus|${device.name} to $status")
                deviceExists = true
                device.status = status
                onDeviceStatusUpdated?.invoke(index)
            }
        }
        if(!deviceExists) {
            Log.d(TAG, "setDeviceStatus|add $deviceName with $status")
            devices.add(ConnectionStatus(deviceName, status))
            onDeviceStatusUpdated?.invoke(devices.count() - 1)
        }
    }

    fun requestConnection(endpointID: String) {
        Log.d(TAG, "requestConnection|Requesting Connection")
        client
            .requestConnection(
                deviceID,
                endpointID,
                connectionCallback
            )
            .addOnSuccessListener {
                Log.d(TAG, "requestConnection|Success")
            }.addOnFailureListener {
                Log.d(TAG, "requestConnection|Failure")
            }.addOnCanceledListener {
                Log.d(TAG, "requestConnection|Cancel")
            }.addOnCompleteListener {
                Log.d(TAG, "requestConnection|Complete")
            }
    }

    private val discoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointID: String, p1: DiscoveredEndpointInfo) {
            Log.d(TAG, "onEndpointFound|Endpoint Found")
            //TODO: endpointName vs endpointID -- what's the diff?
            requestConnection(endpointID)
        }

        override fun onEndpointLost(endpointID: String) {
            setDeviceStatus(endpointID, ConnectionType.DISCONNECTED)
        }
    }

    //Initiate advertising/discovery
    fun startFinding() {
        with(Nearby.getConnectionsClient(context)) {
            startDiscovery(
                context.packageName,
                discoveryCallback,
                discoveryOptions
            ).addOnSuccessListener {
                Log.d(TAG, "startFinding|startDiscovery Success")
            }.addOnFailureListener {
                Log.d(TAG, "startFinding|startDiscovery Failure")
                Log.d(TAG, it.toString())
            }.addOnCanceledListener {
                Log.d(TAG, "startFinding|startDiscovery Canceled")
            }.addOnCompleteListener {
                Log.d(TAG, "startFinding|startDiscovery Complete")
            }

            startAdvertising(
                deviceID,
                applicationContext.packageName,
                connectionCallback,
                advertisingOptions
            ).addOnSuccessListener {
                Log.d(TAG, "startFinding|startAdvertising Success")
            }.addOnFailureListener {
                Log.d(TAG, "startFinding|startAdvertising Failure")
                Log.d(TAG, it.toString())
            }.addOnCanceledListener {
                Log.d(TAG, "startFinding|startAdvertising Canceled")
            }.addOnCompleteListener {
                Log.d(TAG, "startFinding|startAdvertising Complete")
            }
        }
    }
}


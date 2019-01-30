package rstudio.vedantroy.swarm

import android.app.Application
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import org.jetbrains.anko.collections.forEachWithIndex
import rstudio.vedantroy.swarm.connections.ConnectionStatus
import rstudio.vedantroy.swarm.connections.ConnectionType
import rstudio.vedantroy.swarm.MainActivity.Companion.TAG
import rstudio.vedantroy.swarm.connections.EndpointID
import rstudio.vedantroy.swarm.connections.DeviceID

enum class Change {
    MODIFICATION,
    INSERTION,
    DELETION,
}


//Singleton class
class  NetworkUtils(val app: Application, uniqueDeviceID: UniqueDeviceID) {
    private val context = app.applicationContext
    private val SERVICE_ID: String = context.packageName
    val USER_ID  = uniqueDeviceID.id
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

    var onDeviceStatusUpdated : ((Int, Change) -> Unit)? = null
    var onPayloadReceived: ((String, Payload) -> Unit)? = null
    var onPayloadTransferUpdate: ((String, PayloadTransferUpdate) -> Unit)? = null

    val payloadCallback = object : PayloadCallback() {
        override fun onPayloadReceived(endpointID: String, payload: Payload) {
            onPayloadReceived?.invoke(endpointID, payload)
        }
        override fun onPayloadTransferUpdate(endpointID: String, payloadUpdate: PayloadTransferUpdate) {
            onPayloadTransferUpdate?.invoke(endpointID, payloadUpdate)
        }
    }

    private val connectionCallback = object: ConnectionLifecycleCallback() {
        override fun onConnectionResult(_endpointID: String, result: ConnectionResolution) {
            Log.d(TAG, "onConnectionResult")
            val endpointID = EndpointID(_endpointID)
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
                else -> {
                    Log.d(TAG,"onConnectionResult|Unknown Error")
                }
            }
        }

        override fun onDisconnected(endpointID: String) {
            setDeviceStatus(EndpointID(endpointID), ConnectionType.DISCONNECTED)
        }

        override fun onConnectionInitiated(endpointID: String, info: ConnectionInfo) {
            Log.d(TAG, "onConnectionInitiated")
            Log.d(TAG, "onConnectionInitiated|${info.endpointName}")
            setDeviceStatus(EndpointID(endpointID), ConnectionType.CONNECTING)
            client.acceptConnection(endpointID, payloadCallback)
        }
    }

    fun addDevice(deviceID: DeviceID, endpointID: EndpointID, status: ConnectionType) {
        val duplicateDevices = devices.filter { it.deviceID == deviceID }
        if(duplicateDevices.count() > 0) {
            Log.d(TAG,"addDevice|Duplicates Found")
            duplicateDevices.forEachWithIndex { index, device ->
                device.endpointID = endpointID
                device.status = status
                onDeviceStatusUpdated?.invoke(index, Change.MODIFICATION)
            }
        } else {
            Log.d(TAG,"addDevice|Adding Device")
            devices.add(ConnectionStatus(deviceID, endpointID, status))
            onDeviceStatusUpdated?.invoke(devices.count() - 1, Change.INSERTION)
        }
    }

    fun removeDevice(endpointID: EndpointID) {
        val devicesForDeletion = devices.filter {
            it.endpointID == endpointID
        }
        if(devicesForDeletion.count() != 1) {
            Log.d(TAG, "removeDevice|Deleting ${devicesForDeletion.count()} -- this is not expected")
        }
        devices.removeAll(devicesForDeletion)
    }

    //optimize for ranges... lol what does this mean, I forget
    fun setDeviceStatus(endpointID: EndpointID, status: ConnectionType) {
        var deviceExists = false

        for((index, device) in devices.withIndex()) {
            if(device.endpointID == endpointID) {
                Log.d(TAG,"setDeviceStatus|${device.deviceID} to $status")
                deviceExists = true
                device.status = status
                onDeviceStatusUpdated?.invoke(index, Change.MODIFICATION)
            }
        }

        if(!deviceExists) {
            Log.d(TAG, "setDeviceStatus|Device did not exist -- this is not expected")
            /*
            Log.d(TAG, "setDeviceStatus|add $deviceName with $status")
            devices.add(ConnectionStatus(end, status))
            onDeviceStatusUpdated?.invoke(devices.count() - 1, Change.INSERTION)
            */
        }
        /*
            val index = devices.indexOf(devices.find { it.name == deviceName })
            if(index > -1) {
                devices.removeAt(index)
                onDeviceStatusUpdated?.invoke(index, Change.DELETION)
            } else {
                Log.d(TAG, "setDeviceStatus|Device scheduled for deletion not found")
            }
       */
    }

    fun requestConnection(endpointID: EndpointID) {
        Log.d(TAG, "requestConnection|Requesting Connection")
        setDeviceStatus(endpointID, ConnectionType.CONNECTING)
        client
            .requestConnection(
                USER_ID,
                endpointID.rawValue,
                connectionCallback
            )
            .addOnSuccessListener {
                Log.d(TAG, "requestConnection|Success")
            }.addOnFailureListener {
                Log.d(TAG, "requestConnection|Failure")
                Log.d(TAG, it.toString())
                setDeviceStatus(endpointID, ConnectionType.DISCONNECTED)
            }.addOnCanceledListener {
                Log.d(TAG, "requestConnection|Cancel")
            }.addOnCompleteListener {
                Log.d(TAG, "requestConnection|Complete")
            }
    }

    //endpointID is transient and fleeting--it changes often.
        //YET--endpointID is what is used for connection
    //endpointName is permanent

    //endpoint name -- permanent. identifies devices
    //endpoint ID -- many to one relationship w/ endpoint name. identifies endpoint
    //used for connections
    //status -- one to one. identifies status

    //advertising callback is only triggered when a discoverer does requestConnection
        //this is why it's a generic connection callback and not advertising specific
    //the discovery callback is triggered when a discovery is made

    //endpoint is changed twice (ever)
    //discovery-- @ this time status is set to disconnected
    //

    private val discoveryCallback = object : EndpointDiscoveryCallback() {
        override fun onEndpointFound(endpointID: String, info: DiscoveredEndpointInfo) {
            Log.d(TAG, "onEndpointFound|Endpoint Found")
            Log.d(TAG, "onEndpointFound|${info.endpointName}")
            //TODO: endpointName vs endpointID -- what's the diff?
            addDevice(DeviceID(info.endpointName), EndpointID(endpointID), ConnectionType.DISCONNECTED)
        }

        override fun onEndpointLost(endpointID: String) {
            //Ths == device is no longer found AT ALL
            removeDevice(EndpointID(endpointID))
        }
    }

    //Initiate advertising/discovery
    fun startFinding() {
        with(Nearby.getConnectionsClient(context)) {
            startDiscovery(
                SERVICE_ID,
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
                USER_ID,
                SERVICE_ID,
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

    fun stopFinding() {
        with(client) {
            stopDiscovery()
            stopAdvertising()
        }
    }

    fun sendBytes(bytes: ByteArray) {
        val payload = Payload.fromBytes(bytes)
        devices.filter {
            it.status == ConnectionType.CONNECTED
        }.map {
            client.sendPayload(it.endpointID.rawValue, payload)
        }
    }
}


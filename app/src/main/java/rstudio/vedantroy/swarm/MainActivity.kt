package rstudio.vedantroy.swarm

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.beust.klaxon.Klaxon
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.text_input_prompt.view.*

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "SWARM_APP"
    }

    @SuppressLint("HardwareIds")
    lateinit var deviceID : String  //= Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)


    private val posts = mutableListOf<Post>()

    val connectionStatuses = HashMap<String, Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        deviceID = android.os.Build.MANUFACTURER //Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)

        postFeed.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.VERTICAL,
            false
        )

        postFeed.adapter = PostAdapter(posts, this)

        createPost.setOnClickListener {
            val textPromptLayout = layoutInflater.inflate(R.layout.text_input_prompt, null)
            AlertDialog.Builder(this)
                .setTitle(getString(R.string.text_prompt_title))
                .setView(textPromptLayout)
                .setPositiveButton(R.string.submit) { dialog, _ ->
                    val post = Post(textPromptLayout.post_input.text.toString(), 0)
                    posts.add(post)
                    postFeed.adapter?.notifyDataSetChanged()

                    val bytes = Klaxon()
                        .toJsonString(post)
                        .toByteArray()

                    for((key, value) in connectionStatuses) {
                        if(value) {
                            Nearby
                                .getConnectionsClient(this)
                                .sendPayload(
                                    key,
                                    Payload.fromBytes(bytes)
                                )
                        }
                    }
                    dialog.cancel()
                }.setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }.create()
                .show()
        }
    }

    override fun onStart() {
        super.onStart()

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //TODO: figure out wtf request code is
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        }

        val payloadCallback = object : PayloadCallback() {
            override fun onPayloadReceived(endpointID: String, payload: Payload) {
                //Can assert that it works because currently only sending payloads as bytes--
                when(payload.type) {
                    Payload.Type.BYTES -> {
                        val bytes = payload.asBytes()
                        bytes?.let {
                             Klaxon().parse<Post>(String(it))
                        }?.let {
                            posts.add(it)
                            postFeed.adapter?.notifyDataSetChanged()
                        }
                    }
                    Payload.Type.FILE -> {
                        Log.d(TAG, "Payload is file!")
                    }
                    else -> {
                        Log.d(TAG, "Payload is not bytes or file!")
                    }
                }
            }

            override fun onPayloadTransferUpdate(endpointID: String, payloadUpdate: PayloadTransferUpdate) {
            }
        }

        val connectionCallback = object: ConnectionLifecycleCallback() {
            override fun onConnectionResult(endpointID: String, result: ConnectionResolution) {
                Log.d(TAG, "onConnectionResult")
                when(result.status.statusCode) {
                    ConnectionsStatusCodes.STATUS_OK -> {
                        connectionStatuses[endpointID] = true
                        Log.d(TAG, "Successful Connection!")
                    }
                    ConnectionsStatusCodes.STATUS_CONNECTION_REJECTED -> {
                        connectionStatuses[endpointID] = false
                        Log.d(TAG, "Connection Rejected")
                    }
                    ConnectionsStatusCodes.STATUS_ERROR -> {
                        connectionStatuses[endpointID] = false
                        Log.d(TAG, "Connection Error")
                    }
                }
            }

            override fun onDisconnected(endpointID: String) {
                connectionStatuses[endpointID] = false
                Log.d(TAG, "onDisconnected: $endpointID")
            }

            override fun onConnectionInitiated(endpointID: String, result: ConnectionInfo) {
                Log.d(TAG, "onConnectionInitiated")
                Nearby
                    .getConnectionsClient(applicationContext)
                    .acceptConnection(endpointID, payloadCallback)
            }

        }

        val advertisingOptions = AdvertisingOptions
            .Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .build()

        Nearby
            .get

        Nearby
            .getConnectionsClient(this)
            .startAdvertising(
                deviceID,
                applicationContext.packageName,
                connectionCallback,
                advertisingOptions
            ).addOnSuccessListener {
                Log.d(TAG, "Advertising!")
            }.addOnFailureListener {
                Log.d(TAG, "Failed to advertise!")
                Log.d(TAG, it.toString())
            }.addOnCanceledListener {
                Log.d(TAG, "Advertising cancelled!")
            }

        val discoveryCallback = object : EndpointDiscoveryCallback() {
            override fun onEndpointFound(endpointID: String, p1: DiscoveredEndpointInfo) {
                Log.d(TAG, "Endpoint found!")
                Nearby
                    .getConnectionsClient(applicationContext)
                    .requestConnection(
                        deviceID,
                        endpointID,
                        connectionCallback
                    ).addOnSuccessListener {
                        Log.d(TAG, "Successfully requested connection!")
                    }.addOnFailureListener {
                        Log.d(TAG,"Failed to request connection")
                        Log.d(TAG,  it.toString())
                    }
            }

            override fun onEndpointLost(endpointID: String) {
                connectionStatuses[endpointID] = false
            }
        }

        val discoveryOptions = DiscoveryOptions
            .Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .build()

        Nearby
            .getConnectionsClient(this)
            .startDiscovery(
                applicationContext.packageName,
                discoveryCallback,
                discoveryOptions
            ).addOnSuccessListener {
                Log.d(TAG, "Discovering!")
            }.addOnFailureListener {
                Log.d(TAG, "Failed to discover!")
                Log.d(TAG, it.toString())
            }.addOnCanceledListener {
                Log.d(TAG, "Discovery cancelled!")
            }
    }

    override fun onStop() {
        super.onStop()



        /*
        with(Nearby.getConnectionsClient(this)) {
            stopDiscovery()
            stopAdvertising()
            stopAllEndpoints()
        }

        for((key, _) in connectionStatuses) {
            connectionStatuses[key] = false
        }
        */
    }
}

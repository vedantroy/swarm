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
    val everyPostEver = mutableListOf<ImmutablePostData>()

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
                    val immutablePostData = ImmutablePostData(textPromptLayout.post_input.text.toString(), deviceID, 0)
                    while(alreadyHavePost(immutablePostData)) {
                        Log.d(TAG,"Count: " + immutablePostData.count)
                        immutablePostData.count++
                    }
                    Log.d(TAG, "Unique Post ID finished")
                    val post = Post(PostData(immutablePostData, 1), true)
                    everyPostEver.add(immutablePostData)
                    posts.add(post)
                    //TODO --make this more efficient
                    postFeed.adapter?.notifyDataSetChanged()
                    sendPostData(post.postData)
                    dialog.cancel()
                }.setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }.create()
                .show()
        }
    }

    //TODO Use-cases can def. be optimized
    fun alreadyHavePost(immutablePostData: ImmutablePostData)  = everyPostEver.find { it == immutablePostData } != null

    fun sendPostData(postData: PostData) {
        val bytes = Klaxon()
            .toJsonString(postData)
            .toByteArray()

        for((key, value) in connectionStatuses) {
            if(value) {
                Nearby
                    .getConnectionsClient(this)
                    .sendPayload(key, Payload.fromBytes(bytes))
            }
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
                             Klaxon().parse<PostData>(String(it))
                        }?.let {
                            if(alreadyHavePost(it.immutablePostData)) {
                                //The !! is fine because it should never be triggered
                                posts.find { post ->
                                    post.postData.immutablePostData == it.immutablePostData
                                }!!.postData.votes = it.votes
                            } else {
                                posts.add(Post(it, false))
                            }
                            //TODO make this more efficient
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
}

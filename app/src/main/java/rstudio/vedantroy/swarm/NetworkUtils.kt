package rstudio.vedantroy.swarm

import android.content.Context
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.AdvertisingOptions
import com.google.android.gms.nearby.connection.ConnectionsClient
import com.google.android.gms.nearby.connection.Strategy
import java.util.concurrent.atomic.AtomicBoolean

/*

class NetworkUtils {

    private var context: Context? = null
    private var instance : NetworkUtils? = null

    constructor(context: Context) {
        if (instance == null) {
            instance = NetworkUtils()
        }
        instance.setContext(context)
        return instance
    }

    private fun setContext(context: Context) {
        this.context = context
    }
}
*/


/*
class NetworkUtils private constructor(val context: Context) {



    fun advertise() {

        val advertisingOptions = AdvertisingOptions
            .Builder()
            .setStrategy(Strategy.P2P_CLUSTER)
            .build()

        Nearby
            .getConnectionsClient(context)
            .startAdvertising(
                deviceID,
                context.packageName,
                connectionCallback,
                advertisingOptions
            ).addOnSuccessListener {
                Log.d(MainActivity.TAG, "Advertising!")
            }.addOnFailureListener {
                Log.d(MainActivity.TAG, "Failed to advertise!")
                Log.d(MainActivity.TAG, it.toString())
            }.addOnCanceledListener {
                Log.d(MainActivity.TAG, "Advertising cancelled!")
            }
    }


    companion object {
        private lateinit var INSTANCE: NetworkUtils
        private val initialized = AtomicBoolean()

        val instance : NetworkUtils get() = INSTANCE

        fun initialize(context: Context) {
            if(!initialized.getAndSet(true)) {
                INSTANCE = NetworkUtils(context.applicationContext)
            }
        }
    }
}
*/

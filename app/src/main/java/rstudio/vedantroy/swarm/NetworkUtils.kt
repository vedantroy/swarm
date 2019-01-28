package rstudio.vedantroy.swarm

import android.content.Context
import com.google.android.gms.nearby.connection.ConnectionsClient
import java.util.concurrent.atomic.AtomicBoolean

class NetworkUtils private constructor(context: Context) {





    companion object {
        private lateinit var INSTANCE: NetworkUtils
        private val initialized = AtomicBoolean()

        val instance : NetworkUtils get() = INSTANCE

        fun initialize(context: Context) {
            if(!initialized.getAndSet(true)) {
                INSTANCE = NetworkUtils(context)
            }
        }
    }
}
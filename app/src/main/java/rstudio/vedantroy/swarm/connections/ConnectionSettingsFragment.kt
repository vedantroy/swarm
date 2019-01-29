package rstudio.vedantroy.swarm.connections

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.connection_settings.*
import org.koin.android.ext.android.inject
import rstudio.vedantroy.swarm.MainActivity.Companion.TAG
import rstudio.vedantroy.swarm.NetworkUtils
import rstudio.vedantroy.swarm.R

class ConnectionSettingsFragment : Fragment() {

    val networkUtils : NetworkUtils by inject()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.connection_settings, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "onActivityCreated")
        connectionStatuses.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        connectionStatuses.adapter = ConnectionSettingsAdapter(networkUtils.devices, context)

        networkUtils.onDeviceStatusUpdated = fun(index, changeType) {
            Log.d(TAG, "onDeviceStatusUpdated|$index")
            connectionStatuses?.adapter?.notifyDataSetChanged()
        }
        networkUtils.startFinding()
    }
}
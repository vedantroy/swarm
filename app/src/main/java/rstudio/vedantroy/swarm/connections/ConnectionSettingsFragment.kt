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
import kotlinx.android.synthetic.main.connection_settings.view.*
import org.koin.android.ext.android.inject
import rstudio.vedantroy.swarm.MainActivity.Companion.TAG
import rstudio.vedantroy.swarm.NetworkUtils
import rstudio.vedantroy.swarm.R

class ConnectionSettingsFragment : Fragment() {

    private val networkUtils : NetworkUtils by inject()

    private var isFinding = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.connection_settings, container, false)
        view.scanButton.setOnClickListener {
            isFinding = if(isFinding) {
                networkUtils.stopFinding()
                view.scanButton.text = getString(R.string.find_phones)
                false
            } else {
                networkUtils.startFinding()
                view.scanButton.text = getString(R.string.stop_finding_phones)
                true
            }
        }
        view.connectAllButton.setOnClickListener {
            Log.d(TAG, "Connect all clicked!")
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, "onActivityCreated")
        connectionStatuses.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        connectionStatuses.adapter = ConnectionSettingsAdapter(networkUtils, context)
    }
}
package rstudio.vedantroy.swarm.connections

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.connection_settings.*
import rstudio.vedantroy.swarm.R

class ConnectionSettingsFragment : Fragment() {

    //---
    private val devices = listOf<ConnectionStatus>(ConnectionStatus("Samsung", StatusType.DISCONNECTED), ConnectionStatus("Barillo", StatusType.CONNECTED), ConnectionStatus("Motorola", StatusType.CONNECTING), ConnectionStatus("Kumba", StatusType.DISCONNECTED))

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        return inflater.inflate(R.layout.connection_settings, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        connectionStatuses.layoutManager = LinearLayoutManager(activity, RecyclerView.VERTICAL, false)
        connectionStatuses.adapter = ConnectionSettingsAdapter(devices, context)
    }
}
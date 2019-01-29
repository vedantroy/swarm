package rstudio.vedantroy.swarm.ConnectionSettings

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.device_status.view.*
import kotlinx.android.synthetic.main.post.view.*
import rstudio.vedantroy.swarm.MainActivity
import rstudio.vedantroy.swarm.Post
import rstudio.vedantroy.swarm.R


class ConnectionSettingsAdapter(private val items : List<ConnectionStatus>, private val context: Context?) : RecyclerView.Adapter<ConnectionSettingsAdapter.ConnectionStatusViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConnectionStatusViewHolder {
        return ConnectionStatusViewHolder(LayoutInflater.from(context).inflate(R.layout.device_status, parent, false))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: ConnectionStatusViewHolder, position: Int) {
        val drawableID = when(items[position].status) {
            StatusType.DISCONNECTED -> R.drawable.ic_no_connection
            StatusType.CONNECTING ->  R.drawable.ic_connecting
            StatusType.CONNECTED -> R.drawable.ic_connected
        }
        holder.statusIcon.setBackgroundResource(drawableID)
        holder.deviceName.text = items[position].name
    }

    inner class ConnectionStatusViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val statusIcon: ImageView = view.status_icon
        val deviceName: TextView = view.device_name

    }
}
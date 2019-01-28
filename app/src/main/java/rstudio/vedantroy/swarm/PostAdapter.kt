package rstudio.vedantroy.swarm

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import kotlinx.android.synthetic.main.post.view.*

class PostAdapter(private val items : List<Post>, private val contex: Context) : RecyclerView.Adapter<PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        return PostViewHolder(LayoutInflater.from(contex).inflate(R.layout.post, parent, false))
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        holder.bodyContent.text = items[position].content
    }
}


class PostViewHolder(view: View): RecyclerView.ViewHolder(view) {
    val bodyContent : TextView = view.bodyContent

}
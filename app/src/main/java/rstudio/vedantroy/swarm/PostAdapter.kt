package rstudio.vedantroy.swarm

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.post.view.*

import rstudio.vedantroy.swarm.MainActivity.Companion.TAG

class PostAdapter(private val items : List<Post>, private val context: MainActivity) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        return PostViewHolder(LayoutInflater.from(context).inflate(R.layout.post, parent, false))
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        items[position].apply {
            holder.itemView.bodyContent.text = postData.immutablePostData.content
            holder.itemView.vote_display.text = postData.votes.toString()
            holder.itemView.user_display.text = postData.immutablePostData.creator
            if(isLiked) {
                holder.itemView.isClickable = false
                holder.itemView.vote_display.setTextColor(ContextCompat.getColor(context, R.color.upvoted_orange))
            }
        }
    }

    inner class PostViewHolder(view: View): RecyclerView.ViewHolder(view) {
        init {
            view.apply {
                setOnClickListener {
                    items[adapterPosition].isLiked = true
                    items[adapterPosition].postData.votes++
                    (context as MainActivity).sendPostData(items[adapterPosition].postData)
                    notifyItemChanged(adapterPosition)
                }
            }
        }

    }
}
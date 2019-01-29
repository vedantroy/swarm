package rstudio.vedantroy.swarm.posts

import android.app.AlertDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.beust.klaxon.Klaxon
import com.google.android.gms.nearby.connection.Payload
import kotlinx.android.synthetic.main.posts_feed.*
import kotlinx.android.synthetic.main.posts_feed.view.*
import kotlinx.android.synthetic.main.text_input_prompt.view.*
import org.koin.android.ext.android.inject
import rstudio.vedantroy.swarm.MainActivity.Companion.TAG
import rstudio.vedantroy.swarm.NetworkUtils
import rstudio.vedantroy.swarm.R

class PostsFragment : Fragment() {


    private val posts = mutableListOf<Post>()
    val everyPostEver = mutableListOf<ImmutablePostData>()


    val networkUtils : NetworkUtils by inject()

    //TODO Use-cases can def. be optimized
    fun alreadyHavePost(immutablePostData: ImmutablePostData)  = everyPostEver.find { it == immutablePostData } != null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view = inflater.inflate(R.layout.posts_feed, container, false)
        view.postFeed.layoutManager = LinearLayoutManager(
            this.context,
            RecyclerView.VERTICAL,
            false
        )
        view.postFeed.adapter = PostAdapter(posts, context, networkUtils)
        view.createPost.setOnClickListener {
            Log.d(TAG, "Showing prompt!")
            val textPromptLayout = layoutInflater.inflate(R.layout.text_input_prompt, null)
            AlertDialog.Builder(context)
                .setTitle(getString(R.string.text_prompt_title))
                .setView(textPromptLayout)
                .setPositiveButton(R.string.submit) { dialog, _ ->
                    val immutablePostData = ImmutablePostData(textPromptLayout.post_input.text.toString(), android.os.Build.MANUFACTURER, 0)
                    while(alreadyHavePost(immutablePostData)) {
                        Log.d(TAG,"Count: " + immutablePostData.count)
                        immutablePostData.count++
                    }
                    Log.d(TAG, "Unique Post ID finished")
                    val post = Post(PostData(immutablePostData, 1), true)
                    everyPostEver.add(immutablePostData)
                    posts.add(post)
                    //TODO --make this more efficient
                    view.postFeed.adapter?.notifyItemInserted(posts.count() - 1)
                    networkUtils.sendBytes(post.postData.toJsonBytes())
                    dialog.cancel()
                }.setNegativeButton(R.string.cancel) { dialog, _ ->
                    dialog.cancel()
                }.create()
                .show()
        }
        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //TODO This is some UGLY code
        networkUtils.onPayloadReceived = fun(endpointID: String, payload: Payload) {
            when(payload.type) {
                Payload.Type.BYTES -> {
                    val bytes = payload.asBytes()
                    bytes?.let {
                        Klaxon().parse<PostData>(String(it))
                    }?.let {
                        if(alreadyHavePost(it.immutablePostData)) {
                            val post = posts.find { post -> post.postData.immutablePostData == it.immutablePostData }
                            if(post != null) {
                                post.postData.votes = it.votes
                                postFeed.adapter?.notifyDataSetChanged()
                            } else {
                                Log.d(TAG,"Post has been deleted!")
                            }
                        } else {
                            posts.add(Post(it, false))
                            postFeed.adapter?.notifyDataSetChanged()
                        }
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
    }
}
package rstudio.vedantroy.swarm

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import androidx.recyclerview.widget.LinearLayoutManager
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.beust.klaxon.Klaxon
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.messages.Message
import com.google.android.gms.nearby.messages.MessageListener
import com.google.android.gms.nearby.messages.PublishCallback
import com.google.android.gms.nearby.messages.PublishOptions
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "SWARM_APP"
    }

    lateinit var mMessageListener: MessageListener
    lateinit var mMessage: Message
    lateinit var deviceID : String

    val posts = mutableListOf<Post>()
    val publishedMessages = mutableListOf<Message>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)



        //deviceID = Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)




        //Floating Action Button listener


        createPost.setOnClickListener {
            //Prompt user to enter text (deal w/ that later)
            val post = Post("Transmitted Post", Random(2).nextInt())

            posts.add(post)
            val bytes = Klaxon()
                .toJsonString(post)
                .toByteArray()
            val message = Message(bytes)
            publishedMessages.add(message)
            Log.d(TAG, "Publishing message...")


            Nearby.getMessagesClient(this).publish(message)
        }


        postFeed.layoutManager = LinearLayoutManager(
            this,
            RecyclerView.VERTICAL,
            false
        )
        postFeed.adapter = PostAdapter(listOf(Post("Hello", 1), Post("World", 2)), this)


        mMessageListener = object : MessageListener() {
            override fun onFound(message: Message) {
                super.onFound(message)
                Log.d(TAG, "Found message: " +  String(message.content))
                Toast.makeText(applicationContext, "Found message: " + String(message.content), Toast.LENGTH_LONG).show()
            }

            override fun onLost(message: Message) {
                super.onLost(message)
                Log.d(TAG, "Lost sight of message: " + String(message.content))
            }

        }

        mMessage =  Message(("Origin: " + android.os.Build.MODEL).toByteArray())
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "Pubbing and subbing started ...")
        Nearby.getMessagesClient(this).publish(mMessage)
        Nearby.getMessagesClient(this).subscribe(mMessageListener)
    }

    override fun onStop() {
        Log.d(TAG, "Pubbing and subbing stopped ...")
        publishedMessages.map {
            Nearby.getMessagesClient(this).unpublish(it)
        }

        Nearby.getMessagesClient(this).unpublish(mMessage)
        Nearby.getMessagesClient(this).unsubscribe(mMessageListener)
        super.onStop()
    }
}

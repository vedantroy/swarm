package rstudio.vedantroy.swarm

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.messages.Message
import com.google.android.gms.nearby.messages.MessageListener

class MainActivity : AppCompatActivity() {

    companion object {
        lateinit var mMessageListener: MessageListener
        lateinit var mMessage: Message
        const val TAG = "SWARM_APP"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mMessageListener = object : MessageListener() {
            override fun onFound(message: Message) {
                super.onFound(message)
                Log.d(TAG, "Found message: " +  String(message.content))
            }

            override fun onLost(message: Message) {
                super.onLost(message)
                Log.d(TAG, "Lost sight of message: " + String(message.content))
            }

        }

        mMessage =  Message("Hello World".toByteArray())
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "Pubbing and subbing started ...")
        Nearby.getMessagesClient(this).publish(mMessage)
        Nearby.getMessagesClient(this).subscribe(mMessageListener)
    }

    override fun onStop() {
        Log.d(TAG, "Pubbing and subbing stopped ...")
        Nearby.getMessagesClient(this).unpublish(mMessage)
        Nearby.getMessagesClient(this).unsubscribe(mMessageListener)
        super.onStop()
    }
}

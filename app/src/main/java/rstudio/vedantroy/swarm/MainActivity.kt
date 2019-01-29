package rstudio.vedantroy.swarm

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.beust.klaxon.Klaxon
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*
import kotlinx.android.synthetic.main.activity_main.*
import rstudio.vedantroy.swarm.posts.ImmutablePostData
import rstudio.vedantroy.swarm.posts.Post
import rstudio.vedantroy.swarm.posts.PostData


class MainActivity : AppCompatActivity() {

    companion object {
        const val TAG = "SWARM_APP"
    }

    @SuppressLint("HardwareIds")
    lateinit var deviceID : String  //= Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID)


    val connectionStatuses = HashMap<String, Boolean>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val pagerAdapter = BaseFragmentPagerAdapter(this, supportFragmentManager)
        view_pager.adapter = pagerAdapter
        tab_layout.setupWithViewPager(view_pager)
    }

    override fun onStart() {
        super.onStart()
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //TODO: figure out wtf request code is
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 1)
        }
    }
}

package rstudio.vedantroy.swarm

import android.annotation.SuppressLint
import android.app.Application
import android.provider.Settings

class UniqueDeviceID(application: Application) {
    @SuppressLint("HardwareIds")
    val id = Settings.Secure.getString(application.applicationContext.contentResolver, Settings.Secure.ANDROID_ID)
}
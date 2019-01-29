package rstudio.vedantroy.swarm

import android.app.Application
import org.koin.android.ext.android.startKoin
import org.koin.android.ext.koin.androidApplication
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module.module

class SwarmApplication : Application() {

    //Koin only seems to be good for creating
    //a singleton that can be accessed anywhere
    //not sure why I don't just manually use
    //applicationContext
    val appModule = module {
        single { NetworkUtils(androidApplication()) }
    }

    override fun onCreate() {
        super.onCreate()
        startKoin(this, listOf(appModule))
    }
}

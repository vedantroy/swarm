package rstudio.vedantroy.swarm

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import rstudio.vedantroy.swarm.ConnectionSettings.ConnectionSettingsFragment

class BaseFragmentPagerAdapter(val context: Context, fm : FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment? {
        return when(position) {
            0 ->  PostsFragment()
            1 -> ConnectionSettingsFragment()
            else -> null
        }
    }

    override fun getCount() = 2

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position) {
            0 -> "Posts"
            1 -> "Settings"
            else -> null
        }
    }
}
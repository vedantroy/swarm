package rstudio.vedantroy.swarm

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import rstudio.vedantroy.swarm.connections.ConnectionSettingsFragment
import rstudio.vedantroy.swarm.posts.PostsFragment

class BaseFragmentPagerAdapter(private val context: Context, fm : FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getItem(position: Int): Fragment? {
        return when(position) {
            0 -> PostsFragment()
            1 -> ConnectionSettingsFragment()
            else -> null
        }
    }

    override fun getCount() = 2

    override fun getPageTitle(position: Int): CharSequence? {
        return when(position) {
            0 -> context.getString(R.string.posts_tab)
            1 -> context.getString(R.string.settings_tab)
            else -> null
        }
    }
}
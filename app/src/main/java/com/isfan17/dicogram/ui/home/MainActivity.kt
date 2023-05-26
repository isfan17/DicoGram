package com.isfan17.dicogram.ui.home

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import com.isfan17.dicogram.R
import com.isfan17.dicogram.data.model.Story
import com.isfan17.dicogram.databinding.ActivityMainBinding
import com.isfan17.dicogram.ui.auth.AuthActivity
import com.isfan17.dicogram.ui.story.StoryActivity
import com.isfan17.dicogram.utils.Constants.Companion.EXTRA_STORY

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val fragment1: Fragment = HomeFragment()
    private val fragment2: Fragment = ExploreFragment()
    private val fragment3: Fragment = ProfileFragment()
    private val fm: FragmentManager = supportFragmentManager
    private var active: Fragment = fragment1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        binding.bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_home -> {
                    fm.beginTransaction().hide(active).show(fragment1).setReorderingAllowed(true).commit()
                    active = fragment1
                    true
                }
                R.id.navigation_explore -> {
                    fm.beginTransaction().hide(active).show(fragment2).commit()
                    active = fragment2
                    true
                }
                R.id.navigation_profile -> {
                    fm.beginTransaction().hide(active).show(fragment3).commit()
                    active = fragment3
                    true
                }
                else -> false
            }
        }

        fm.beginTransaction().add(R.id.nav_host_fragment_activity_main, fragment3, "3").hide(fragment3).commit()
        fm.beginTransaction().add(R.id.nav_host_fragment_activity_main, fragment2, "2").hide(fragment2).commit()
        fm.beginTransaction().add(R.id.nav_host_fragment_activity_main, fragment1, "1").commit()
    }

    fun moveToAuth() {
        val intent = Intent(this@MainActivity, AuthActivity::class.java)
        finish()
        startActivity(intent)
    }

    fun moveToDetail(story: Story) {
        val intent = Intent(this@MainActivity, StoryDetailActivity::class.java)
        intent.putExtra(EXTRA_STORY, story)
        startActivity(intent)
    }

    fun moveToStory() {
        val intent = Intent(this@MainActivity, StoryActivity::class.java)
        startActivity(intent)
    }
}
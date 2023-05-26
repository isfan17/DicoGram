package com.isfan17.dicogram.ui.home

import android.graphics.drawable.Drawable
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.isfan17.dicogram.data.model.Story
import com.isfan17.dicogram.databinding.ActivityStoryDetailBinding
import com.isfan17.dicogram.utils.Constants.Companion.EXTRA_STORY
import com.isfan17.dicogram.utils.Helper

class StoryDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStoryDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val story = if (Build.VERSION.SDK_INT >= 33) {
            intent.getParcelableExtra(EXTRA_STORY, Story::class.java)
        } else {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra(EXTRA_STORY)
        }
        if (story != null) {
            bind(story)
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> finish()
        }
        return super.onOptionsItemSelected(item)
    }

    private fun bind(story: Story) {
        binding.apply {
            tvDetailName.text = story.name
            tvCreatedAt.text = Helper.getDetailDateFormat(story.createdAt)
            tvDetailDescription.text = story.description
            ivDetailPhoto.transitionName = story.photoUrl
        }
        Glide.with(this)
            .load(story.photoUrl)
            .listener(object : RequestListener<Drawable> {
                override fun onResourceReady(
                    resource: Drawable?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                    dataSource: DataSource?,
                    isFirstResource: Boolean
                ): Boolean {
                    // The image has been loaded successfully, hide the progress bar
                    binding.progressBar.visibility = View.GONE
                    return false
                }

                override fun onLoadFailed(
                    e: GlideException?,
                    model: Any?,
                    target: com.bumptech.glide.request.target.Target<Drawable>?,
                    isFirstResource: Boolean
                ): Boolean {
                    // The image failed to load, hide the progress bar
                    binding.progressBar.visibility = View.GONE
                    return false
                }
            })
            .into(binding.ivDetailPhoto)
    }
}
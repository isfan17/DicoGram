package com.isfan17.dicogram.ui.splashscreen

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.viewModels
import com.isfan17.dicogram.databinding.ActivitySplashScreenBinding
import com.isfan17.dicogram.ui.auth.AuthActivity
import com.isfan17.dicogram.ui.home.MainActivity
import com.isfan17.dicogram.ui.viewmodels.AuthViewModel
import com.isfan17.dicogram.ui.viewmodels.ViewModelFactory
import com.isfan17.dicogram.utils.Constants

@SuppressLint("CustomSplashScreen")
class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.hide()

        @Suppress("DEPRECATION")
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        val factory: ViewModelFactory = ViewModelFactory.getInstance(this)
        val viewModel: AuthViewModel by viewModels { factory }

        viewModel.getUserPreferences(Constants.USER_PREF_TOKEN_NAME).observe(this) { token ->
            if (token != Constants.USER_PREF_DEFAULT_VALUE)
            {
                startActivity(Intent(this@SplashScreenActivity, MainActivity::class.java))
                finish()
            }
            else
            {
                startActivity(Intent(this@SplashScreenActivity, AuthActivity::class.java))
                finish()
            }
        }
    }
}
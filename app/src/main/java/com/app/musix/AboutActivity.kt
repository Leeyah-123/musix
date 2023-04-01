package com.app.musix

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.app.musix.MainActivity.Companion.currentNavTheme
import com.app.musix.MainActivity.Companion.themeIndex
import com.app.musix.databinding.ActivityAboutBinding


class AboutActivity : AppCompatActivity() {
    lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(currentNavTheme[themeIndex])

        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "About"
        binding.aboutText.text = aboutText()
    }

    private fun aboutText(): String {
        return getString(R.string.about_text)
    }

    // enable back button to work
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

}
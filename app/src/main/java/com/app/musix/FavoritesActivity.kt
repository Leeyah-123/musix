package com.app.musix

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.musix.adapters.FavoriteAdapter
import com.app.musix.databinding.ActivityFavoritesBinding
import com.app.musix.pojo.Music
import com.app.musix.pojo.checkPlaylist

class FavoritesActivity : AppCompatActivity() {
    private lateinit var binding: ActivityFavoritesBinding
    private lateinit var favoriteAdapter: FavoriteAdapter

    companion object {
        var favoriteSongs: ArrayList<Music> = ArrayList()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentNavTheme[MainActivity.themeIndex])
        binding = ActivityFavoritesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Favorites"

        favoriteSongs = checkPlaylist(favoriteSongs)
        binding.favoriteRV.setHasFixedSize(true)
        binding.favoriteRV.setItemViewCacheSize(13)
        binding.favoriteRV.layoutManager = LinearLayoutManager(this@FavoritesActivity)
        favoriteAdapter = FavoriteAdapter(this@FavoritesActivity, favoriteSongs)
        binding.favoriteRV.adapter = favoriteAdapter
        if (favoriteSongs.size < 1) binding.shuffleBtnFA.visibility = View.INVISIBLE

        binding.shuffleBtnFA.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "FavoriteShuffle")
            ContextCompat.startActivity(this, intent, null)
        }
    }

    // back button functionality
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
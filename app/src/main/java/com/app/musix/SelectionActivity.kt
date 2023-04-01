package com.app.musix

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.musix.adapters.MusicAdapter
import com.app.musix.databinding.ActivitySelectionBinding
import com.google.gson.GsonBuilder

class SelectionActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySelectionBinding
    private lateinit var adapter: MusicAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySelectionBinding.inflate(layoutInflater)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])
        setContentView(binding.root)

        binding.selectionRV.setHasFixedSize(true)
        binding.selectionRV.setItemViewCacheSize(10)
        binding.selectionRV.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(this, MainActivity.MusicListMA, playlistDetails = false, selectionActivity = true)
        binding.selectionRV.adapter = adapter

        binding.backBtnSA.setOnClickListener { finish() }

        // for search view
        binding.searchViewSA.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    MainActivity.musicListSearch = ArrayList()
                    val userInput = newText.lowercase()
                    for (song in MainActivity.MusicListMA) {
                        if (song.title.lowercase()
                                .contains(userInput)
                        ) MainActivity.musicListSearch.add(song)
                    }
                    MainActivity.search = true
                    adapter.updateMusicList(MainActivity.musicListSearch)
                } else adapter.updateMusicList(MainActivity.MusicListMA)

                MainActivity.search = false
                return true
            }

        })
    }

    override fun onDestroy() {
        super.onDestroy()

        // For persisting favorites data using shared preferences
        val playlistsEditor = getSharedPreferences("PLAYLISTS", MODE_PRIVATE).edit()
        val playlistsJsonString = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        playlistsEditor.putString("Playlists", playlistsJsonString)
        playlistsEditor.apply()
    }
}
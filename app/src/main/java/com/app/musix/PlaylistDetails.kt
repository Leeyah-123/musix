package com.app.musix

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.musix.adapters.MusicAdapter
import com.app.musix.databinding.PlaylistDetailsBinding
import com.app.musix.pojo.checkPlaylist
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder

class PlaylistDetails : AppCompatActivity() {
    lateinit var binding: PlaylistDetailsBinding
    private lateinit var adapter: MusicAdapter

    companion object {
        var currentPlaylistPos: Int = -1
    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentNavTheme[MainActivity.themeIndex])

        binding = PlaylistDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        currentPlaylistPos = intent.extras?.getInt("index") as Int

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.song_name)

        PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist = checkPlaylist(PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist)
        binding.playlistDetailsRV.setHasFixedSize(true)
        binding.playlistDetailsRV.setItemViewCacheSize(10)
        binding.playlistDetailsRV.layoutManager = LinearLayoutManager(this)
        adapter = MusicAdapter(this, PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist, true)
        binding.playlistDetailsRV.adapter = adapter

        binding.addSongPD.setOnClickListener {
            startActivity(Intent(this, SelectionActivity::class.java))
        }

        binding.removeAllPD.setOnClickListener {
            // showing a confirmation dialog
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Clear Playlist")
                .setMessage("Are you sure you want to remove all songs from playlist ${PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].name}?\nNote: This action is irreversible")
                .setPositiveButton("Yes") {dialog, _ ->
                    PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].playlist = ArrayList()

                    adapter.refreshPlaylist()
                    binding.totalSongsPD.text = "Total Songs: ${adapter.itemCount}"
                    dialog.dismiss()
                }
                .setNegativeButton("No") {dialog, _ -> dialog.dismiss()}

            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE)
            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE)
        }

        binding.shuffleBtnPD.setOnClickListener {
            val intent = Intent(this, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "PlaylistDetailsShuffle")
            ContextCompat.startActivity(this, intent, null)
        }
    }

    @SuppressLint("SetTextI18n", "NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()

        supportActionBar?.title = PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].name
        binding.totalSongsPD.text = "Total Songs: ${adapter.itemCount}"
        binding.createdAtPD.text = "Created On: ${PlaylistActivity.musicPlaylist.ref[currentPlaylistPos].createdOn}"

        if (adapter.itemCount > 0) {
            binding.shuffleBtnPD.visibility = View.VISIBLE
        }

        adapter.notifyDataSetChanged()
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

    override fun onDestroy() {
        super.onDestroy()

        // For persisting favorites data using shared preferences
        val playlistsEditor = getSharedPreferences("PLAYLISTS", MODE_PRIVATE).edit()
        val playlistsJsonString = GsonBuilder().create().toJson(PlaylistActivity.musicPlaylist)
        playlistsEditor.putString("Playlists", playlistsJsonString)
        playlistsEditor.apply()
    }
}
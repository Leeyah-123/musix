package com.app.musix

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import com.app.musix.adapters.PlaylistAdapter
import com.app.musix.databinding.ActivityPlaylistBinding
import com.app.musix.databinding.AddPlaylistDialogBinding
import com.app.musix.pojo.MusicPlaylist
import com.app.musix.pojo.Playlist
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import java.text.SimpleDateFormat
import java.util.*

class PlaylistActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlaylistBinding
    private lateinit var playlistAdapter: PlaylistAdapter

    companion object {
        var musicPlaylist: MusicPlaylist = MusicPlaylist()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentNavTheme[MainActivity.themeIndex])

        binding = ActivityPlaylistBinding.inflate(layoutInflater)
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Playlists"

        binding.playlistRV.setHasFixedSize(true)
        binding.playlistRV.setItemViewCacheSize(13)
        binding.playlistRV.layoutManager = GridLayoutManager(this@PlaylistActivity, 3)
        playlistAdapter = PlaylistAdapter(this@PlaylistActivity, musicPlaylist.ref)
        binding.playlistRV.adapter = playlistAdapter

        binding.addPlaylistBtnPA.setOnClickListener { showCustomDialog() }
    }

    private fun showCustomDialog() {
        val customDialog = LayoutInflater.from(this@PlaylistActivity)
            .inflate(R.layout.add_playlist_dialog, binding.root, false)
        val binding = AddPlaylistDialogBinding.bind(customDialog)

        val builder = MaterialAlertDialogBuilder(this)
        val dialog = builder
            .setView(customDialog)
            .setTitle("New Playlist")
            .setPositiveButton("CREATE") {_, _ ->
               val playlistName = binding.playlistName.text

                if (playlistName != null && playlistName.length >= 3) {
                    createPlaylist(playlistName.toString())
                    playlistAdapter.refreshPlaylist()
                } else Toast.makeText(this, "Playlist name must be at least three characters", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("CANCEL") {dialog, _ -> dialog.dismiss()}
            .show()
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE)
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE)
    }

    private fun createPlaylist(playlistName: String) {
        var playlistExists = false

        for (playlist in musicPlaylist.ref) {
            if (playlist.name == playlistName) playlistExists = true
            break
        }

        if (playlistExists) {
            Toast.makeText(this, "Playlist already exists", Toast.LENGTH_SHORT).show()
            return
        }

        val newPlaylist = Playlist()
        newPlaylist.name = playlistName
        val calendar = Calendar.getInstance().time
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
        newPlaylist.createdOn = sdf.format(calendar)

        musicPlaylist.ref.add(newPlaylist)
        playlistAdapter.refreshPlaylist()
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

    @SuppressLint("NotifyDataSetChanged")
    override fun onResume() {
        super.onResume()
        playlistAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        persistData()
    }

    private fun persistData() {
        // For persisting favorites data using shared preferences
        val playlistsEditor = getSharedPreferences("PLAYLISTS", MODE_PRIVATE).edit()
        val playlistsJsonString = GsonBuilder().create().toJson(musicPlaylist)
        playlistsEditor.putString("Playlists", playlistsJsonString)
        playlistsEditor.apply()
    }
}
package com.app.musix.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.musix.*
import com.app.musix.databinding.MusicViewBinding
import com.app.musix.pojo.Music
import com.app.musix.pojo.formatDuration
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class MusicAdapter(private val context: Context,
                   private var musicList: ArrayList<Music>,
                   private val playlistDetails: Boolean = false,
                   private val selectionActivity: Boolean = false) : RecyclerView.Adapter<MusicAdapter.MyHolder>() {

    class MyHolder(binding: MusicViewBinding): RecyclerView.ViewHolder(binding.root) {
        val title = binding.songNameMV
        val album = binding.songAlbumMV
        val image = binding.imageMV
        val duration = binding.songDuration
        var root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val holder = MyHolder(MusicViewBinding.inflate(LayoutInflater.from(context), parent, false))
        holder.root.setOnClickListener {
            Toast.makeText(context, "WEE", Toast.LENGTH_SHORT).show()
            holder.root.visibility = View.INVISIBLE
        }
        holder.itemView.setOnClickListener {
            Toast.makeText(context, "OMO", Toast.LENGTH_SHORT).show()
            holder.root.visibility = View.INVISIBLE
        }

        return holder
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.title.text = musicList[position].title
        holder.album.text = musicList[position].album
        holder.duration.text = formatDuration(musicList[position].duration)

//        Load song icons, if any
        Glide.with(context)
            .load(musicList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player_icon).centerCrop()) // default icon if no icon is present
            .into(holder.image)

        when {
            playlistDetails -> {
                holder.root.setOnClickListener {
                    sendIntent("PlaylistDetailsAdapter", position)
                }
            } selectionActivity -> {
                holder.root.setOnClickListener {
                   addSong(musicList[position])
                }
            } else -> {
                holder.root.setOnClickListener {
                    when {
                        MainActivity.search -> sendIntent("MusicAdapterSearch", position)
                        musicList[position].id == PlayerActivity.nowPlayingId -> sendIntent("NowPlaying",
                            PlayerActivity.songPosition
                        )
                        else -> sendIntent("MusicAdapter", position)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateMusicList(searchList: ArrayList<Music>) {
        musicList = ArrayList()
        musicList.addAll(searchList)
        notifyDataSetChanged()
    }

    private fun sendIntent(ref: String, position: Int) {
        val intent = Intent(context, PlayerActivity::class.java)
        intent.putExtra("index", position)
        intent.putExtra("class", ref)
        ContextCompat.startActivity(context, intent, null)
    }

    private fun addSong(song: Music): Boolean {
        PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist.forEachIndexed { index, music ->
            if (song.id == music.id) {
                val builder = MaterialAlertDialogBuilder(context)
                builder.setTitle("Playlist")
                    .setMessage("Song already exists in playlist. Do you want to remove it from the playlist?")
                    .setPositiveButton("Yes") {_, _ ->
                        PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist.removeAt(index)
                        Toast.makeText(context, "Song removed from playlist", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("No") {dialog, _ -> dialog.dismiss()}

                val customDialog = builder.create()
                customDialog.show()
                customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE)
                customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE)

                return false
            }
        }

        Toast.makeText(context, "Song added to playlist", Toast.LENGTH_SHORT).show()
        PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist.add(song)
        return true
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshPlaylist() {
        musicList = ArrayList()
        musicList = PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist
        notifyDataSetChanged()
    }
}
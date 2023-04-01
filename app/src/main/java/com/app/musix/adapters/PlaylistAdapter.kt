package com.app.musix.adapters

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.app.musix.PlaylistActivity
import com.app.musix.PlaylistDetails
import com.app.musix.databinding.PlaylistViewBinding
import com.app.musix.pojo.Playlist
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PlaylistAdapter(private val context: Context, private var playlists: ArrayList<Playlist>): RecyclerView.Adapter<PlaylistAdapter.MyHolder>() {
    class MyHolder(binding: PlaylistViewBinding): RecyclerView.ViewHolder(binding.root) {
        val name = binding.songNamePV
        val deleteBtn = binding.deleteBtnPV
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(PlaylistViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.name.text = playlists[position].name
        holder.name.isSelected = true

        holder.root.setOnClickListener{
            val intent = Intent(context, PlaylistDetails::class.java)
            intent.putExtra("index", position)
            ContextCompat.startActivity(context, intent, null)
        }

        holder.deleteBtn.setOnClickListener {
            // showing a confirmation dialog
            val builder = MaterialAlertDialogBuilder(context)
            builder.setTitle("Delete Playlist")
                .setMessage("Are you sure you want to delete playlist " + playlists[position].name + "?\nNote: This action is irreversible")
                .setPositiveButton("Yes") {_, _ ->
                    deletePlaylist(position)
                }
                .setNegativeButton("No") {dialog, _ -> dialog.dismiss()}

            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE)
            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE)
        }
    }

    override fun getItemCount(): Int {
        return playlists.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun refreshPlaylist() {
        playlists = ArrayList()
        playlists.addAll(PlaylistActivity.musicPlaylist.ref)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun deletePlaylist(index: Int) {
        playlists.removeAt(index)
        notifyDataSetChanged()
    }
}
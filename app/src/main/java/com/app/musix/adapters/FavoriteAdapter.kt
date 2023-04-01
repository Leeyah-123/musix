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
import com.app.musix.PlayerActivity
import com.app.musix.R
import com.app.musix.databinding.FavoriteViewBinding
import com.app.musix.pojo.Music
import com.app.musix.pojo.formatDuration
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class FavoriteAdapter(private val context: Context, private var musicList: ArrayList<Music>) : RecyclerView.Adapter<FavoriteAdapter.MyHolder>() {
    class MyHolder(binding: FavoriteViewBinding): RecyclerView.ViewHolder(binding.root) {
        val title = binding.songNameFV
        val album = binding.songAlbumFV
        val image = binding.imageViewFV
        val duration = binding.songDurationFV
        val deleteBtn = binding.deleteBtnFV
        val root = binding.root
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        return MyHolder(FavoriteViewBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: MyHolder, position: Int) {
        holder.title.text = musicList[position].title
        holder.album.text = musicList[position].album
        holder.duration.text = formatDuration(musicList[position].duration)

        Glide.with(context)
            .load(musicList[position].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player_icon).centerCrop()) // default icon if no icon is present
            .into(holder.image)

        holder.deleteBtn.setOnClickListener {
            // showing a confirmation dialog
            val builder = MaterialAlertDialogBuilder(context)
            builder.setTitle("Remove")
                .setMessage("Are you sure you want to remove this song from favorites?")
                .setPositiveButton("Yes") {_, _ ->
                    removeFromFavorites(position)
                }
                .setNegativeButton("No") {dialog, _ -> dialog.dismiss()}

            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE)
            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE)
        }

        holder.root.setOnClickListener {
            val intent = Intent(context, PlayerActivity::class.java)
            intent.putExtra("index", position)
            intent.putExtra("class", "FavoriteAdapter")
            ContextCompat.startActivity(context, intent, null)
        }
    }

    override fun getItemCount(): Int {
        return musicList.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun removeFromFavorites(index: Int) {
        musicList.removeAt(index)
        notifyDataSetChanged()
    }
}
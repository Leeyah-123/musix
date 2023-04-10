package com.app.musix.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.app.musix.*
import com.app.musix.pojo.exitApplication
import com.app.musix.pojo.favoriteChecker
import com.app.musix.pojo.setSongPosition
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class NotificationReceiver: BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onReceive(context: Context?, intent: Intent?) {
        when(intent?.action) {
            ApplicationClass.PREVIOUS -> prevNextSong(false, context!!)
            ApplicationClass.PLAY -> if (PlayerActivity.isPlaying) pauseMusic() else playMusic()
            ApplicationClass.NEXT -> prevNextSong(true, context!!)
            ApplicationClass.EXIT -> {
               exitApplication()
            }
        }
    }

    private fun playMusic() {
        PlayerActivity.isPlaying = true
        PlayerActivity.musicService?.mediaPlayer?.start()
        PlayerActivity.musicService?.showNotification(R.drawable.pause_icon, 1F)
        PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
        NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon)
    }

    private fun pauseMusic() {
        PlayerActivity.isPlaying = false
        PlayerActivity.musicService?.mediaPlayer?.pause()
        PlayerActivity.musicService?.showNotification(R.drawable.play_icon, 0F)
        PlayerActivity.binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
        NowPlaying.binding.playPauseBtnNP.setIconResource(R.drawable.play_icon)
    }

    private fun prevNextSong(increment: Boolean, context: Context) {
        setSongPosition(increment)
        PlayerActivity.musicService?.createMediaPlayer()

        PlayerActivity.musicService?.mediaPlayer?.start()
        PlayerActivity.isPlaying = true

        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player_icon).centerCrop()) // default icon if no icon is present
            .into(NowPlaying.binding.songImageNP)

        Glide.with(context)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player_icon).centerCrop()) // default icon if no icon is present
            .into(PlayerActivity.binding.songImagePA)

        NowPlaying.binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        PlayerActivity.binding.songNamePA.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        PlayerActivity.fIndex = favoriteChecker(PlayerActivity.musicListPA[PlayerActivity.songPosition].id)

        if (PlayerActivity.isFavorite) PlayerActivity.binding.favoriteBtnPA.setImageResource(R.drawable.favorites_icon)
        else PlayerActivity.binding.favoriteBtnPA.setImageResource(R.drawable.favorite_empty_icon)

        playMusic()
    }
}
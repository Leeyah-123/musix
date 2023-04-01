package com.app.musix

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.app.musix.databinding.FragmentNowPlayingBinding
import com.app.musix.pojo.setSongPosition
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

class NowPlaying : Fragment() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var binding: FragmentNowPlayingBinding
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        requireContext().theme.applyStyle(MainActivity.currentTheme[MainActivity.themeIndex], true)
        val view = inflater.inflate(R.layout.fragment_now_playing, container, false)
        binding = FragmentNowPlayingBinding.bind(view)
        binding.root.visibility = View.INVISIBLE

        binding.root.setOnClickListener {
            val intent = Intent(requireContext(), PlayerActivity::class.java)
            intent.putExtra("index", PlayerActivity.songPosition)
            intent.putExtra("class", "NowPlaying")
            ContextCompat.startActivity(requireContext(), intent, null)
        }

        binding.playPauseBtnNP.setOnClickListener {
            if (PlayerActivity.isPlaying) pauseMusic() else playMusic()
        }

        binding.nextBtnNP.setOnClickListener { nextSong() }

        return view
    }

    override fun onResume() {
        super.onResume()
        if (PlayerActivity.musicService != null) {
            binding.root.visibility = View.VISIBLE
            binding.songNameNP.isSelected = true
            Glide.with(this)
                .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
                .apply(RequestOptions().placeholder(R.drawable.music_player_icon).centerCrop()) // default icon if no icon is present
                .into(binding.songImageNP)
            binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
            if (PlayerActivity.isPlaying) binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon)
            else binding.playPauseBtnNP.setIconResource(R.drawable.play_icon)
        }
    }

    private fun playMusic() {
        PlayerActivity.musicService?.mediaPlayer!!.start()
        binding.playPauseBtnNP.setIconResource(R.drawable.pause_icon)
        PlayerActivity.musicService?.showNotification(R.drawable.pause_icon)
        PlayerActivity.binding.nextBtn.setIconResource(R.drawable.pause_icon)
        PlayerActivity.isPlaying = true
    }

    private fun pauseMusic() {
        PlayerActivity.musicService?.mediaPlayer!!.pause()
        binding.playPauseBtnNP.setIconResource(R.drawable.play_icon)
        PlayerActivity.musicService?.showNotification(R.drawable.play_icon)
        PlayerActivity.binding.nextBtn.setIconResource(R.drawable.play_icon)
        PlayerActivity.isPlaying = false
    }

    private fun nextSong() {
        setSongPosition(true)
        PlayerActivity.musicService?.createMediaPlayer()

        PlayerActivity.musicService?.mediaPlayer?.start()
        PlayerActivity.isPlaying = true

        Glide.with(this)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player_icon).centerCrop()) // default icon if no icon is present
            .into(binding.songImageNP)
        Glide.with(this)
            .load(PlayerActivity.musicListPA[PlayerActivity.songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player_icon).centerCrop()) // default icon if no icon is present
            .into(PlayerActivity.binding.songImagePA)

        PlayerActivity.binding.songNamePA.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title
        binding.songNameNP.text = PlayerActivity.musicListPA[PlayerActivity.songPosition].title

        PlayerActivity.musicService?.showNotification(R.drawable.pause_icon)
        playMusic()
    }

}
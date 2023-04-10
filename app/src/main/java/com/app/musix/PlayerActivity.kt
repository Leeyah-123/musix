package com.app.musix

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.audiofx.AudioEffect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.app.musix.databinding.ActivityPlayerBinding
import com.app.musix.notification.MusicService
import com.app.musix.pojo.*
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class PlayerActivity : AppCompatActivity(), ServiceConnection, MediaPlayer.OnCompletionListener {

    companion object {
        lateinit var musicListPA: ArrayList<Music>

        var songPosition: Int = 0
        var isPlaying: Boolean = false
        var musicService: MusicService? = null
        var repeat: Boolean = false

        var min15: Boolean = false
        var min30: Boolean = false
        var min60: Boolean = false

        var nowPlayingId: String = ""

        var isFavorite: Boolean = false
        var fIndex: Int = -1

        @SuppressLint("StaticFieldLeak")
        lateinit var binding: ActivityPlayerBinding
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentTheme[MainActivity.themeIndex])

//        if (Build.VERSION.SDK_INT >= 27) {
//            setShowWhenLocked(true)
//            setTurnScreenOn(true)
//        }
//        else
//            window.addFlags(
//                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
//                        or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
//            )

        binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initializeLayout()

        binding.backBtnPA.setOnClickListener { finish() }

        binding.playPauseBtnPA.setOnClickListener {if (isPlaying) pauseMusic() else playMusic() }

        binding.previousBtn.setOnClickListener { prevNextSong(false) }

        binding.nextBtn.setOnClickListener { prevNextSong(true) }

        binding.favoriteBtnPA.setOnClickListener {
            if (isFavorite) {
                isFavorite = false
                binding.favoriteBtnPA.setImageResource(R.drawable.favorite_empty_icon)
                FavoritesActivity.favoriteSongs.removeAt(fIndex)
            }
            else {
                isFavorite = true
                binding.favoriteBtnPA.setImageResource(R.drawable.favorites_icon)
                FavoritesActivity.favoriteSongs.add(musicListPA[songPosition])
            }
        }

        binding.equalizerBtnPA.setOnClickListener {
            try {
                val eqIntent = Intent(AudioEffect.ACTION_DISPLAY_AUDIO_EFFECT_CONTROL_PANEL)
                eqIntent.putExtra(
                    AudioEffect.EXTRA_AUDIO_SESSION,
                    musicService?.mediaPlayer!!.audioSessionId
                )
                eqIntent.putExtra(AudioEffect.EXTRA_PACKAGE_NAME, baseContext.packageName)
                eqIntent.putExtra(AudioEffect.EXTRA_CONTENT_TYPE, AudioEffect.CONTENT_TYPE_MUSIC)

                startActivityForResult(eqIntent, 13)
            } catch (e: Exception) {
                Toast.makeText(this, "Equalizer feature not supported", Toast.LENGTH_SHORT).show()
            }
        }

        binding.timerBtnPA.setOnClickListener {
            if (min15 || min30 || min60) {
                val builder = MaterialAlertDialogBuilder(this)
                builder.setTitle("Stop Timer")
                    .setMessage("Do you want to stop timer?")
                    .setPositiveButton("Yes") {_, _ ->
                        min15 = false
                        min30 = false
                        min60 = false

                        binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.cool_blue))
                        Toast.makeText(this, "Timer has been removed", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("No") {dialog, _ -> dialog.dismiss()}

                val customDialog = builder.create()
                customDialog.show()
                customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE)
                customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE)
            }

            else showBottomSheetDialog()
        }

        binding.shareBtnPA.setOnClickListener {
            val shareIntent = Intent()
            shareIntent.action = Intent.ACTION_SEND
            shareIntent.type = "audio/*"
            shareIntent.putExtra(Intent.EXTRA_STREAM, Uri.parse(musicListPA[songPosition].path))
            startActivity(Intent.createChooser(shareIntent, "Share Music File"))
        }

        binding.repeatBtnPa.setOnClickListener {
            if (!repeat) {
                repeat = true
                binding.repeatBtnPa.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
                Toast.makeText(this, "Repeat is on", Toast.LENGTH_SHORT).show()
            } else {
                repeat = false
                binding.repeatBtnPa.setColorFilter(ContextCompat.getColor(this, R.color.cool_blue))
                Toast.makeText(this, "Repeat is off", Toast.LENGTH_SHORT).show()
            }
        }

        binding.seekbarPA.setOnSeekBarChangeListener(object: SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekbar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) musicService?.mediaPlayer?.seekTo(progress)
            }

            override fun onStartTrackingTouch(seekbar: SeekBar?) = Unit // Unit corresponds to void in Java
            override fun onStopTrackingTouch(p0: SeekBar?) = Unit
        })
    }

    private fun setLayout() {
        fIndex = favoriteChecker(musicListPA[songPosition].id)

        Glide.with(this@PlayerActivity)
            .load(musicListPA[songPosition].artUri)
            .apply(RequestOptions().placeholder(R.drawable.music_player_icon).centerCrop()) // default icon if no icon is present
            .into(binding.songImagePA)

        binding.songNamePA.text = musicListPA[songPosition].title

        if (repeat) binding.repeatBtnPa.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
        if (min15 || min30 || min60) binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))

        if (isFavorite) binding.favoriteBtnPA.setImageResource(R.drawable.favorites_icon)
        else binding.favoriteBtnPA.setImageResource(R.drawable.favorite_empty_icon)
    }

    private fun createMediaPlayer() {
        try {
            if (musicService?.mediaPlayer == null) musicService?.mediaPlayer = MediaPlayer()

            // initialize media Player
            musicService?.mediaPlayer?.reset()
            musicService?.mediaPlayer?.setDataSource(musicListPA[songPosition].path)
            musicService?.mediaPlayer?.prepare()
            musicService?.mediaPlayer?.start()

            // setLayout
            isPlaying = true
            binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
            musicService?.showNotification(R.drawable.pause_icon, 1F)
            binding.tvSeekbarStart.text = formatDuration(musicService?.mediaPlayer!!.currentPosition.toLong())
            binding.tvSeekbarEnd.text = formatDuration(musicService?.mediaPlayer!!.duration.toLong())
            binding.seekbarPA.progress = musicService?.mediaPlayer!!.currentPosition
            binding.seekbarPA.max = musicService?.mediaPlayer!!.duration

            musicService?.mediaPlayer!!.setOnCompletionListener(this)

            nowPlayingId = musicListPA[songPosition].id
        } catch (e: Exception) {
            Log.i("ERROR= ", e.stackTraceToString())
        }
    }

    private fun initializeLayout() {
        songPosition = intent.getIntExtra("index", 0)

        when(intent.getStringExtra("class")) {
            "MusicAdapterSearch" -> {
                startBackgroundService()

                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.musicListSearch)
                setLayout()
            }
            "MusicAdapter" -> {
                startBackgroundService()

                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                setLayout()
            }
            "FavoriteAdapter" -> {
                startBackgroundService()

                musicListPA = ArrayList()
                musicListPA.addAll(FavoritesActivity.favoriteSongs)
                setLayout()
            }
            "PlaylistDetailsAdapter" -> {
                startBackgroundService()

                musicListPA = ArrayList()
                musicListPA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist)
                setLayout()
            }
            "MainActivity" -> {
                startBackgroundService()

                musicListPA = ArrayList()
                musicListPA.addAll(MainActivity.MusicListMA)
                musicListPA.shuffle()
                setLayout()
            }
            "FavoriteShuffle" -> {
                startBackgroundService()

                musicListPA = ArrayList()
                musicListPA.addAll(FavoritesActivity.favoriteSongs)
                musicListPA.shuffle()
                setLayout()
            }
            "PlaylistDetailsShuffle" -> {
                startBackgroundService()

                musicListPA = ArrayList()
                musicListPA.addAll(PlaylistActivity.musicPlaylist.ref[PlaylistDetails.currentPlaylistPos].playlist)
                musicListPA.shuffle()
                setLayout()
            }
            "NowPlaying" -> {
                setLayout()

                binding.tvSeekbarStart.text = formatDuration(musicService?.mediaPlayer!!.currentPosition.toLong())
                binding.tvSeekbarEnd.text = formatDuration(musicService?.mediaPlayer!!.duration.toLong())
                binding.seekbarPA.progress = musicService?.mediaPlayer!!.currentPosition
                binding.seekbarPA.max = musicService?.mediaPlayer!!.duration

                if (isPlaying) binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
                else binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
            }
        }
    }

    private fun playMusic() {
        isPlaying = true
        binding.playPauseBtnPA.setIconResource(R.drawable.pause_icon)
        musicService?.showNotification(R.drawable.pause_icon, 1F)
        musicService?.mediaPlayer?.start()
    }

    private fun pauseMusic() {
        isPlaying = false
        binding.playPauseBtnPA.setIconResource(R.drawable.play_icon)
        musicService?.showNotification(R.drawable.play_icon, 0F)
        musicService?.mediaPlayer?.pause()
    }

    private fun prevNextSong(increment: Boolean) {
        setSongPosition(increment)
        setLayout()
        createMediaPlayer()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onServiceConnected(name: ComponentName?, service
    : IBinder?) {
        val binder = service as MusicService.MyBinder
        musicService = binder.currentService()

        createMediaPlayer()
        musicService?.seekBarSetup()

        musicService!!.audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).setOnAudioFocusChangeListener(
            musicService!!
        ).build()
        musicService!!.audioManager.requestAudioFocus(focusRequest)
    }

    override fun onServiceDisconnected(name: ComponentName?) {
        musicService = null
    }

    override fun onCompletion(p0: MediaPlayer?) {
        if (!repeat) setSongPosition(true)
        createMediaPlayer()
        try {setLayout()} catch(e: Exception) {return}
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 13 || resultCode == RESULT_OK)
            return
    }

    @RequiresApi(Build.VERSION_CODES.N)
    private fun showBottomSheetDialog() {
        val dialog = BottomSheetDialog(this@PlayerActivity)
        dialog.setContentView(R.layout.bottom_sheet_dialog)
        dialog.show()

        dialog.findViewById<LinearLayout>(R.id.min_15)?.setOnClickListener {
            Toast.makeText(baseContext, "Music will stop after 15 minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            min15 = true
            Thread{
                Thread.sleep((15 * 60 * 1000).toLong())
                if (min15) {
                    finishAffinity()
                    exitApplication()
                }
            }.start()
            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.min_30)?.setOnClickListener {
            Toast.makeText(baseContext, "Music will stop after 30 minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            min30 = true
            Thread{
                Thread.sleep((30 * 60 * 1000).toLong())
                if (min30) {
                    finishAffinity()
                    exitApplication()
                }
            }.start()
            dialog.dismiss()
        }

        dialog.findViewById<LinearLayout>(R.id.min_60)?.setOnClickListener {
            Toast.makeText(baseContext, "Music will stop after 60 minutes", Toast.LENGTH_SHORT).show()
            binding.timerBtnPA.setColorFilter(ContextCompat.getColor(this, R.color.purple_500))
            min60 = true
            Thread{
                Thread.sleep((60 * 60 * 1000).toLong())
                if (min60) {
                    finishAffinity()
                    exitApplication()
                }
            }.start()
            dialog.dismiss()
        }
    }

    private fun startBackgroundService() {
        val intent = Intent(this, MusicService::class.java)
        bindService(intent, this, BIND_AUTO_CREATE)
        startService(intent)
    }
}

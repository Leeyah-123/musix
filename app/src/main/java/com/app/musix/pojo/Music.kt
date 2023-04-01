package com.app.musix.pojo

import android.app.Service
import android.media.MediaMetadataRetriever
import android.os.Build
import androidx.annotation.RequiresApi
import com.app.musix.FavoritesActivity
import com.app.musix.PlayerActivity
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.system.exitProcess

data class Music(val id: String, val title: String, val album: String, val artist: String, val duration: Long = 0, val path: String, val artUri: String)

class Playlist {
    lateinit var name: String
    var playlist: ArrayList<Music> = ArrayList()
    lateinit var createdOn: String
}

class MusicPlaylist {
    var ref: ArrayList<Playlist> = ArrayList()
}

fun formatDuration(duration: Long):String {
    // Get minutes value from song duration
    val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)

    // Get seconds value and minus minutes value
    val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) - minutes * TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))

    return String.format("%02d:%02d", minutes, seconds)
}

fun getImageArt(path: String): ByteArray? {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(path)
    return retriever.embeddedPicture
}

fun setSongPosition(increment: Boolean) {
    if (increment) {
        if (PlayerActivity.musicListPA.size - 1 == PlayerActivity.songPosition) PlayerActivity.songPosition = 0
        else ++PlayerActivity.songPosition
    }
    else {
        if (PlayerActivity.songPosition == 0) PlayerActivity.songPosition = PlayerActivity.musicListPA.size - 1
        else --PlayerActivity.songPosition
    }
}

fun favoriteChecker(id: String): Int {
    PlayerActivity.isFavorite = false

    FavoritesActivity.favoriteSongs.forEachIndexed { index, music ->
        if (id == music.id) {
            PlayerActivity.isFavorite = true
            return index
        }
    }

    return -1
}

@RequiresApi(Build.VERSION_CODES.N)
fun exitApplication() {
    if (PlayerActivity.musicService != null) {
        PlayerActivity.musicService?.audioManager?.abandonAudioFocus(PlayerActivity.musicService)
        PlayerActivity.musicService?.stopForeground(Service.STOP_FOREGROUND_REMOVE)
        PlayerActivity.musicService?.mediaPlayer?.release()
        PlayerActivity.musicService = null
    }
    exitProcess(1)
}

fun checkPlaylist(playlist: ArrayList<Music>): ArrayList<Music> {
    playlist.forEachIndexed{ index, music ->
        val file = File(music.path)
        if (!file.exists()) playlist.removeAt(index)
    }
    return playlist
}

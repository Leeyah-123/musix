package com.app.musix

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.widget.LinearLayout
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.app.musix.adapters.MusicAdapter
import com.app.musix.databinding.ActivityMainBinding
import com.app.musix.pojo.Music
import com.app.musix.pojo.MusicPlaylist
import com.app.musix.pojo.exitApplication
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var toggle: ActionBarDrawerToggle
    private lateinit var musicAdapter: MusicAdapter

    companion object{
        lateinit var MusicListMA: ArrayList<Music>
        lateinit var musicListSearch: ArrayList<Music>
        var search: Boolean = false
        var themeIndex: Int = 0
        var sortOrder: String = "ASC"
        var sortByIndex: Int = 0
        val sortTypes = arrayOf(MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.DATE_ADDED, MediaStore.Audio.Media.SIZE)
        val currentTheme = arrayOf(R.style.coolBlue, R.style.coolPink, R.style.coolGreen, R.style.blue, R.style.purple, R.style.black)
        val currentNavTheme = arrayOf(R.style.coolBlueNav, R.style.coolPinkNav, R.style.coolGreenNav, R.style.blueNav, R.style.purpleNav, R.style.blackNav)
        val currentGradient = arrayOf(R.drawable.gradient_cool_blue, R.drawable.gradient_cool_pink, R.drawable.gradient_cool_green, R.drawable.gradient_blue, R.drawable.gradient_purple, R.drawable.gradient_black)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val themeEditor = getSharedPreferences("THEMES", MODE_PRIVATE)
        themeIndex = themeEditor.getInt("themeIndex", 0)

        setTheme(currentNavTheme[themeIndex])
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        Initialize navigation drawer
        toggle = ActionBarDrawerToggle(this, binding.root, R.string.open, R.string.close)
        binding.root.addDrawerListener(toggle)
        toggle.syncState()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        if (requestRuntimePermission()) {
            // retrieving favorite songs stored in shared preferences
            val editor = getSharedPreferences("FAVORITES", MODE_PRIVATE)
            val jsonString = editor.getString("FavoriteSongs", null)

            if (jsonString != null) {
                val typeToken = object: TypeToken<ArrayList<Music>>(){}.type
                val data: ArrayList<Music> = GsonBuilder().create().fromJson(jsonString, typeToken)

                FavoritesActivity.favoriteSongs = ArrayList()
                FavoritesActivity.favoriteSongs.addAll(data)
            }

            // retrieving playlists stored in shared preferences
            val playlistsEditor = getSharedPreferences("PLAYLISTS", MODE_PRIVATE)
            val playlistsJsonString = playlistsEditor.getString("Playlists", null)

            if (playlistsJsonString != null) {
                val typeToken = object: TypeToken<MusicPlaylist>(){}.type
                val playlistData: MusicPlaylist = GsonBuilder().create().fromJson(playlistsJsonString, typeToken)

                PlaylistActivity.musicPlaylist = playlistData
            }

            initializeLayout()
        }

        binding.shuffleBtn.setOnClickListener {
            val intent = Intent(this@MainActivity, PlayerActivity::class.java)
            intent.putExtra("index", 0)
            intent.putExtra("class", "MainActivity")
            startActivity(intent)
        }

        binding.favoritesBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity, FavoritesActivity::class.java))
        }

        binding.playlistsBtn.setOnClickListener {
            startActivity(Intent(this@MainActivity, PlaylistActivity::class.java))
        }

        binding.navView.setNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navSettings -> startActivity(Intent(this@MainActivity, SettingsActivity::class.java))
                R.id.navAbout -> startActivity(Intent(this@MainActivity, AboutActivity::class.java))
                R.id.navExit -> {
                    val builder = MaterialAlertDialogBuilder(this)
                    builder.setTitle("Exit")
                        .setMessage("Are you sure you want to exit?")
                        .setPositiveButton("Yes") {_, _ ->
                            exitApplication()
                        }
                        .setNegativeButton("No") {dialog, _ -> dialog.dismiss()}

                    val customDialog = builder.create()
                    customDialog.show()
                    customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE)
                    customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE)
                }
            }
            true
        }
    }

//    Requesting necessary permissions
    @RequiresApi(Build.VERSION_CODES.P)
    private fun requestRuntimePermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 13)
            return false
        }
        return true
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 13) {
            if(grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                initializeLayout()
            }
            else {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), 13)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.R)
    @SuppressLint("SetTextI18n")
    private fun initializeLayout() {
//        Initialize music recycler view and adapter
        val sortingEditor = getSharedPreferences("SORTING", MODE_PRIVATE)
        val sortOrderValue = sortingEditor.getString("SortOrder", "ASC")
        val sortByIndexValue = sortingEditor.getInt("SortBy", 0)

        search = false
        MusicListMA = getAllAudio(sortType = sortByIndexValue, sortOrder = sortOrderValue!!)

        binding.musicRV.setHasFixedSize(true)
        binding.musicRV.setItemViewCacheSize(13)
        binding.musicRV.layoutManager = LinearLayoutManager(this@MainActivity)
        musicAdapter = MusicAdapter(this@MainActivity, MusicListMA)
        binding.musicRV.adapter = musicAdapter
        binding.totalSongs.text = "Total Songs: " + musicAdapter.itemCount
    }

    @SuppressLint("Range")
    @RequiresApi(Build.VERSION_CODES.R)
    private fun getAllAudio(sortType: Int = 0, sortOrder: String = " ASC"): ArrayList<Music> {
        val tempList = ArrayList<Music>()
        val selection = MediaStore.Audio.Media.IS_MUSIC + " != 0"
        val projection = arrayOf(MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATE_ADDED,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.SIZE,
        )

//        Get All Audio File in Phone
        val cursor = this.contentResolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            projection, selection, null,
            sortTypes[sortType] + " " + sortOrder,
            null
        )

        if(cursor != null)
            if(cursor.moveToFirst())
                do {
                    val id = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE))
                    val album = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM))
                    val artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST))
                    val duration = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.DURATION))
                    val path = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA))
                    val albumID = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)).toString()
                    val uri = Uri.parse("content://media/external/audio/albumart")
                    val artURI = Uri.withAppendedPath(uri, albumID).toString()

                    val music = Music(id, title, album, artist, duration, path, artURI)
                    val file = File(music.path)
                    if (file.exists())
                        tempList.add(music)
                } while (cursor.moveToNext())

        cursor?.close()

        return tempList
    }

    @RequiresApi(Build.VERSION_CODES.R)
    override fun onResume() {
        super.onResume()

        // For persisting favorites data using shared preferences
        val editor = getSharedPreferences("FAVORITES", MODE_PRIVATE).edit()
        val jsonString = GsonBuilder().create().toJson(FavoritesActivity.favoriteSongs)
        editor.putString("FavoriteSongs", jsonString)
        editor.apply()

        // For retrieving sort data using shared preferences
        val sortingEditor = getSharedPreferences("SORTING", MODE_PRIVATE)
        val sortOrderValue = sortingEditor.getString("SortOrder", "ASC")
        val sortByIndexValue = sortingEditor.getInt("SortBy", 0)

        if (sortByIndex != sortByIndexValue || sortOrder != sortOrderValue) {
            sortByIndex = sortByIndexValue
            sortOrder = sortOrderValue!!

            MusicListMA = getAllAudio(sortType = sortByIndexValue, sortOrder = sortOrderValue)
            musicAdapter.updateMusicList(MusicListMA)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onDestroy() {
        super.onDestroy()
        if (!PlayerActivity.isPlaying) exitApplication()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_view_menu, menu)

        // set nav header gradient
        findViewById<LinearLayout>(R.id.navHeader)?.setBackgroundResource(currentGradient[themeIndex])

        val searchView = menu?.findItem(R.id.search_view)?.actionView as SearchView

        searchView.setOnQueryTextListener(object: SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean = true

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null && newText != "") {
                    search = true
                    musicListSearch = ArrayList()
                    val userInput = newText.lowercase()
                    for (song in MusicListMA) {
                        if (song.title.lowercase().contains(userInput)) musicListSearch.add(song)
                    }
                    musicAdapter.updateMusicList(musicListSearch)
                } else {
                    search = false
                    musicAdapter.updateMusicList(MusicListMA)
                }

                return true
            }

        })

        return super.onCreateOptionsMenu(menu)
    }
}
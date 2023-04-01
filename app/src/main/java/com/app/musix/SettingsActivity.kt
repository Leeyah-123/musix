package com.app.musix

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.media.AudioManager
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.app.musix.databinding.ActivitySettingsBinding
import com.app.musix.pojo.exitApplication
import com.google.android.material.dialog.MaterialAlertDialogBuilder


class SettingsActivity : AppCompatActivity() {
    lateinit var binding: ActivitySettingsBinding
    lateinit var audioManager: AudioManager

    @SuppressLint("ApplySharedPref", "SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(MainActivity.currentNavTheme[MainActivity.themeIndex])
        binding = ActivitySettingsBinding.inflate(layoutInflater)
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        setContentView(binding.root)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Settings"

        // set version name
        binding.versionName.text = setVersionDetails()
        binding.volumeSeekbar.progress = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
        binding.volumeLabel.text = "Volume : ${audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)}"

        // set selected theme
        when(MainActivity.themeIndex) {
            0 -> binding.coolBlueTheme.setBackgroundColor(Color.CYAN)
            1 -> binding.coolPinkTheme.setBackgroundColor(Color.CYAN)
            2 -> binding.coolGreenTheme.setBackgroundColor(Color.CYAN)
            3 -> binding.blueTheme.setBackgroundColor(Color.CYAN)
            4 -> binding.purpleTheme.setBackgroundColor(Color.CYAN)
            5 -> binding.blackTheme.setBackgroundColor(Color.CYAN)
        }

        // set selected sort order
        when(MainActivity.sortOrder) {
            "ASC" -> {
                binding.ascRadioBtn.isChecked = true
                binding.descRadioBtn.isChecked = false
            }
            "DESC" -> {
                binding.ascRadioBtn.isChecked = false
                binding.descRadioBtn.isChecked = true
            }
        }

        // theme changers
        binding.coolBlueTheme.setOnClickListener { saveTheme(0) }
        binding.coolPinkTheme.setOnClickListener { saveTheme(1) }
        binding.coolGreenTheme.setOnClickListener { saveTheme(2) }
        binding.blueTheme.setOnClickListener { saveTheme(3) }
        binding.purpleTheme.setOnClickListener { saveTheme(4) }
        binding.blackTheme.setOnClickListener { saveTheme(5) }

        // sort type
        binding.sortBtn.setOnClickListener {
            val menuList = arrayOf("Song Title", "Recently Added", "File Size")
            var currentSortType = MainActivity.sortByIndex

            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Sort Music By")
                .setPositiveButton("OK") {_, _ ->
                    val editor = getSharedPreferences("SORTING", MODE_PRIVATE).edit()
                    editor.putInt("SortBy", currentSortType)
                    editor.apply()
                }
                .setSingleChoiceItems(menuList, currentSortType) {_, index ->
                    currentSortType = index
                }

            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE)
        }

        // sort order
        binding.radioGroup.setOnCheckedChangeListener { _, _ ->
            val sortOrder: String = if (binding.ascRadioBtn.isChecked) "ASC" else "DESC"

            val editor = getSharedPreferences("SORTING", MODE_PRIVATE).edit()
            editor.putString("SortOrder", sortOrder)
            editor.commit()
        }

        binding.volumeSeekbar.max = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC)

        binding.volumeSeekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            @SuppressLint("SetTextI18n")
            override fun onProgressChanged(seekBar: SeekBar, newVolume: Int, b: Boolean) {
                binding.volumeLabel.text = "Volume : $newVolume"
                audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, newVolume, 0)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

    }

    @SuppressLint("ApplySharedPref")
    @RequiresApi(Build.VERSION_CODES.N)
    private fun saveTheme(index: Int) {
        if (MainActivity.themeIndex != index) {
            val builder = MaterialAlertDialogBuilder(this)
            builder.setTitle("Apply theme")
                .setMessage("Do you want to apply theme?\nNote: App will automatically restart to apply changes")
                .setPositiveButton("Yes") {_, _ ->
                    val editor = getSharedPreferences("THEMES", MODE_PRIVATE).edit()
                    editor.putInt("themeIndex", index)
                    editor.commit()
                    exitApplication()
                }
                .setNegativeButton("No") {dialog, _ -> dialog.dismiss()}

            val customDialog = builder.create()
            customDialog.show()
            customDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.BLUE)
            customDialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.BLUE)
        }
    }

    private fun setVersionDetails(): String {
        return "Version Name: ${BuildConfig.VERSION_NAME}"
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
}
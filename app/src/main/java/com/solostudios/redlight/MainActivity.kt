/*
 * RedLight - minimalist red screen app for Android
 * Copyright (c) 2025 Eugene Gutin <solostudios@gmail.com>
 *
 * Licensed under the MIT License. See LICENSE file in the project root for details.
 */

package com.solostudios.redlight

import android.content.ContentResolver
import android.content.Context
import android.graphics.Color
import android.os.*
import android.provider.Settings
import android.view.Gravity
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.animation.ScaleAnimation
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import android.view.View
import android.view.animation.Animation

class MainActivity : AppCompatActivity() {

    private lateinit var contentResolver: ContentResolver
    private lateinit var brightnessSlider: SeekBar
    private lateinit var brightnessLabel: TextView
    private var previousBrightness: Int = 125
    private val prefsName = "RedLightPrefs"
    private val brightnessKey = "brightness_level"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        contentResolver = applicationContext.contentResolver
        val prefs = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
        val savedBrightness = prefs.getInt(brightnessKey, 125)

        try {
            previousBrightness = Settings.System.getInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            )
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
        }

        val layout = FrameLayout(this)
        layout.setBackgroundColor(Color.RED)

        // Brightness label
        brightnessLabel = TextView(this).apply {
            text = getString(R.string.brightness_label, savedBrightness * 100 / 255)
            setTextColor(Color.BLACK)
            textSize = 18f
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
            ).apply {
                bottomMargin = 480
            }
        }

        // Close button
        val closeButton = TextView(this).apply {
            text = "✕"
            setTextColor(Color.BLACK)
            textSize = 24f
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(160, 160, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL).apply {
                bottomMargin = 160
            }

            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor(Color.RED)
                setStroke(5, Color.BLACK)
            }

            setOnClickListener {
                vibrateOnce()
                animateButtonPress(this)
                Handler(Looper.getMainLooper()).postDelayed({
                    finishAffinity()
                }, 100) // small delay for animation
            }
        }

        // Brightness slider
        brightnessSlider = SeekBar(this).apply {
            max = 255
            progress = savedBrightness
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
            ).apply {
                bottomMargin = 400
                marginStart = 50
                marginEnd = 50
            }
        }

        brightnessSlider.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                val safeBrightness = progress.coerceIn(10, 255)
                applyBrightness(safeBrightness)
                brightnessLabel.text = getString(R.string.brightness_label, safeBrightness * 100 / 255)
                prefs.edit {
                    putInt(brightnessKey, safeBrightness)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        layout.addView(brightnessLabel)
        layout.addView(brightnessSlider)
        layout.addView(closeButton)
        setContentView(layout)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.insetsController?.let { controller ->
            controller.hide(
                WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()
            )
            controller.systemBarsBehavior =
                WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        applyBrightness(savedBrightness)
    }

    override fun onResume() {
        super.onResume()
        val savedBrightness = getSharedPreferences(prefsName, Context.MODE_PRIVATE)
            .getInt(brightnessKey, 125)
        applyBrightness(savedBrightness)
    }

    override fun onDestroy() {
        super.onDestroy()
        applyBrightness(previousBrightness)

        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.insetsController?.show(
            WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars()
        )
    }

    private fun applyBrightness(brightness: Int) {
        val lp = window.attributes
        lp.screenBrightness = brightness / 255f
        window.attributes = lp
    }

    private fun vibrateOnce() {
        val vibrator = getSystemService(VibratorManager::class.java).defaultVibrator
        vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
    }

    private fun animateButtonPress(view: View) {
        val animation = ScaleAnimation(
            1.0f, 1.2f, // X scale
            1.0f, 1.2f, // Y scale
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 80
            repeatMode = Animation.REVERSE
            repeatCount = 1
        }
        view.startAnimation(animation)
    }
}

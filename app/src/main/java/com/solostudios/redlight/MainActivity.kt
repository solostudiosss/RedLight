package com.solostudios.redlight

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.VibrationEffect
import android.os.VibratorManager
import android.provider.Settings
import android.view.Gravity
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import android.view.animation.Animation
import android.view.animation.ScaleAnimation
import android.widget.FrameLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.edit
import androidx.core.view.WindowCompat
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import android.view.MotionEvent
import android.view.GestureDetector
import kotlin.math.roundToInt
import android.app.Dialog
import android.widget.Button

class MainActivity : AppCompatActivity() {

    private lateinit var contentResolver: ContentResolver
    private lateinit var brightnessSlider: SeekBar
    private lateinit var brightnessLabel: TextView
    private lateinit var redOverlay: View
    private var previousBrightness: Int = 125
    private val prefsName = "RedLightPrefs"
    private val brightnessKey = "brightness_level"

    // GestureDetector must be a class-level variable!
    private lateinit var gestureDetector: GestureDetector

    /*  Tint selector  */
    private val tintList = arrayOf(
        "#FF0000".toColorInt(), // 0 Red
        "#DC143C".toColorInt(), // 1 Crimson
        "#B22222".toColorInt(), // 2 Firebrick
        "#9B111E".toColorInt(), // 3 Ruby
        "#8B0000".toColorInt(), // 4 DarkRed (default)
        "#800000".toColorInt(), // 5 Maroon
        "#800020".toColorInt(), // 6 Burgundy
        "#65000B".toColorInt(), // 7 Rosewood
        "#4A0000".toColorInt()  // 8 Oxblood
    )
    private val tintIndexKey = "red_tint_index"
    private val tooltipShownKey = "tooltip_shown"

    private var currentTintIndex = 4  // default (#8B0000)

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        contentResolver = applicationContext.contentResolver
        val prefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        val savedBrightness = prefs.getInt(brightnessKey, 125)
        currentTintIndex = prefs.getInt(tintIndexKey, 4)

        try {
            previousBrightness = Settings.System.getInt(
                contentResolver,
                Settings.System.SCREEN_BRIGHTNESS
            )
        } catch (e: Settings.SettingNotFoundException) {
            e.printStackTrace()
        }

        val layout = CustomFrameLayout(this)
        layout.setBackgroundColor(Color.BLACK)

        redOverlay = View(this).apply {
            setBackgroundColor(tintList[currentTintIndex])
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT
            )
        }
        layout.addView(redOverlay, 0)

        val swipeArea = View(this).apply {
            layoutParams = FrameLayout.LayoutParams(
                (resources.displayMetrics.widthPixels * 0.2).toInt(),
                FrameLayout.LayoutParams.MATCH_PARENT,
                Gravity.START
            )
            setBackgroundColor(Color.TRANSPARENT)
        }
        layout.addView(swipeArea)

        swipeArea.setOnTouchListener { _, event ->
            val heightPx = resources.displayMetrics.heightPixels
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    swipeArea.performClick()
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val fraction = event.y / heightPx
                    currentTintIndex = (fraction * (tintList.size - 1))
                        .roundToInt()
                        .coerceIn(0, tintList.size - 1)

                    redOverlay.setBackgroundColor(tintList[currentTintIndex])
                    getSharedPreferences(prefsName, MODE_PRIVATE).edit {
                        putInt(tintIndexKey, currentTintIndex)
                    }
                    true
                }
                else -> false
            }
        }

        brightnessLabel = TextView(this).apply {
            text = getString(R.string.brightness_label, savedBrightness * 100 / 255)
            setTextColor(Color.BLACK)
            textSize = 18f
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM
            ).apply { bottomMargin = 480 }
        }
        layout.addView(brightnessLabel)

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
                prefs.edit { putInt(brightnessKey, safeBrightness) }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        layout.addView(brightnessSlider)

        val closeButton = TextView(this).apply {
            text = "✕"
            setTextColor(Color.BLACK)
            textSize = 24f
            gravity = Gravity.CENTER
            layoutParams = FrameLayout.LayoutParams(160, 160, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL).apply { bottomMargin = 160 }
            background = android.graphics.drawable.GradientDrawable().apply {
                shape = android.graphics.drawable.GradientDrawable.OVAL
                setColor("#8B0000".toColorInt())
                setStroke(5, Color.BLACK)
            }
            setOnClickListener {
                vibrateOnce()
                animateButtonPress(this)
                Handler(Looper.getMainLooper()).postDelayed({ finishAffinity() }, 100)
            }
        }
        layout.addView(closeButton)

        val beerLink = TextView(this).apply {
            text = getString(R.string.buy_me_a_beer)
            textSize = 12f
            paintFlags = paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
            setTextColor("#99000000".toColorInt())
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.END
            ).apply { bottomMargin = 30; marginEnd = 30 }
            setOnClickListener {
                val url = "https://www.buymeacoffee.com/utv8egbqbj"
                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW)
                intent.data = url.toUri()
                startActivity(intent)
            }
        }
        layout.addView(beerLink)

        val tipLink = TextView(this).apply {
            text = "\uD83D\uDCA1 ${getString(R.string.show_tip)}" // 💡 Show tip
            textSize = 12f
            paintFlags = paintFlags or android.graphics.Paint.UNDERLINE_TEXT_FLAG
            setTextColor("#99000000".toColorInt())
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.BOTTOM or Gravity.START
            ).apply { bottomMargin = 30; marginStart = 30 }
            setOnClickListener {
                showTipDialog()
            }
        }
        layout.addView(tipLink)

        setContentView(layout)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.insetsController?.let { controller ->
            controller.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            controller.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }

        // --- GESTURE DETECTOR ---
        gestureDetector = GestureDetector(this, object : GestureDetector.SimpleOnGestureListener() {
            override fun onDoubleTap(e: MotionEvent): Boolean {
                currentTintIndex = 4
                redOverlay.setBackgroundColor(tintList[currentTintIndex])
                getSharedPreferences(prefsName, MODE_PRIVATE).edit {
                    putInt(tintIndexKey, currentTintIndex)
                }
                return true
            }
        })

        val tooltipPrefs = getSharedPreferences(prefsName, MODE_PRIVATE)
        val tooltipShown = tooltipPrefs.getBoolean(tooltipShownKey, false)
        if (!tooltipShown) {
            showTipDialog()
            tooltipPrefs.edit { putBoolean(tooltipShownKey, true) }
        }

        applyBrightness(savedBrightness)
    }

    // --- GLOBAL GESTURE HANDLING ---
    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (gestureDetector.onTouchEvent(ev)) {
            return true
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onResume() {
        super.onResume()
        val savedBrightness = getSharedPreferences(prefsName, MODE_PRIVATE)
            .getInt(brightnessKey, 125)
        applyBrightness(savedBrightness)
        redOverlay.setBackgroundColor(tintList[currentTintIndex])
    }

    override fun onDestroy() {
        super.onDestroy()
        applyBrightness(previousBrightness)
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.insetsController?.show(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
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
            1.0f, 1.2f,
            1.0f, 1.2f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 80
            repeatMode = Animation.REVERSE
            repeatCount = 1
        }
        view.startAnimation(animation)
    }

    // --- TIP DIALOG FUNCTION ---
    private fun showTipDialog() {
        val fakeParent = FrameLayout(this)
        val view = layoutInflater.inflate(R.layout.tip_dialog, fakeParent, false)
        val dialog = Dialog(this)
        dialog.setContentView(view)

        val tipText = view.findViewById<TextView>(R.id.tipText)
        tipText.text = getString(R.string.tip_message)

        val closeBtn = view.findViewById<Button>(R.id.closeTip)
        closeBtn.text = getString(R.string.close)
        closeBtn.setOnClickListener { dialog.dismiss() }

        dialog.setCanceledOnTouchOutside(true)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        dialog.show()
    }

}

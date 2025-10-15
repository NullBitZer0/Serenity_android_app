package com.example.serenity_mad_le3

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.StateListDrawable
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.serenity_mad_le3.data.Prefs
import com.mikepenz.iconics.IconicsDrawable
import com.mikepenz.iconics.typeface.library.fontawesome.FontAwesome
import com.mikepenz.iconics.utils.colorInt
import com.mikepenz.iconics.utils.sizeDp


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Show splash immediately (Android 12+ and backport)
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val prefs = Prefs(this)
        // Apply the persisted theme before inflating views to avoid a visible flash.
        AppCompatDelegate.setDefaultNightMode(
            if (prefs.getSettings().darkModeEnabled) {
                AppCompatDelegate.MODE_NIGHT_YES
            } else {
                AppCompatDelegate.MODE_NIGHT_NO
            }
        )
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val navController = findNavController(R.id.nav_host_fragment)
        // Use static start destination from nav graph (no onboarding)
        val graph = navController.navInflater.inflate(R.navigation.nav_graph)
        navController.graph = graph
        val bottom = findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(R.id.bottom_nav)
        bottom.setupWithNavController(navController)
        val iconSize = (resources.getDimension(R.dimen.bottom_nav_icon_size) / resources.displayMetrics.density).toInt()
        val contourWidth = resources.getDimensionPixelSize(R.dimen.bottom_nav_outline_width)
        fun statefulIcon(icon: FontAwesome.Icon): StateListDrawable {
            return StateListDrawable().apply {
                addState(
                    intArrayOf(android.R.attr.state_checked),
                    IconicsDrawable(this@MainActivity, icon).apply {
                        sizeDp = iconSize
                        colorInt = ContextCompat.getColor(this@MainActivity, R.color.secondary_30)
                    }
                )
                addState(
                    intArrayOf(),
                    IconicsDrawable(this@MainActivity, icon).apply {
                        sizeDp = iconSize
                        colorList = ColorStateList.valueOf(Color.TRANSPARENT)
                        drawContour = true
                        contourColorList =
                            ColorStateList.valueOf(
                                ContextCompat.getColor(this@MainActivity, R.color.text_secondary)
                            )
                        contourWidthPx = contourWidth
                    }
                )
            }
        }
        // Outline icons keep the default state light, while solid icons emphasize the active destination.
        bottom.menu.findItem(R.id.habitsFragment).icon =
            statefulIcon(FontAwesome.Icon.faw_tasks)
        bottom.menu.findItem(R.id.moodFragment).icon =
            statefulIcon(FontAwesome.Icon.faw_smile)
        bottom.menu.findItem(R.id.settingsFragment).icon =
            statefulIcon(FontAwesome.Icon.faw_cog)
        // Hide bottom bar on non-root destinations
        navController.addOnDestinationChangedListener { _, dest, _ ->
            // Secondary screens (calendar, chart) get more vertical space without the nav bar.
            bottom.visibility = when (dest.id) {
                R.id.habitsFragment, R.id.moodFragment, R.id.settingsFragment -> android.view.View.VISIBLE
                else -> android.view.View.GONE
            }
        }
        // Edge-to-edge layout needs window insets applied manually for status/navigation bars.
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}









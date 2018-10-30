package com.thkf.sentinelx.activities.prefActivity

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.preference.PreferenceFragment
import android.view.MenuItem
import com.thkf.sentinelx.R
import com.thkf.sentinelx.extensions.Prefs
import com.thkf.sentinelx.extensions.logI
import com.thkf.sentinelx.prefs.FAST_INTERVAL
import com.thkf.sentinelx.prefs.MAP_MODE
import com.thkf.sentinelx.prefs.MAP_TYPE


class PreferencesActivity: AppPrefActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        getSupportActionBar()?.setDisplayHomeAsUpEnabled(true)
        fragmentManager.beginTransaction()
                .replace(android.R.id.content, MainPrefsFragment())
                .commit()

    }

    class MainPrefsFragment : PreferenceFragment() {

        private lateinit var mContext: Context

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            addPreferencesFromResource(R.xml.pref_main)

            val updateTimePref = findPreference(getString(R.string.update_time_key))
            val mapType = findPreference(getString(R.string.map_type_key))
            val mapMode = findPreference(getString(R.string.map_mode_key))

            val defaultUpdateTime = Prefs(mContext).getString(FAST_INTERVAL, "15")
            val defaultMapType = Prefs(mContext).getString(MAP_TYPE, "1")
            val defaultMapMode = Prefs(mContext).getString(MAP_MODE, "1")

            val type = when(defaultMapType) {
                "1" -> "NORMAL"
                "2" -> "SATELLITE"
                "3" -> "TERRAIN"
                "4" -> "HYBRID"
                else -> "NONE"
            }

            val mode = when(defaultMapMode) {
                "1" -> "DRIVING"
                "2" -> "WALKING"
                else -> "NONE"
            }

            updateTimePref.summary = "Update location on every $defaultUpdateTime sec"
            mapType.summary = "Map type $type"
            mapMode.summary = "Map mode $mode"

            updateTimePref.setOnPreferenceChangeListener { preference, value ->
                preference.summary = "Update location on every $value sec"
                true
            }

            mapType.setOnPreferenceChangeListener { preference, value ->
                val t = when(value) {
                    "1" -> "NORMAL"
                    "2" -> "SATELLITE"
                    "3" -> "TERRAIN"
                    "4" -> "HYBRID"
                    else -> "NONE"
                }
                preference.summary = "Map type $t"
                true
            }

            mapMode.setOnPreferenceChangeListener { preference, value ->
                preference.summary = "Map mode ${if(value == "1") "DRIVING" else "WALKING"}"
                true
            }

        }

        override fun onResume() {
            logI("onResume")
            super.onResume()
        }

        override fun onAttach(context: Context) {
            super.onAttach(context)
            mContext = context
        }

        override fun onAttach(activity: Activity) {
            super.onAttach(activity)
            mContext = activity
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

}
package com.tmc.safecharge

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.support.design.widget.Snackbar
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.content.Intent
import android.content.IntentFilter
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.view.View
import com.tmc.safecharge.widgets.CircularSliderRange
import com.tmc.safecharge.widgets.ThumbEvent
import kotlinx.android.synthetic.main.main_content.*
import kotlinx.android.synthetic.main.battery_circle.*
import android.os.BatteryManager
import android.content.BroadcastReceiver




class MainActivity : AppCompatActivity() {
    private val mBatteryLevelReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {

            //int rawLevel = intent.getIntExtra("level", -1);
            val rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            //int scale = intent.getIntExtra("scale", -1);
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            var level = -1
            if (rawLevel >= 0 && scale > 0) {
                level = rawLevel * 100 / scale
            }
            bat_perc.text = level.toString() + "%"
        }

    }

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)


        battery_full.background = battery_full.background.apply { setColorFilter(ContextCompat.getColor(this@MainActivity, R.color.colorPrimary), PorterDuff.Mode.MULTIPLY) }
        battery_empty.background = battery_empty.background.apply { setColorFilter(ContextCompat.getColor(this@MainActivity, R.color.colorPrimary), PorterDuff.Mode.MULTIPLY) }
        tip_icon.background = tip_icon.background.apply { setColorFilter(ContextCompat.getColor(this@MainActivity, R.color.colorPrimary), PorterDuff.Mode.MULTIPLY) }
        top_icon.background = top_icon.background.apply { setColorFilter(ContextCompat.getColor(this@MainActivity, R.color.colorPrimary), PorterDuff.Mode.MULTIPLY) }
        phone_name.text = android.os.Build.MANUFACTURER + " " + android.os.Build.MODEL

        circular.onSliderRangeMovedListener = object : CircularSliderRange.OnSliderRangeMovedListener {
            @SuppressLint("SetTextI18n")
            override fun onStartSliderMoved(pos: Double) {
                cur_perc.text = (pos * 100).toInt().toString() + "%"
            }

            @SuppressLint("SetTextI18n")
            override fun onEndSliderMoved(pos: Double) {
                cur_perc.text = (pos * 100).toInt().toString() + "%"
            }

            override fun onStartSliderEvent(event: ThumbEvent) {
                cur_perc_layout.visibility = when(event) {
                    ThumbEvent.THUMB_PRESSED ->View.VISIBLE
                    ThumbEvent.THUMB_RELEASED ->View.INVISIBLE
                }
                circular.startThumbColor = ContextCompat.getColor(this@MainActivity, when(event) {
                    ThumbEvent.THUMB_PRESSED -> R.color.colorAccent
                    ThumbEvent.THUMB_RELEASED -> R.color.colorPrimary
                })
            }

            override fun onEndSliderEvent(event: ThumbEvent) {
                cur_perc_layout.visibility = when(event) {
                    ThumbEvent.THUMB_PRESSED -> View.VISIBLE
                    ThumbEvent.THUMB_RELEASED -> View.INVISIBLE
                }
                circular.endThumbColor = ContextCompat.getColor(this@MainActivity, when(event) {
                    ThumbEvent.THUMB_PRESSED -> R.color.colorAccent
                    ThumbEvent.THUMB_RELEASED -> R.color.colorPrimary
                })
            }
        }

        IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            this@MainActivity.registerReceiver(mBatteryLevelReceiver, ifilter)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    public override fun onResume() {
        super.onResume()
        // Check to see that the Activity started due to an Android Beam
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            processIntent(intent)
        }
    }

    public override fun onNewIntent(intent: Intent) {
        // onResume gets called after this to handle the intent
        setIntent(intent)
    }

    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    fun processIntent(intent: Intent) {
        val rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES)
        // only one message sent during the beam
        val msg = rawMsgs[0] as NdefMessage
        // record 0 contains the MIME type, record 1 is the AAR, if present
//        v.text = String(msg.records[0].payload)
    }


}

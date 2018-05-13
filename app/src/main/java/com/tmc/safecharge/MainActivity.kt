package com.tmc.safecharge

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_main.*
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.content.Intent
import android.content.IntentFilter
import android.graphics.PorterDuff
import android.support.v4.content.ContextCompat
import android.view.View
import com.tmc.safecharge.widgets.CircularSliderRange
import com.tmc.safecharge.widgets.ThumbEvent
import kotlinx.android.synthetic.main.main_content.*
import kotlinx.android.synthetic.main.battery_circle.*
import android.os.BatteryManager
import android.content.BroadcastReceiver
import android.graphics.drawable.Drawable
import com.tmc.safecharge.services.BluetoothConnectionService
import android.os.Build
import android.util.Log
import com.tmc.safecharge.services.ChargerState
import java.util.*


class MainActivity : AppCompatActivity() {
    enum class RequestCodes {
        REQUEST_ENABLE_BT
    }
//    var level = 0
//    var min = 0
//    var max = 0
//    var state = BatteryManager.BATTERY_STATUS_UNKNOWN

    private val mBatteryLevelReceiver = object : BroadcastReceiver() {
        @SuppressLint("SetTextI18n")
        override fun onReceive(context: Context, intent: Intent) {

            //int rawLevel = intent.getIntExtra("level", -1);
            val rawLevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
            //int scale = intent.getIntExtra("scale", -1);
            val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
            chargerState.phoneChargingState = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1)
            chargerState.level = -1
            if (rawLevel >= 0 && scale > 0) {
                chargerState.level = rawLevel * 100 / scale
            }
            updateBatteryDependent()
        }

    }

    private val mPairingRequestReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothDevice.ACTION_PAIRING_REQUEST) {
                try {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val pin = intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY", 1234)
                    //the pin in case you need to accept for an specific pin
                    Log.d(TAG, "Start Auto Pairing. PIN = " + intent.getIntExtra("android.bluetooth.device.extra.PAIRING_KEY", 1234))
                    val pinBytes: ByteArray
                    pinBytes = ("" + pin).toByteArray(charset("UTF-8"))
                    device.setPin(pinBytes)
                    //setPairing confirmation if neeeded
                    device.setPairingConfirmation(true)
                } catch (e: Exception) {
                    Log.e(TAG, "Error occurs when trying to auto pair")
                    e.printStackTrace()
                }

            }
        }
    }

    lateinit var bluetoothAdapter: BluetoothAdapter
    lateinit var chargerState: ChargerState

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
                chargerState.min  = (pos * 100).toInt()

            }

            @SuppressLint("SetTextI18n")
            override fun onEndSliderMoved(pos: Double) {
                cur_perc.text = (pos * 100).toInt().toString() + "%"
                chargerState.max = (pos * 100).toInt()

            }

            override fun onStartSliderEvent(event: ThumbEvent) {
                cur_perc_layout.visibility = when(event) {
                    ThumbEvent.THUMB_PRESSED ->View.VISIBLE
                    ThumbEvent.THUMB_RELEASED -> View.INVISIBLE.apply {
                        updateBatteryDependent()
                    }
                }
                circular.startThumbColor = ContextCompat.getColor(this@MainActivity, when(event) {
                    ThumbEvent.THUMB_PRESSED -> R.color.colorAccent
                    ThumbEvent.THUMB_RELEASED -> R.color.colorPrimary
                })
            }

            override fun onEndSliderEvent(event: ThumbEvent) {

                cur_perc_layout.visibility = when(event) {
                    ThumbEvent.THUMB_PRESSED -> View.VISIBLE
                    ThumbEvent.THUMB_RELEASED -> View.INVISIBLE.apply {
                        updateBatteryDependent()
                    }
                }
                circular.endThumbColor = ContextCompat.getColor(this@MainActivity, when(event) {
                    ThumbEvent.THUMB_PRESSED -> R.color.colorAccent
                    ThumbEvent.THUMB_RELEASED -> R.color.colorPrimary
                })
            }
        }

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent,RequestCodes.REQUEST_ENABLE_BT.ordinal)
        }

        chargerState = ChargerState(BluetoothConnectionService())
        chargerState.min = (circular.min() * 100).toInt()
        chargerState.max = (circular.max() * 100).toInt()


        val dbHelper = ConnectedDevicesDbHelper(this)
        println(dbHelper.getEntries())

        IntentFilter(Intent.ACTION_BATTERY_CHANGED).let { ifilter ->
            this@MainActivity.registerReceiver(mBatteryLevelReceiver, ifilter)
        }

        IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST).let { ifilter ->
            this@MainActivity.registerReceiver(mPairingRequestReceiver, ifilter)
        }
    }

    @SuppressLint("SetTextI18n")
    fun updateBatteryDependent() {
        bat_perc.text = chargerState.level.toString() + "%"
        chargerState.write("updateDisplay")
        chargerState.updatePower()
        when(chargerState.phoneChargingState) {
            BatteryManager.BATTERY_STATUS_CHARGING -> when {
                chargerState.isOn() -> top_icon.background.apply { setColorFilter(ContextCompat.getColor(this@MainActivity, R.color.colorPrimary), PorterDuff.Mode.MULTIPLY) }
                else -> top_icon.background.apply { setColorFilter(ContextCompat.getColor(this@MainActivity, R.color.colorAccent), PorterDuff.Mode.MULTIPLY) }
            }
            else -> top_icon.background.apply { setColorFilter(ContextCompat.getColor(this@MainActivity, R.color.disabled), PorterDuff.Mode.MULTIPLY) }
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

    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val reqCode = RequestCodes.values()[requestCode]
        super.onActivityResult(requestCode, resultCode, data)
    }

    fun connectDevice(mac: String) {
        val device = bluetoothAdapter.getRemoteDevice(mac)
        device.createBond()
        chargerState.cancel()
        chargerState.connect(device)
        updateBatteryDependent()
        Log.i(TAG, "Device connect: ${device.createBond()}")
    }

    @SuppressLint("ObsoleteSdkInt")
    private fun checkBTPermissions() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            var permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION")
            permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION")
            if (permissionCheck != 0) {

                this.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION), 1001) //Any number
            }
        } else {
            Log.d(TAG, "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.")
        }
    }

    /**
     * Parses the NDEF Message from the intent and prints to the TextView
     */
    fun processIntent(intent: Intent) {
        val rawMsgs = intent.getParcelableArrayExtra(
                NfcAdapter.EXTRA_NDEF_MESSAGES)
        // only one message sent during the beam
        val msg = rawMsgs[0] as NdefMessage
        val mac = String(msg.records[0].payload)
        connectDevice(mac)
        // record 0 contains the MIME type, record 1 is the AAR, if present
//        v.text = String(msg.records[0].payload)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBatteryLevelReceiver)
        unregisterReceiver(mPairingRequestReceiver)
    }

    companion object {
        private const val TAG = "MainActivity"
    }

}

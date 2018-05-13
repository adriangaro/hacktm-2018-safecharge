package com.tmc.safecharge.services

import android.bluetooth.BluetoothDevice
import android.os.BatteryManager

class ChargerState(val bs: BluetoothConnectionService) {
    var power = false
        private set
    var min = 0
    var level = 0
    var max = 0
    var phoneChargingState: Int = BatteryManager.BATTERY_STATUS_UNKNOWN

    fun isOn() = power
    fun isOff() = !power

    fun write(s: String) {
        when(s) {
            "turnOn" -> {
                power = true
                bs.write("turnOn".toByteArray())
            }
            "turnOff" -> {
                power = false
                bs.write("turnOff".toByteArray())
            }
            "updateDisplay" -> {
                bs.write("*$level,${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}".toByteArray())
            }
        }
    }

    fun updatePower() {
        if (level > max) {
            write("turnOff")
            return
        }
        if (level < min)
            write("turnOn")
    }

    fun cancel() = bs.cancel()
    fun connect(d: BluetoothDevice) = bs.connect(d)
}
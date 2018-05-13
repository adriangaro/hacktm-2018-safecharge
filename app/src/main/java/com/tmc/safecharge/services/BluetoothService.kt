package com.tmc.safecharge.services

import android.bluetooth.BluetoothSocket
import android.util.Log
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothServerSocket
import android.bluetooth.BluetoothAdapter
import android.content.Context
import java.nio.charset.Charset
import java.util.*


class BluetoothConnectionService {
    private var mConnectedThread: ConnectedThread? = null



    /**
     * Finally the ConnectedThread which is responsible for maintaining the BTConnection, Sending the data, and
     * receiving incoming data through input/output streams respectively.
     */
    private inner class ConnectedThread(private val mmSocket: BluetoothSocket) : Thread() {
        private val mmInStream: InputStream?
        private val mmOutStream: OutputStream?

        init {
            Log.d(TAG, "ConnectedThread: Starting.")
            var tmpIn: InputStream? = null
            var tmpOut: OutputStream? = null

            //dismiss the progressdialog when connection is established
            try {
            } catch (e: NullPointerException) {
                e.printStackTrace()
            }
            println(mmSocket)

            try {
                tmpIn = mmSocket.inputStream
                tmpOut = mmSocket.outputStream
            } catch (e: IOException) {
                e.printStackTrace()
            }

            mmInStream = tmpIn
            mmOutStream = tmpOut
        }

        override fun run() {
            val buffer = ByteArray(1024)  // buffer store for the stream

            var bytes: Int // bytes returned from read()

            // Keep listening to the InputStream until an exception occurs
            while (true) {
                // Read from the InputStream
                try {
                    bytes = mmSocket.inputStream.read(buffer)
                    val incomingMessage = String(buffer, 0, bytes)
                    Log.d(TAG, "InputStream: $incomingMessage")
                } catch (e: IOException) {
                    Log.e(TAG, "write: Error reading Input Stream. " + e.message)
                    break
                }

            }
        }

        //Call this from the main activity to send data to the remote device
        fun write(bytes: ByteArray) {
            val text = String(bytes, Charset.defaultCharset())
            Log.d(TAG, "write: Writing to outputstream: $text")
            try {
                mmSocket.outputStream.write(bytes)
            } catch (e: Exception) {
                Log.e(TAG, "write: Error writing to output stream. " + e.message)
            }

        }

        /* Call this from the main activity to shutdown the connection */
        fun cancel() {
            try {
                mmSocket.close()
            } catch (e: IOException) {
            }

        }
    }

    fun connect(mmDevice: BluetoothDevice) {
        if(!connected) {
            Log.d(TAG, "connect: Starting.")
            val sock = mmDevice.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"))
            sock.connect()
            // Start the thread to manage the connection and perform transmissions
            mConnectedThread = ConnectedThread(sock)
            mConnectedThread!!.start()
        }
    }

    val connected: Boolean
        get() = mConnectedThread != null


    /**
     * Write to the ConnectedThread in an unsynchronized manner
     *
     * @param out The bytes to write
     * @see ConnectedThread.write
     */
    fun write(out: ByteArray) {
        // Synchronize a copy of the ConnectedThread
        Log.d(TAG, "write: Write Called.")
        //perform the write
        mConnectedThread?.write(out)
    }

    fun cancel() {
        mConnectedThread?.cancel()
        mConnectedThread = null
    }
    companion object {
        private val TAG = "BluetoothService"

        private val appName = "SAFECHARGE"

        private val MY_UUID_INSECURE = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66")
    }

}

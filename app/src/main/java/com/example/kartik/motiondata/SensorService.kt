package com.example.kartik.motiondata

import android.app.Service
import android.content.Context
import android.content.Intent
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import android.app.Activity




class SensorService: Service() {
    private var myBinder = MyLocalBinder()
    var status :Int= 0
    var username : String? = null
    var bigRecordNumber = 0
    var recordNumber = 0
    var firstTIme = true
    val SAMPLING_PERIOD = 3333
    var data = arrayOf(0f,0f,0f,0f,0f,0f,0f,0f,0f)
    var activity: Callbacks? = null
    val t = Timer()
    var sensorManager : SensorManager? = null
    var accelerometerListener = object :SensorEventListener{
        override fun onSensorChanged(event: SensorEvent?) {
            data[0] = event!!.values[0]
            data[1] = event.values[1]
            data[2] = event.values[2]
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }
    var gyroscopeListener = object :SensorEventListener{
        override fun onSensorChanged(event: SensorEvent?) {
            data[3] = event!!.values[0]
            data[4] = event.values[1]
            data[5] = event.values[2]
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }
    var linearAccelerometerListener = object :SensorEventListener{
        override fun onSensorChanged(event: SensorEvent?) {
            data[6] = event!!.values[0]
            data[7] = event.values[1]
            data[8] = event.values[2]
        }
        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}
    }
    var handler = Handler()
    var serviceRunnable: Runnable = object : Runnable {
        override fun run() {
            activity?.updateClient(bigRecordNumber, status)
            handler.postDelayed(this, 1000)
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        setupSensorManager()
        return myBinder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        status = intent?.getStringExtra("status")?.toInt()!!
        username = intent?.getStringExtra("name")

        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager?

        t.schedule(object : TimerTask() {
            override fun run() {
                var dataString = ""
                for (i in 0..8)
                    dataString = dataString + java.lang.Float.toString(data[i])+" "
                //Log.d("run: ", dataString)
                recordNumber++

                if (recordNumber == 128 && firstTIme) {
                    //Log.d("run: status", Integer.toString(status))
                    recordNumber = 0
                    bigRecordNumber++
                    firstTIme = false
                    dataString += (Integer.toString(status!!) + " ")
                }

                if (recordNumber == 64 && !firstTIme) {
                    //Log.d("run: status", Integer.toString(status))
                    recordNumber = 0
                    bigRecordNumber++
                    dataString += (Integer.toString(status!!) + " ")
                }
                dataString += "\n"
                writeIntoFile(dataString)
            }
        }, 0, 20)

        return super.onStartCommand(intent, flags, startId)
    }

    private fun setupSensorManager() {
        sensorManager?.registerListener(accelerometerListener, sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SAMPLING_PERIOD)
        sensorManager?.registerListener(gyroscopeListener, sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SAMPLING_PERIOD)
        sensorManager?.registerListener(linearAccelerometerListener, sensorManager?.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SAMPLING_PERIOD)
    }

    private fun writeIntoFile(newData: String) {
        val path = Environment.getExternalStorageDirectory()
        val filename = username + "_data.txt"
        path.mkdirs()
        val file = File(path, filename)
        try {
            val fos = FileOutputStream(file, true)
            fos.write(newData.toByteArray())
            fos.close()
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    inner class MyLocalBinder : Binder(){
        fun getService() : SensorService{
            return this@SensorService
        }
    }

    fun updateStatus(status: Int){
        this.status = status
    }

    fun registerClient(activity: Activity) {
        this.activity = activity as Callbacks
    }
    interface Callbacks {
        fun updateClient(bigRecordNumber:Int, status:Int?)
    }
}
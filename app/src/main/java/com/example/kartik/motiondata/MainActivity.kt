package com.example.kartik.motiondata

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.*
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity(), SensorService.Callbacks{


    var username :String? = null
    var connectionStatus = true
    var data = arrayOf(0f,0f,0f,0f,0f,0f,0f,0f,0f)
    private val SAMPLING_PERIOD = 3333
    var status :Int = 0
    var sensorManager :SensorManager? = null
    var accelerometerListener : SensorEventListener? = null
    var gyroscopeListener : SensorEventListener? = null
    var linearAccelerometerListener : SensorEventListener? = null
    var sensorService = SensorService()


    var serviceConnection = object : ServiceConnection{
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            sensorService.registerClient(this@MainActivity)
            connectionStatus = true
            statusTextView.text = "Connected to Service"
            statusTextView.setTextColor(Color.GREEN)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            connectionStatus = false
            statusTextView.text = "Disonnected to Service"
            statusTextView.setTextColor(Color.RED)
        }

    }
    var serviceIntent :Intent? = null


    var STATUS_SITTING = 0
    var STATUS_LAYING = 1
    var STATUS_STANDING = 2
    var STATUS_WALKING = 3
    var STATUS_RUNNING = 4
    var STATUS_UPSTAIRS = 5
    var STATUS_DOWNSTAIRS = 6
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val intent = getIntent()
        username = intent.getStringExtra("name")
        status = intent.getStringExtra("state").toInt()


        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager?
        setTitle("$username : Data")
        setupSensors()
        setAllRed()
        setGreen()
        startSensorService()
    }

    private fun startSensorService() {
        serviceIntent = Intent(this@MainActivity, SensorService::class.java)
        serviceIntent?.putExtra("name", username)
        serviceIntent?.putExtra("status", status.toString())

        startService(serviceIntent)
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun stopSensorService(){
        unbindService(serviceConnection)
        stopService(serviceIntent)
    }


    private fun setupSensors() {

        accelerometerListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                data[0] = event!!.values[0]
                data[1] = event.values[1]
                data[2] = event.values[2]
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                Toast.makeText(applicationContext, "Accelerometer Accuracy Changed", Toast.LENGTH_LONG).show()
            }
        }
        gyroscopeListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                data[3] = event!!.values[0]
                data[4] = event.values[1]
                data[5] = event.values[2]
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                Toast.makeText(applicationContext, "Gyroscope Accuracy Changed", Toast.LENGTH_LONG).show()
            }
        }
        linearAccelerometerListener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent?) {
                data[6] = event!!.values[0]
                data[7] = event.values[1]
                data[8] = event.values[2]
            }

            override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
                Toast.makeText(applicationContext, "Linear Accelerometer Accuracy Changed", Toast.LENGTH_LONG).show()
            }
        }


        sensorManager?.registerListener(accelerometerListener, sensorManager?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SAMPLING_PERIOD)
        sensorManager?.registerListener(gyroscopeListener, sensorManager?.getDefaultSensor(Sensor.TYPE_GYROSCOPE), SAMPLING_PERIOD)
        sensorManager?.registerListener(linearAccelerometerListener, sensorManager?.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION), SAMPLING_PERIOD)
    }

    private fun setAllRed(){
        standingButton.setBackgroundColor(Color.RED)
        sittingButton.setBackgroundColor(Color.RED)
        layingButton.setBackgroundColor(Color.RED)
        walkingButton.setBackgroundColor(Color.RED)
        runningButton.setBackgroundColor(Color.RED)
        upstairsButton.setBackgroundColor(Color.RED)
        downstairsButton.setBackgroundColor(Color.RED)

    }

    private fun setGreen() {
        when(status){
            STATUS_SITTING -> sittingButton.setBackgroundColor(Color.GREEN)
            STATUS_LAYING -> layingButton.setBackgroundColor(Color.GREEN)
            STATUS_STANDING -> standingButton.setBackgroundColor(Color.GREEN)
            STATUS_WALKING -> walkingButton.setBackgroundColor(Color.GREEN)
            STATUS_RUNNING -> runningButton.setBackgroundColor(Color.GREEN)
            STATUS_UPSTAIRS -> upstairsButton.setBackgroundColor(Color.GREEN)
            STATUS_DOWNSTAIRS -> downstairsButton.setBackgroundColor(Color.GREEN)
        }
    }

    override fun updateClient(bigRecordNumber:Int, status:Int?) {
        bigRecordTextView.text = bigRecordNumber.toString()
        when(status){
            STATUS_SITTING -> latestRecordTextView.text = "Sitting"
            STATUS_STANDING -> latestRecordTextView.text = "Standing"
            STATUS_LAYING -> latestRecordTextView.text = "Laying"
            STATUS_WALKING -> latestRecordTextView.text = "Walking"
            STATUS_RUNNING -> latestRecordTextView.text = "Running"
            STATUS_UPSTAIRS -> latestRecordTextView.text = "Upstairs"
            STATUS_DOWNSTAIRS-> latestRecordTextView.text = "Downstairs"
        }
    }

    //button functions
    fun connectionButton(view:View){
        if(connectionStatus){
            stopSensorService()
            connectButton.text = "Start service"
            connectButton.setBackgroundColor(Color.GREEN)
        }else{
            startSensorService()
            connectButton.text = "Stop service"
            connectButton.setBackgroundColor(Color.RED)
        }
    }



    fun sitting(view: View){
        status = STATUS_SITTING
        sensorService.updateStatus(status)

        setAllRed()
        setGreen()
    }
    fun standing(view: View){
        status = STATUS_STANDING
        sensorService.updateStatus(status)
        setAllRed()
        setGreen()
    }
    fun laying(view: View){
        status = STATUS_LAYING
        sensorService.updateStatus(status)
        setAllRed()
        setGreen()
    }
    fun walking(view: View){
        status = STATUS_WALKING
        sensorService.updateStatus(status)
        setAllRed()
        setGreen()
    }
    fun running(view: View){
        status = STATUS_RUNNING
        sensorService.updateStatus(status)
        setAllRed()
        setGreen()
    }
    fun upstairs(view: View){
        status = STATUS_UPSTAIRS
        sensorService.updateStatus(status)
        setAllRed()
        setGreen()
    }
    fun downstairs(view: View){
        status = STATUS_DOWNSTAIRS
        sensorService.updateStatus(status)
        setAllRed()
        setGreen()
    }

}

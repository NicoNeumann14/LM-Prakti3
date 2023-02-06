package com.lm22_23.prakti3

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import kotlin.math.pow

class MainActivity : AppCompatActivity(), OnMapReadyCallback, SensorEventListener {


    private lateinit var mapFragment: SupportMapFragment
    private lateinit var googleMap: GoogleMap

    private lateinit var btnLocation: Button
    private lateinit var btnSave: Button

    private lateinit var fusedProvider: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

    private lateinit var locationManager: LocationManager
    private lateinit var locationListener:android.location.LocationListener

    private var posiLogWithTime = JSONArray()
    private var isStarted = false

    private lateinit var waypoints: ArrayList<LatLng>

    private var wayTime = arrayListOf<Long>()
    private var waypointsWithTime = JSONArray()
    private var postCounter = 0

    private var aktuellePosi = Location("aktuellePosi")

    //Sensoren
    private lateinit var sensorManager: SensorManager
    private lateinit var sensorBeschleunigung: Sensor
    private var inBewegung = false
    private var unterMaxGesc = false

    private var fragGPS = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()

        val sharedPref = getSharedPreferences(SETTINGS_PREFERENCES, Context.MODE_PRIVATE)
        val route1 = sharedPref.getBoolean(ROUTE_1, false)
        val route2 = sharedPref.getBoolean(ROUTE_2, false)
//        val route3 = sharedPref.getBoolean(ROUTE_3, false)

        waypoints = if(route1)
            waypoints_route_1
        else if(route2)
            waypoints_route_2
        else
            waypoints_route_3

        if(ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ), 0)
            }
        }else{
            when (intent.getSerializableExtra("ReportingStrategy")) {
                ReportingStrategy.PERIODIC -> {
                    val value = sharedPref.getInt(PERIOD_MS, 1000)
                    getLocationPeriodisch(value.toLong())
                }
                ReportingStrategy.DISTANCE -> {
                    val value = sharedPref.getInt(DISTANCE_M, 50)
                    distanzbasiertesReporting(value.toDouble())
                }
                ReportingStrategy.ENERGY_EFFICIENT -> {
                    // TODO: getLocationEnergyEfficient
                }
                ReportingStrategy.STILL -> {
                    val value = sharedPref.getInt(DISTANCE_M, 50)
                    stillstandstrategie()
                    distanzbasiertesReporting(value.toDouble())
                }
                else -> {
                    println("Keine Reporting Strategie angegeben!")

                }
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()

        when (intent.getSerializableExtra("ReportingStrategy")) {
            ReportingStrategy.PERIODIC -> {
                fusedProvider.removeLocationUpdates(locationCallback)
            }
            ReportingStrategy.DISTANCE -> {
                locationManager.removeUpdates(locationListener)
            }
            ReportingStrategy.ENERGY_EFFICIENT -> {
                locationManager.removeUpdates(locationListener)
            }
            ReportingStrategy.STILL -> {
                locationManager.removeUpdates(locationListener)
                sensorManager.unregisterListener(this, sensorBeschleunigung)
            }

        }
    }


    private fun initView() {
        val actionbar = supportActionBar
        when (intent.getSerializableExtra("ReportingStrategy")) {
            ReportingStrategy.PERIODIC -> actionbar!!.title = "Periodisches-Reporting"
            ReportingStrategy.DISTANCE -> actionbar!!.title = "Distanz-basiertes-Reporting"
            ReportingStrategy.ENERGY_EFFICIENT -> actionbar!!.title = "Energie-effizientes-Reporting"
            ReportingStrategy.STILL -> actionbar!!.title = "Stillstand-gewahres-Reporting"
            else -> actionbar!!.title = "Keine Reporting Strategie angegeben!"
        }
        actionbar.setDisplayHomeAsUpEnabled(true)


        btnLocation = findViewById(R.id.btnLocation)
        btnSave = findViewById(R.id.btnSpeichern)

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)


        btnSave.setOnClickListener {

            if(wayTime.size >= waypoints.size){

                when (intent.getSerializableExtra("ReportingStrategy")) {
                    ReportingStrategy.PERIODIC -> {
                        fusedProvider.removeLocationUpdates(locationCallback)
                    }
                    ReportingStrategy.DISTANCE -> {
                        locationManager.removeUpdates(locationListener)
                    }
                    ReportingStrategy.ENERGY_EFFICIENT -> {
                        locationManager.removeUpdates(locationListener)
                    }
                    ReportingStrategy.STILL -> {
                        locationManager.removeUpdates(locationListener)
                        sensorManager.unregisterListener(this, sensorBeschleunigung)
                    }

                }

                isStarted = false

                for (i in 0 until waypoints.size) {
                    val jsonLog = JSONObject()
                    jsonLog.put("Latitude", waypoints[i].latitude)
                    jsonLog.put("Longitude", waypoints[i].longitude)
                    jsonLog.put("Timestamp", wayTime[i])
                    waypointsWithTime.put(jsonLog)
                }

                httpPost(posiLogWithTime,"${actionbar.title}#Posi+Time")
                Toast.makeText(applicationContext, "http-POST: Posi+Time", Toast.LENGTH_LONG).show()

                val interPolartionsArray = alleKoorLinearInterpolieren(waypointsWithTime)
                httpPost(interPolartionsArray,"${actionbar.title}#InterpolPosi")
                Toast.makeText(applicationContext, "http-POST: InterpolPosi", Toast.LENGTH_LONG).show()

                putCircle(interPolartionsArray, Color.BLUE)
                putCircle(posiLogWithTime, Color.GREEN)

            }else{
                Toast.makeText(this, "Zuwenig Time for Waypoints", Toast.LENGTH_SHORT).show()
            }

        }

    }

    //Aufgabe 1a
    @SuppressLint("MissingPermission")
    private fun getLocationPeriodisch(deltaTime:Long){
        postCounter = 0
        fusedProvider = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest().setFastestInterval(deltaTime).setInterval(deltaTime).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)


        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)

                if(isStarted){
                    val jsonLog = JSONObject()
                    jsonLog.put("Latitude", p0?.lastLocation?.latitude)
                    jsonLog.put("Longitude", p0?.lastLocation?.longitude)
                    jsonLog.put("Timestamp", p0?.lastLocation?.time)
                    posiLogWithTime.put(jsonLog)

                    //der http Post nach jedem gps fix
                    val jsonArrayPost = JSONArray()
                    jsonArrayPost.put(jsonLog)
                    postCounter++
                    httpPost(jsonArrayPost,"Periodisch-$postCounter")
                    //Toast.makeText(applicationContext, "http-POST gesendet", Toast.LENGTH_LONG).show()
                }


                btnLocation.setOnClickListener {
                    wayTime.add(p0?.lastLocation?.time!!)
                    if(!isStarted) {
                        isStarted = true
                    }
                }
            }
        }

        fusedProvider.requestLocationUpdates(locationRequest,locationCallback,null)


    }

    //Aufgabe 1b
    //todo: localMangaer
    @SuppressLint("MissingPermission")
    private fun distanzbasiertesReporting(configurableDistanceThreshold: Double) {

        postCounter = 0
        val moeglicheNeuePosi = Location("moeglicheNeuePosi")
        fusedProvider = LocationServices.getFusedLocationProviderClient(this)


        locationRequest = LocationRequest().setFastestInterval(4000).setInterval(4000)
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        aktuellePosi.latitude = 0.0
        aktuellePosi.longitude = 0.0

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)

                if ((aktuellePosi.latitude == 0.0) && (aktuellePosi.longitude == 0.0)) {
                    aktuellePosi.latitude = p0?.lastLocation?.latitude!!
                    aktuellePosi.longitude = p0.lastLocation?.longitude!!
                    aktuellePosi.time = p0.lastLocation?.time!!

                    val aktPosiLatLng = LatLng(aktuellePosi.latitude, aktuellePosi.longitude)
                    googleMap.addCircle(
                        CircleOptions().center(aktPosiLatLng).radius(2.0).strokeColor(Color.GREEN)
                            .fillColor(Color.GREEN)
                    )
                    googleMap.addCircle(
                        CircleOptions().center(aktPosiLatLng).radius(configurableDistanceThreshold).strokeColor(Color.RED)
                    )
                    val cameraUpdate = CameraUpdateFactory.newLatLngZoom(aktPosiLatLng, 18f)
                    googleMap.animateCamera(cameraUpdate)
                }

                if (isStarted) {
                    // Flags für 1c und 1d, 1b always true
                    when (intent.getSerializableExtra("ReportingStrategy")) {
                        ReportingStrategy.DISTANCE -> {
                            fragGPS = true
                        }
                        ReportingStrategy.ENERGY_EFFICIENT -> {
                            fragGPS = unterMaxGesc
                        }
                        ReportingStrategy.STILL -> {
                            fragGPS = inBewegung
                        }
                    }

                    if(fragGPS){
                        moeglicheNeuePosi.latitude = p0?.lastLocation?.latitude!!
                        moeglicheNeuePosi.longitude = p0.lastLocation?.longitude!!
                        moeglicheNeuePosi.time = p0.lastLocation?.time!!

                        val moegPosiLatLng = LatLng(moeglicheNeuePosi.latitude, moeglicheNeuePosi.longitude)
                        googleMap.addCircle(
                            CircleOptions().center(moegPosiLatLng).radius(2.0).strokeColor(Color.BLUE)
                                .fillColor(Color.BLUE)
                        )

                        if (aktuellePosi.distanceTo(moeglicheNeuePosi) >= configurableDistanceThreshold) {
                            aktuellePosi.latitude = moeglicheNeuePosi.latitude
                            aktuellePosi.longitude = moeglicheNeuePosi.longitude
                            aktuellePosi.time = moeglicheNeuePosi.time

                            val aktPosiLatLng = LatLng(aktuellePosi.latitude, aktuellePosi.longitude)
                            googleMap.addCircle(
                                CircleOptions().center(aktPosiLatLng).radius(2.0).strokeColor(Color.GREEN)
                                    .fillColor(Color.GREEN)
                            )
                            googleMap.addCircle(
                                CircleOptions().center(aktPosiLatLng).radius(configurableDistanceThreshold).strokeColor(Color.RED)
                            )

                            val jsonLog = JSONObject()
                            jsonLog.put("Latitude", aktuellePosi.latitude)
                            jsonLog.put("Longitude", aktuellePosi.longitude)
                            jsonLog.put("Timestamp", aktuellePosi.time)
                            posiLogWithTime.put(jsonLog)

                            //http-POST nach jedem neuen passenden GPS-Fix
                            val jsonPositionPOST = JSONArray()
                            jsonPositionPOST.put(jsonLog)
                            postCounter++
                            httpPost(jsonPositionPOST, "distanzbasiert-$postCounter")
                            //Toast.makeText(applicationContext, "http-POST gesendet", Toast.LENGTH_LONG).show()

                        }
                        else {
                            Toast.makeText(applicationContext, "Distanz zu gering", Toast.LENGTH_SHORT).show()
                        }
                    }
                }

                btnLocation.setOnClickListener {
                    wayTime.add(p0?.lastLocation?.time!!)
                    if (!isStarted) {
                        isStarted = true
                    }
                }
            }
        }

        fusedProvider.requestLocationUpdates(locationRequest, locationCallback, null)
    }

    //Aufgabe 1d zur erweiterung von 1b
    private fun stillstandstrategie(){
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        sensorBeschleunigung = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        if(sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION) != null){
            sensorManager.registerListener(this,sensorBeschleunigung,SensorManager.SENSOR_DELAY_FASTEST)
        }
    }

    override fun onSensorChanged(p0: SensorEvent?) {
        if(p0!!.sensor.type == Sensor.TYPE_LINEAR_ACCELERATION) {
            val x = p0.values[0]
            val y = p0.values[1]
            val z = p0.values[2]
            val speed = kotlin.math.sqrt(
                (x.toDouble().pow(2.0) + y.toDouble().pow(2.0) + z.toDouble().pow(2.0))
            )
            Log.d("Speed", "onSensorChanged: $speed")
            inBewegung = speed >= 1
        }

    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {

    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        //Hat die APP zum absturtz gebracht, nur in den Methoden zu den Aufgaben aufrufen.. siehe zeile 323
        //fusedProvider.requestLocationUpdates(locationRequest, locationCallback, null)

        var i = 1
        waypoints.forEach{
            googleMap.addMarker(MarkerOptions().position(it).title("Pos$i"))
            i++
        }

        googleMap.addPolyline(
            PolylineOptions().addAll(waypoints).color(Color.RED)
        )

        //Das ist dafür da um am anfang auf die startpunkte unserer Route zu zoomen.
        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(waypoints[0], 18f)
        googleMap.animateCamera(cameraUpdate)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        val sharedPref = getSharedPreferences(SETTINGS_PREFERENCES, Context.MODE_PRIVATE)

        if(requestCode == 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            when (intent.getSerializableExtra("ReportingStrategy")) {
                ReportingStrategy.PERIODIC -> {
                    val value = sharedPref.getInt(PERIOD_MS, 1000)
                    getLocationPeriodisch(value.toLong())
                }
                ReportingStrategy.DISTANCE -> {
                    val value = sharedPref.getInt(DISTANCE_M, 50)
                    distanzbasiertesReporting(value.toDouble())
                }
                ReportingStrategy.ENERGY_EFFICIENT -> {
                    // TODO: getLocationEnergyEfficient
                }
                ReportingStrategy.STILL -> {
                    val value = sharedPref.getInt(DISTANCE_M, 50)
                    stillstandstrategie()
                    distanzbasiertesReporting(value.toDouble())
                }
                else -> {
                    println("Keine Reporting Strategie angegeben!")

                }
            }
        }

    }

    private fun httpPost(text: JSONArray, api:String){
        val client = OkHttpClient()
        val postBody = text.toString()
        val request = Request.Builder().url("https://lm2022.free.beeceptor.com/$api")
            .post(postBody.toRequestBody()).build()

        client.newCall(request).enqueue(object: Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                if (!response.isSuccessful) throw IOException("Fehler: $response")
                Log.e("Res", response.body!!.string())
            }
        })
    }

    private fun putCircle(samples: JSONArray, color:Int){
        for(i in 0 until samples.length()){
            val positionLatLng = LatLng(
                (samples.getJSONObject(i).get("Latitude") as Double),
                (samples.getJSONObject(i).get("Longitude") as Double)
            )
            googleMap.addCircle(CircleOptions().center(positionLatLng).radius(1.0).strokeColor(color).fillColor(color))
        }
    }

    private fun alleKoorLinearInterpolieren(jsonArray: JSONArray): JSONArray {

        val jsonArrayIp = JSONArray()

        for(i in 0 until jsonArray.length()-1) {

            val dLongitude = (jsonArray.getJSONObject(i+1).get("Longitude") as Double) - (jsonArray.getJSONObject(i).get("Longitude") as Double)
            val dLatitude = (jsonArray.getJSONObject(i+1).get("Latitude") as Double) - (jsonArray.getJSONObject(i).get("Latitude") as Double)
            val schrittweiteMillisekunden = 2500.toLong()
            val t21 = (jsonArray.getJSONObject(i+1).get("Timestamp") as Long) - (jsonArray.getJSONObject(i).get("Timestamp") as Long)
            var t = (jsonArray.getJSONObject(i).get("Timestamp") as Long) + schrittweiteMillisekunden

            while (t < (jsonArray.getJSONObject(i+1).get("Timestamp") as Long)) {

                val t1 = (jsonArray.getJSONObject(i).get("Timestamp") as Long)
                val dt = (t - t1) / t21.toDouble()

                val neueLongi =
                    (jsonArray.getJSONObject(i).get("Longitude") as Double) + dLongitude * dt
                val neueLati =
                    (jsonArray.getJSONObject(i).get("Latitude") as Double) + dLatitude * dt

                val jsonObject = JSONObject()
                jsonObject.put("Latitude", neueLati)
                jsonObject.put("Longitude", neueLongi)
                jsonObject.put("Timestamp", t)
                jsonArrayIp.put(jsonObject)

                t += schrittweiteMillisekunden
            }
        }

        return jsonArrayIp
    }

    //Nur noch Vorlage
    @SuppressLint("MissingPermission")
    private fun getLocation(){

        fusedProvider = LocationServices.getFusedLocationProviderClient(this)
        //locationRequest = LocationRequest().setFastestInterval(2000).setInterval(3000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest = LocationRequest().setFastestInterval(2000).setInterval(3000).setPriority(
            LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        )
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                super.onLocationResult(p0)

                if(isStarted){
                    val jsonLog = JSONObject()
                    jsonLog.put("Latitude", p0?.lastLocation?.latitude)
                    jsonLog.put("Longitude", p0?.lastLocation?.longitude)
                    jsonLog.put("Timestamp", p0?.lastLocation?.time)
                    posiLogWithTime.put(jsonLog)

                }


                btnLocation.setOnClickListener {
                    wayTime.add(p0?.lastLocation?.time!!)
                    if(!isStarted) {
                        isStarted = true
                    }
                }
            }
        }
        fusedProvider.requestLocationUpdates(locationRequest,locationCallback,null)

        /*
        locationListener = android.location.LocationListener { p0 ->
            if(isStarted){
                val jsonLog = JSONObject()
                jsonLog.put("Latitude", p0.latitude)
                jsonLog.put("Longitude", p0.longitude)
                jsonLog.put("Timestamp", p0.time)
                posiLogWithTime.put(jsonLog)

            }
            btnLocation.setOnClickListener {
                wayTime.add(p0.time)
                if(!isStarted) {
                    isStarted = true
                }
            }
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,1000L,0f,locationListener)
        */
    }

}
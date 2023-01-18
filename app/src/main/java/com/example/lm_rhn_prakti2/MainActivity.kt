package com.example.lm_rhn_prakti2

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.LocationManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import okhttp3.*
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException


class MainActivity : AppCompatActivity(), OnMapReadyCallback {


    private lateinit var mapFragment:SupportMapFragment
    private lateinit var googleMap:GoogleMap

    private lateinit var btnLocation:Button
    private lateinit var btnSave:Button

    private lateinit var fusedProvider:FusedLocationProviderClient
    private lateinit var locationRequest:LocationRequest
    private lateinit var locationCallback:LocationCallback

    //private lateinit var locationManager:LocationManager
    //private lateinit var locationListener:android.location.LocationListener

    private var posiLogWithTime = JSONArray()
    private var isStarted = false

    //Route 1
    /*
    private var pos0 = LatLng(51.44792,7.27067)
    private var pos01 = LatLng(51.44731,7.27120)
    private var pos1 = LatLng(51.44677, 7.27165)
    private var pos2 = LatLng(51.44652,7.27187)
    private var pos3 = LatLng(51.4463,7.27206)
    private var pos4 = LatLng(51.44626,7.27209)
    private var pos5 = LatLng(51.44647,7.27227)
    private var pos6 = LatLng(51.44661,7.27269)
    private var pos7 = LatLng(51.44717,7.2722)
    private var pos8 = LatLng(51.44727,7.27253)
    private var pos9 = LatLng(51.44789,7.27198)
    private var pos10 = LatLng(51.44794,7.27201)
    private var pos11 = LatLng(51.44829,7.27171)
    */
    //Route 2
    private var pos0 = LatLng(51.44879,7.27288)
    private var pos01 = LatLng(51.44817,7.27358)
    private var pos02 = LatLng(51.44798,7.27395)
    private var pos1 = LatLng(51.44742,7.27471)
    private var pos2 = LatLng(51.44728,7.27492)
    private var pos3 = LatLng(51.44727,7.27534)
    //private var pos31 = LatLng(51.44742,7.27540)
    private var pos4 = LatLng(51.44746,7.27543)
    private var pos5 = LatLng(51.44785,7.27596)
    private var pos6 = LatLng(51.44837,7.27652)
    private var pos7 = LatLng(51.44917,7.27583)
    private var pos8 = LatLng(51.44988,7.27525)
    private var pos9 = LatLng(51.44974,7.27490)
    private var pos10 = LatLng(51.44977, 7.27471)
    private var pos11 = LatLng(51.44918, 7.27334)

    //Route 3
    /*
    private var pos0 = LatLng(51.53084,7.22745)
    private var pos01 = LatLng(51.53166,7.22723)
    private var pos02 = LatLng(51.53191,7.22825)
    private var pos1 = LatLng(51.53211,7.22909)
    private var pos2 = LatLng(51.53224,7.22963)
    private var pos3 = LatLng(51.53177,7.23)
    private var pos4 = LatLng(51.53105,7.23061)
    private var pos5 = LatLng(51.53071,7.22935)
    private var pos6 = LatLng(51.53038,7.22763)
    */

    //Route1
    //private var waypoints = arrayListOf(pos0,pos01,pos1,pos2,pos3,pos4,pos5,pos6,pos7,pos8,pos9,pos10,pos11,pos0)

    //Route2
    private var waypoints = arrayListOf(pos0,pos01,pos02,pos1,pos2,pos3,pos4,pos5,pos6,pos7,pos8,pos9,pos10,pos11,pos0)

    //Route3
    //private var waypoints = arrayListOf(pos0,pos01,pos02,pos1,pos2,pos3,pos4,pos5,pos6,pos0)

    private var wayTime = arrayListOf<Long>()
    private var waypointsWithTime = JSONArray()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initView()

        //locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if(ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION), 0)
            }
        }else{
            getLocation()
        }

    }


    private fun initView(){
        btnLocation = findViewById(R.id.btnLocation)
        btnSave = findViewById(R.id.btnSpeichern)

        mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnSave.setOnClickListener {

            if(wayTime.size >= waypoints.size){

                //locationManager.removeUpdates(locationListener)
                fusedProvider.removeLocationUpdates(locationCallback)

                isStarted = false

                for (i in 0 until waypoints.size) {
                    val jsonLog = JSONObject()
                    jsonLog.put("Latitude", waypoints[i].latitude)
                    jsonLog.put("Longitude", waypoints[i].longitude)
                    jsonLog.put("Timestamp", wayTime[i])
                    waypointsWithTime.put(jsonLog)
                }

                val interPolartionsArray = alleKoorLinearInterpolieren(waypointsWithTime)
                httpPost(posiLogWithTime,"Posipoints")
                httpPost(interPolartionsArray,"InterpolWaypoints")
                putCircle(interPolartionsArray,Color.BLUE)
                putCircle(posiLogWithTime,Color.GREEN)

            }else{
                Toast.makeText(this, "Zuwenig Time for Waypoints", Toast.LENGTH_SHORT).show()
            }

        }

    }

    override fun onMapReady(p0: GoogleMap) {
        googleMap = p0
        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL
        var i = 1
        waypoints.forEach{
            googleMap.addMarker(MarkerOptions().position(it).title("Pos$i"))
            i++
        }
        //Route 1
        //googleMap.addPolyline(PolylineOptions().add(pos0).add(pos01).add(pos1).add(pos2).add(pos3).add(pos4).add(pos5).add(pos6).add(pos7).add(pos8).add(pos9).add(pos10).add(pos11).add(pos0).color(Color.RED))

        //Route 2
        googleMap.addPolyline(PolylineOptions().add(pos0).add(pos01).add(pos02).add(pos1).add(pos2).add(pos3).add(pos4).add(pos5).add(pos6).add(pos7).add(pos8).add(pos9).add(pos10).add(pos11).add(pos0).color(Color.RED))

        //Route 3
        //googleMap.addPolyline(PolylineOptions().add(pos0).add(pos01).add(pos02).add(pos1).add(pos2).add(pos3).add(pos4).add(pos5).add(pos6).add(pos0).color(Color.RED))


        val cameraUpdate = CameraUpdateFactory.newLatLngZoom(waypoints[0],18f)
        googleMap.animateCamera(cameraUpdate)
    }


    @SuppressLint("MissingPermission")
    private fun getLocation(){

        fusedProvider = LocationServices.getFusedLocationProviderClient(this)
        //locationRequest = LocationRequest().setFastestInterval(2000).setInterval(3000).setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
        locationRequest = LocationRequest().setFastestInterval(2000).setInterval(3000).setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
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


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode == 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            getLocation()
        }

    }

    private fun httpPost(text:JSONArray,api:String){
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

    private fun putCircle(samples:JSONArray,color:Int){
        for(i in 0 until samples.length()){
            val positionLatLng = LatLng((samples.getJSONObject(i).get("Latitude") as Double),(samples.getJSONObject(i).get("Longitude") as Double))
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


}
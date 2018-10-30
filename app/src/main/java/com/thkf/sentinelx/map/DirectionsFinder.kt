package com.thkf.sentinelx.map

import android.content.Context
import android.graphics.Color
import android.os.AsyncTask
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import com.google.maps.android.PolyUtil
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

const val ROUTES = "routes"
const val DURATION = "duration"
const val DISTANCE = "distance"
const val END_ADDRESS = "end_address"
const val START_ADDRESS = "start_address"
const val TEXT = "text"
const val POLYLINE = "polyline"
const val POINTS = "points"
const val LEGS = "legs"
const val STEPS = "steps"
const val STATUS = "status"
const val OK = "OK"
const val ERROR = "ERROR"

const val DRIVING = "driving"
const val WALKING = "walking"

interface AsyncResponse {
    fun onProcessFinish(data: Pair<String, List<Path>?>)
}

fun getDirectionUrl(origin: LatLng, destination: LatLng, alternatives: Boolean, modeType: Int): String {
    val start = "origin=${origin.latitude},${origin.longitude}"
    val end = "destination=${destination.latitude},${destination.longitude}"
    val mode = if(modeType == 1) "mode=driving" else "mode=walking"
    val alternates = if(alternatives) "&alternatives=true" else ""
    val api = "key=AIzaSyA-ItOXImN9QIUYA2_cQnkiNDtDWyp4Tlc"
//    val parameters = "$start&$end&$mode&$api"
    val parameters = "$start&$end&$mode$alternates&$api"
    val output = "json"
    return "https://maps.googleapis.com/maps/api/directions/$output?$parameters"
}

fun downloadUrl(strUrl: String): String {
    var data = ""
    var iStream: InputStream? = null
    var urlConnection: HttpURLConnection? = null
    try {
        val url = URL(strUrl)
        urlConnection = url.openConnection() as HttpURLConnection
        urlConnection.connect()
        iStream = urlConnection.inputStream
        data = iStream.bufferedReader().use(BufferedReader::readText)
    } catch (e: IOException) {
        e.printStackTrace()
    } finally {
        if (iStream != null) {
            iStream.close()
        }
        if (urlConnection != null) {
            urlConnection.disconnect()
        }
    }
    return data
}

class GetDirectionsData() : AsyncTask<String, Unit, String>() {

    private lateinit var taskResponse: AsyncResponse

    constructor(context: Context) : this() {
        if (context is AsyncResponse) {
            taskResponse = context
        }
    }

    override fun doInBackground(vararg args: String): String {
        val url = args[0]
        var directionData = ""
        try {
            directionData = downloadUrl(url)
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return directionData
    }

    override fun onPostExecute(result: String) {
        val parser = DataParser(result)
        val code = parser.getStatusCode()
        if (code == OK) {
            val directions = parser.parseMultiDirections()
            taskResponse.onProcessFinish(Pair(code, directions))
//            val directions = parser.parseDirections()
//            val info = parser.getInfo()
//
//            val polyLines = mutableListOf<PolylineOptions>()
//            directions.forEach {
//                val poly = PolylineOptions()
//                poly.color(Color.RED)
//                poly.width(8f)
//                poly.addAll(PolyUtil.decode(it))
//
//                polyLines.add(poly)
//            }
//            taskResponse.onProcessFinish(Triple(code, polyLines, info))
        } else {
            taskResponse.onProcessFinish(Pair(ERROR, null))
        }
    }

}

class DataParser(val data: String) {

    fun getStatusCode(): String {
        return if (JSONObject(data).getString(STATUS) == OK) OK else ERROR
    }

    fun getInfo(): HashMap<String, String> {
        val infoMap = hashMapOf<String, String>()

        try {
            val jsonObject = JSONObject(data)
            val json = jsonObject.getJSONArray(ROUTES)
            infoMap[DURATION] = json.getJSONObject(0).getJSONArray(LEGS).getJSONObject(0).getJSONObject(DURATION).getString(TEXT)
            infoMap[DISTANCE] = json.getJSONObject(0).getJSONArray(LEGS).getJSONObject(0).getJSONObject(DISTANCE).getString(TEXT)
            infoMap[START_ADDRESS] = json.getJSONObject(0).getJSONArray(LEGS).getJSONObject(0).getString(START_ADDRESS)
            infoMap[END_ADDRESS] = json.getJSONObject(0).getJSONArray(LEGS).getJSONObject(0).getString(END_ADDRESS)

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return infoMap
    }

    private fun getInfo(routesArray: JSONArray): MutableList<HashMap<String, String>> {
        val infoList = mutableListOf<HashMap<String, String>>()
        try {
            val length = routesArray.length()
            for (i in 0 until length) {
                val infoMap = hashMapOf<String, String>()
                infoMap[DURATION] = routesArray.getJSONObject(i).getJSONArray(LEGS).getJSONObject(0).getJSONObject(DURATION).getString(TEXT)
                infoMap[DISTANCE] = routesArray.getJSONObject(i).getJSONArray(LEGS).getJSONObject(0).getJSONObject(DISTANCE).getString(TEXT)
                infoMap[START_ADDRESS] = routesArray.getJSONObject(i).getJSONArray(LEGS).getJSONObject(0).getString(START_ADDRESS)
                infoMap[END_ADDRESS] = routesArray.getJSONObject(i).getJSONArray(LEGS).getJSONObject(0).getString(END_ADDRESS)
                infoList.add(infoMap)
            }

        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return infoList
    }

    fun parseDirections(): MutableList<String> {
        var jsonArray: JSONArray? = null

        try {
            val jsonObject = JSONObject(data)
            jsonArray = jsonObject.getJSONArray(ROUTES)
                    .getJSONObject(0).getJSONArray(LEGS)
                    .getJSONObject(0).getJSONArray(STEPS)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return getPaths(jsonArray)
    }

    fun parseMultiDirections(): MutableList<Path> {
        val paths = mutableListOf<MutableList<String>>()
        val listOfPaths = mutableListOf<Path>()
        try {
            val jsonObject = JSONObject(data)
            val routesArray = jsonObject.getJSONArray(ROUTES)
            val info = getInfo(routesArray)

            val length = routesArray.length()
            for (i in 0 until length) {
                val pathArray = routesArray.getJSONObject(i).getJSONArray(LEGS).getJSONObject(0).getJSONArray(STEPS)
                val mutableList = getPaths(pathArray)
                paths.add(mutableList)
            }
            val polyLines = mutableListOf<PolylineOptions>()
            paths.forEachIndexed { index, mutableList ->
                mutableList.forEach {
                    val poly = PolylineOptions()
                    poly.color(getColor(index))
                    poly.width(8f)
                    poly.addAll(PolyUtil.decode(it))

                    polyLines.add(poly)
                }
                listOfPaths.add(Path(info[index], polyLines))
            }


        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return listOfPaths
    }


    private fun getPaths(googleStepsJson: JSONArray?): MutableList<String> {
        val count = googleStepsJson!!.length()
        val polyLines = mutableListOf<String>()

        try {
            for (i in 0 until count) {
                polyLines.add(getPath(googleStepsJson.getJSONObject(i)))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return polyLines
    }

    private fun getPath(googlePathJson: JSONObject): String {
        var polyline = ""
        try {
            polyline = googlePathJson.getJSONObject(POLYLINE).getString(POINTS)
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return polyline
    }

    private fun getColor(index: Int): Int {
        return when (index) {
            0 -> {
                Color.RED
            }
            1 -> {
                Color.BLUE
            }
            2 -> {
                Color.GREEN
            }
            3 -> {
                Color.GRAY
            }
            4 -> {
                Color.MAGENTA
            }
            5 -> {
                Color.CYAN
            }
            else -> {
                Color.BLACK
            }
        }
    }

}

class Path(val info: HashMap<String, String>, val polylineOptions: MutableList<PolylineOptions>)
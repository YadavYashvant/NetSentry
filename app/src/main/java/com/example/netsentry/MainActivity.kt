package com.example.netsentry

import android.annotation.SuppressLint
import android.content.Context
import android.icu.text.SimpleDateFormat
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.netsentry.ui.theme.NetSentryTheme
import java.util.Date
import java.util.Locale

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import java.util.*


class MainActivity : ComponentActivity() {

    private val REQUEST_PERMISSION_CODE = 101

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check if the permission is already granted
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE) != PackageManager.PERMISSION_GRANTED) {
            // Request the permission
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_NETWORK_STATE), REQUEST_PERMISSION_CODE)
        } else {
            // Permission is already granted, proceed with the app
            setContent {
                App()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_PERMISSION_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Permission was granted, proceed with the app
            setContent {
                App()
            }
        }
    }
}

@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Daily Data Usage") })
        },
        content = { MainContent() }
    )
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun MainContent() {
    val context = LocalContext.current
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val dataUsageList = mutableStateListOf<DataUsageItem>()

    LaunchedEffect(Unit) {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                val networkInfo = connectivityManager.getNetworkInfo(network)
                val networkType = networkInfo?.typeName ?: "Unknown"
                val usageInfo = dataUsageList.find { it.networkType == networkType }
                if (usageInfo != null) {
                    dataUsageList.remove(usageInfo)
                }
            }

            override fun onUnavailable() {
                super.onUnavailable()
                val networkInfo = connectivityManager.activeNetworkInfo
                val networkType = networkInfo?.typeName ?: "Unknown"
                val usageInfo = dataUsageList.find { it.networkType == networkType }
                if (usageInfo != null) {
                    dataUsageList.remove(usageInfo)
                }
            }

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                val networkInfo = connectivityManager.getNetworkInfo(network)
                val networkType = networkInfo?.typeName ?: "Unknown"
                var usageInfo = dataUsageList.find { it.networkType == networkType }
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                if (usageInfo == null || usageInfo.date != currentDate) {
                    usageInfo = DataUsageItem(networkType, currentDate)
                    dataUsageList.add(usageInfo)
                }
                if(usageInfo.dataUsage.isNotEmpty()) {
                    usageInfo.dataUsage = usageInfo.dataUsage.substring(11)
                }
            }
        }
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 100.dp)) {
        items(dataUsageList) { dataUsageItem ->
            DataUsageListItem(dataUsageItem)
        }
    }
}

@Composable
fun DataUsageListItem(dataUsageItem: DataUsageItem) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        //backgroundColor = MaterialTheme.colors.primary
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = dataUsageItem.networkType, fontSize = 18.sp, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = dataUsageItem.dataUsage, fontSize = 14.sp, color = Color.White)
        }
    }
}

data class DataUsageItem(val networkType: String, val date: String, var dataUsage: String = "")
/*
class MainActivity : ComponentActivity() {

    private val REQUEST_PERMISSION_CODE = 101
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)



        */
/*setContent {
            NetSentryTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App()
                }
            }
        }*//*

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun App() {
    Scaffold(
        topBar = {
            TopAppBar(title = { Text(text = "Daily Data Usage")})
        },
        content = {MainContent()}
    )
}

@Composable
fun MainContent(){
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        DataUsageList()
    }
}

@SuppressLint("UnrememberedMutableState")
@Composable
fun DataUsageList() {
    val context = LocalContext.current
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as
            ConnectivityManager
    val dataUsageList = mutableStateListOf<Pair<String, String>>()

    LaunchedEffect(Unit) {
        val networkCallback = object:ConnectivityManager.NetworkCallback(){
            override fun onLosing(network: Network, maxMsToLive: Int) {
                super.onLosing(network, maxMsToLive)
                val networkInfo = connectivityManager.getNetworkInfo(network)
                val networkType = networkInfo?.typeName?:"Unknown"
                val usageInfo = dataUsageList.filter {
                    it.first == networkType
                }.maxByOrNull { it.second.toLong() }
                if(usageInfo != null) {
                    dataUsageList.remove(usageInfo)
                }
            }

            override fun onUnavailable() {
                super.onUnavailable()
                val networkInfo = connectivityManager.activeNetworkInfo
                val networkType = networkInfo?.typeName?:"Unknown"
                val usageInfo = dataUsageList.filter { it.first == networkType }
                    .maxByOrNull { it.second.toLong() }
                if(usageInfo != null) {
                    dataUsageList.remove(usageInfo)
                }
            }

            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                val networkInfo = connectivityManager.getNetworkInfo(network)
                val networkType = networkInfo?.typeName ?: "Unknown"
                var usageInfo = dataUsageList.filter { it.first == networkType }.maxByOrNull { it.second.toLong() }
                val currentTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
                if (usageInfo == null) {
                    usageInfo = Pair(networkType, currentTime)
                    dataUsageList.add(usageInfo)
                }
                usageInfo = usageInfo.copy(second = currentTime)
            }
        }
        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 100.dp)) {
        items(dataUsageList) {dataUsageItem ->
            DataUsageListItem(dataUsageItem)
        }
    }
}

@Composable
fun DataUsageListItem(dataUsageItem: Pair<String, String>) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(10.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = dataUsageItem.first, fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = dataUsageItem.second, fontSize = 14.sp)
        }

    }
}
*/


package com.example.netsentry

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import com.example.netsentry.ui.theme.NetSentryTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NetSentryTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    App()
                }
            }
        }
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
        }
    }
}

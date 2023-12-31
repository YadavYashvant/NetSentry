package com.example.netsentry.viewmodels
import android.annotation.SuppressLint
import android.content.Context
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.app.usage.NetworkStats.Bucket
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class DataUsageViewModel(private val context: Context) : ViewModel() {
    private val networkStatsManager =
        context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

    // Get the total data usage for the device
    fun getTotalDataUsage(callback: (Long) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val totalUsage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val networkType = getNetworkType()
                val bucket = queryDataUsage(networkType, 0, System.currentTimeMillis())
                bucket.rxBytes + bucket.txBytes
            } else {
                // Handle devices running Android versions older than Marshmallow
                // This is a placeholder; you might need to implement a different approach
                0L
            }
            callback(totalUsage / (1024 * 1024)) // Convert bytes to MB
        }
    }

    fun getTodayDataUsage(callback: (Long) -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            val todayUsage = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val networkType = getNetworkType()
                val startTime = getStartOfDayMillis()
                val bucket = queryDataUsage(networkType, startTime, System.currentTimeMillis())
                bucket.rxBytes + bucket.txBytes
            } else {
                // Handle devices running Android versions older than Marshmallow
                // This is a placeholder; you might need to implement a different approach
                0L
            }
            callback(todayUsage / (1024 * 1024)) // Convert bytes to MB
        }
    }

    private fun getNetworkType(): Int {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork?.let {
            connectivityManager.getNetworkCapabilities(it)
        }
        return when {
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> {
                ConnectivityManager.TYPE_WIFI
            }
            networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> {
                ConnectivityManager.TYPE_MOBILE
            }
            else -> ConnectivityManager.TYPE_DUMMY
        }
    }

    private fun queryDataUsage(networkType: Int, startTime: Long, endTime: Long): Bucket {
        val subscriberId = getSubscriberId()
        val bucket = Bucket()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkStats = networkStatsManager.querySummary(
                networkType,
                subscriberId,
                startTime,
                endTime
            )
            networkStats.use {
                while (it.hasNextBucket()) {
                    it.getNextBucket(bucket)
                }
            }
        }
        return bucket
    }

    private fun getSubscriberId(): String {
        return try {
            val telephonyManager =
                context.getSystemService(Context.TELEPHONY_SERVICE) as android.telephony.TelephonyManager
            telephonyManager.subscriberId ?: ""
        } catch (e: SecurityException) {
            // Handle the case when the app doesn't have the necessary permissions
            ""
        }
    }

    @SuppressLint("NewApi")
    private fun getStartOfDayMillis(): Long {
        val now = Instant.now()
        val midnight =
            ZonedDateTime.ofInstant(now, ZoneId.systemDefault()).toLocalDate().atStartOfDay(
                ZoneId.systemDefault()
            )
        return midnight.toInstant().toEpochMilli()
    }
}

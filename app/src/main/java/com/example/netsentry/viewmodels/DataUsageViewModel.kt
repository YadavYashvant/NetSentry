package com.example.netsentry.viewmodels

import android.content.Context
import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.app.usage.NetworkStats.Bucket
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class DataUsageViewModel(private val context: Context) {
    private val networkStatsManager =
        context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager

    suspend fun getTotalDataUsage(): Long {
        return withContext(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val networkType = getNetworkType()
                val bucket = queryDataUsage(networkType, 0, System.currentTimeMillis())
                bucket.rxBytes + bucket.txBytes
            } else {
                // Handle devices running Android versions older than Marshmallow
                // This is a placeholder; you might need to implement a different approach
                0L
            }
        }
    }

    suspend fun getTodayDataUsage(): Long {
        return withContext(Dispatchers.IO) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val networkType = getNetworkType()
                val startTime = getStartOfDayMillis()
                val bucket = queryDataUsage(networkType, startTime, System.currentTimeMillis())
                bucket.rxBytes + bucket.txBytes
            } else {
                // Handle devices running Android versions older than Marshmallow
                // This is a placeholder; you might need to implement a different approach
                0L
            }
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
        val bucket = NetworkStats.Bucket()

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
            val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as android.telephony.TelephonyManager
            telephonyManager.subscriberId ?: ""
        } catch (e: SecurityException) {
            // Handle the case when the app doesn't have the necessary permissions
            ""
        }
    }


    private fun getStartOfDayMillis(): Long {
        val now = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Instant.now()
        } else {
            TODO("VERSION.SDK_INT < O")
        }
        val midnight = ZonedDateTime.ofInstant(now, ZoneId.systemDefault()).toLocalDate()
            .atStartOfDay(ZoneId.systemDefault())
        return midnight.toInstant().toEpochMilli()
    }
}

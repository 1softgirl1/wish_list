package com.example.wish_list.ui

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri

object RouteLauncher {
    fun launchRoute(
        context: Context,
        destinationLat: Double,
        destinationLon: Double,
        startLat: Double? = null,
        startLon: Double? = null
    ): Boolean {
        val rtext = if (startLat != null && startLon != null) {
            "$startLat,$startLon~$destinationLat,$destinationLon"
        } else {
            "~$destinationLat,$destinationLon"
        }
        val appUri = Uri.parse("yandexmaps://maps.yandex.ru/?rtext=$rtext&rtt=auto")
        val webUri = Uri.parse("https://yandex.ru/maps/?rtext=$rtext&rtt=auto")

        return launch(context, appUri) || launch(context, webUri)
    }

    private fun launch(context: Context, uri: Uri): Boolean {
        val intent = Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        val packageManager: PackageManager = context.packageManager
        val isAvailable = packageManager.queryIntentActivities(intent, 0).isNotEmpty()
        if (!isAvailable) return false
        context.startActivity(intent)
        return true
    }
}

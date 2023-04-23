package com.rslakra.apnetvapp.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class NetworkChangeReceiver : BroadcastReceiver() {

    //should check null because in airplane mode it will be null
    fun isOnline(context: Context): Boolean {
//        try {
//            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
//            val netInfo = cm.activeNetworkInfo
//            //should check null because in airplane mode it will be null
//            return (netInfo != null && netInfo.isConnected)
//        } catch (e: Exception) {
//            return false
//        }

        return false
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (isOnline(context)) {
            onInternetUpdate(context, true)
        } else {
            onInternetUpdate(context, false)
        }
    }

    private fun onInternetUpdate(context: Context, isConnected: Boolean) {
//        val intent = Intent(context.getString(R.string.keySendInternetStatus))
//        intent.putExtra("isConnected", isConnected)
//        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}

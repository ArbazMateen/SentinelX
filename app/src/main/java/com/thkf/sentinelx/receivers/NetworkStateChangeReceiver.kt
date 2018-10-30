package com.thkf.sentinelx.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.thkf.sentinelx.commons.isOnline

interface OnlineStatusListiner {
    fun isOnline(online: Boolean)
}

class NetworkStateChangeReceiver : BroadcastReceiver() {

    companion object {

        var online = false

    }

    private var listener: OnlineStatusListiner? = null
//    lateinit var dialog: MaterialDialog

    override fun onReceive(context: Context, intent: Intent) {

//        dialog = MaterialDialog.Builder(context)
//                .iconRes(R.drawable.warning)
//                .title("Internet connection error...")
//                .backgroundColorRes(R.color.greenish)
//                .cancelable(false)
//                .build()

        online = isOnline(context)

        if(context is OnlineStatusListiner) {
            listener = context
            listener?.isOnline(online)
        }

    }

}

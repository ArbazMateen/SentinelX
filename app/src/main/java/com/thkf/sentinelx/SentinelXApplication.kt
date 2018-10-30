package com.thkf.sentinelx

import android.app.Application
import android.content.Context
import android.support.multidex.MultiDex
import com.google.firebase.FirebaseApp
import com.nostra13.universalimageloader.core.ImageLoader
import com.thkf.sentinelx.commons.UniversalImageLoader


class SentinelXApplication : Application() {

//    companion object {
//        var userOnline = false
//        var loginUser = User()
//    }

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
        ImageLoader.getInstance().init(UniversalImageLoader(this).config)
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

}
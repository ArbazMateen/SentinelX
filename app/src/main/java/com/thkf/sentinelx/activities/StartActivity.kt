package com.thkf.sentinelx.activities

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.thkf.sentinelx.R
import com.thkf.sentinelx.commons.EMAIL_VERIFIED
import com.thkf.sentinelx.commons.IMAGE
import com.thkf.sentinelx.commons.OFFLINE
import com.thkf.sentinelx.commons.ONLINE
import com.thkf.sentinelx.commons.VERIFIED
import com.thkf.sentinelx.commons.auth
import com.thkf.sentinelx.commons.displayName
import com.thkf.sentinelx.commons.doc
import com.thkf.sentinelx.commons.fireDatabase
import com.thkf.sentinelx.commons.firestore
import com.thkf.sentinelx.commons.statusPath
import com.thkf.sentinelx.commons.uid
import com.thkf.sentinelx.extensions.Prefs
import com.thkf.sentinelx.extensions.logE
import com.thkf.sentinelx.extensions.logI
import com.thkf.sentinelx.fragments.SplashFragment
import com.thkf.sentinelx.models.User

class StartActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        logI("onCreate")
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start)

        authListener()

        fragmentManager.beginTransaction()
                .replace(R.id.frame_layout, SplashFragment(), SplashFragment.TAG)
                .commit()
    }

    private fun authListener() {
        logI("authListener")
        super.onStart()
        auth().addAuthStateListener { auth ->
            if (auth.currentUser != null) {
                logI("'${displayName()}' is online")

                MainActivity.LOGIN = true

                fireDatabase().getReference(statusPath(uid())).setValue(ONLINE)

                if(auth.currentUser?.isEmailVerified!! && !Prefs(this).getBoolean(EMAIL_VERIFIED)) {
                    logI("Email verified...")
                    Prefs(this).putBoolean(EMAIL_VERIFIED, true)
                    val data =  mapOf(EMAIL_VERIFIED to VERIFIED)
                    firestore().document(doc(auth.currentUser?.uid!!)).update(data)
                } else {
                    logE("Email not verified...")
                }

                firestore().document(doc(auth.currentUser?.uid!!)).get().addOnCompleteListener { task ->
                    if(task.isSuccessful) {
                        val user = task.result.toObject(User::class.java)
                        if (user != null) {
                            Prefs(this@StartActivity).putString(IMAGE, user.image)
                        }
                        logE(user.toString())
                    }
                }

                fireDatabase().getReference(statusPath(uid())).onDisconnect().setValue(OFFLINE)

            } else {
                logE("Current user is offline")
                MainActivity.LOGIN = false
            }
        }
        firestore().firestoreSettings = FirebaseFirestoreSettings.Builder().setPersistenceEnabled(true).build()
    }

}

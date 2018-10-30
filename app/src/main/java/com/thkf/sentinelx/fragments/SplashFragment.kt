package com.thkf.sentinelx.fragments


import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.afollestad.materialdialogs.MaterialDialog
import com.google.firebase.firestore.SetOptions
import com.thkf.sentinelx.R
import com.thkf.sentinelx.activities.MainActivity
import com.thkf.sentinelx.commons.*
import com.thkf.sentinelx.extensions.Prefs
import com.thkf.sentinelx.extensions.logI
import com.thkf.sentinelx.prefs.FIRST_LOGIN
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch


class SplashFragment : Fragment() {

    companion object {
        val TAG = SplashFragment::class.java.name.toString()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        launch(UI) {
            delay(1000)
            checkOnlineAndLogin()
        }

    }

    private fun checkOnlineAndLogin() {
        logI("checkOnlineAndLogin")
        if (isOnline(activity)) {
            val email = Prefs(activity).get(EMAIL, "")
            val pass = Prefs(activity).get(PASSWORD, "")

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                auth().signInWithEmailAndPassword(email, pass)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {

                                if (Prefs(activity).get(FIRST_LOGIN, true)) {
                                    Prefs(activity).put(FIRST_LOGIN, false)
                                    launch {
                                        val username = Prefs(activity).get(NAME, "")

                                        firestore().collection(ROOT).document(uid())
                                                .set(mapOf(NAME to username), SetOptions.merge())
                                    }
                                }

                                mainActivity()
                            } else {
                                loginFragment()
                            }
                        }
                        .addOnFailureListener { _ ->
                            loginFragment()
                        }
            } else {
                loginFragment()
            }
        } else {
            if (Prefs(activity).get(FIRST_LOGIN, true)) {
                MaterialDialog.Builder(activity)
                        .iconRes(R.drawable.warning)
                        .title("Internet connection error...")
                        .backgroundColorRes(R.color.greenish)
                        .cancelable(false)
                        .negativeText("Close")
                        .onNegative { _, _ ->
                            activity.finish()
                        }
                        .show()
            } else {
                mainActivity()
            }
        }
    }

    private fun mainActivity() {
        activity.startActivity(Intent(activity, MainActivity::class.java))
        activity.finish()
    }

    private fun loginFragment() {
        activity.fragmentManager.beginTransaction()
                .replace(R.id.frame_layout, LoginFragment(), LoginFragment.TAG)
                .commit()
    }

}

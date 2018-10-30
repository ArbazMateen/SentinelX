package com.thkf.sentinelx.fragments

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.google.firebase.firestore.SetOptions
import com.thkf.sentinelx.R
import com.thkf.sentinelx.activities.MainActivity
import com.thkf.sentinelx.commons.*
import com.thkf.sentinelx.extensions.Prefs
import com.thkf.sentinelx.extensions.logI
import com.thkf.sentinelx.extensions.textNotNull
import com.thkf.sentinelx.extensions.toastLong
import com.thkf.sentinelx.prefs.FIRST_LOGIN
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.coroutines.experimental.launch

class LoginFragment : Fragment() {

    companion object {
        val TAG = LoginFragment::class.java.name.toString()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dialog = MaterialDialog.Builder(activity)
                .content("Authenticating...")
                .progress(true, 0)
                .cancelable(false)
                .theme(Theme.LIGHT)
                .build()

        btn_login.setOnClickListener {
            hideSoftKeyboard(activity)
            if (validateEmail() && validatePass()) {
                if (!isOnline(activity)) {
                    MaterialDialog.Builder(activity)
                            .iconRes(R.drawable.warning)
                            .title("Internet connection error...")
                            .theme(Theme.LIGHT)
                            .show()
                    return@setOnClickListener
                }

                dialog.show()

                val email = et_email.text.toString()
                val password = et_password.text.toString()

                auth().signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                if (Prefs(activity).get(FIRST_LOGIN, true)) {
                                    Prefs(activity).put(EMAIL, email)
                                    Prefs(activity).put(PASSWORD, password)
                                    Prefs(activity).put(FIRST_LOGIN, false)
                                    Prefs(activity).put(NAME, auth().currentUser!!.displayName.toString())
                                    Prefs(activity).put(UID, auth().currentUser!!.uid)
                                    updateUsername()
                                }

                                dialog.hide()
                                activity.startActivity(Intent(activity, MainActivity::class.java))
                                activity.finish()
                            } else {
                                dialog.hide()
                                toastLong("Cannot login to your account!")
                            }
                        }
                        .addOnFailureListener { _ ->
                            dialog.hide()
                            toastLong("Something went wrong! please check and try again.")
                        }

            }
        }

        tv_forgot_password.setOnClickListener {
            logI("Forgot Password clicked...")
        }

        tv_create_account.setOnClickListener {
            activity.fragmentManager.beginTransaction()
                    .replace(R.id.frame_layout, SignUpFragment(), SignUpFragment.TAG)
                    .addToBackStack(SignUpFragment.TAG)
                    .commit()
        }
    }

    private fun validateEmail(): Boolean {
        return if (et_email.text.trim().isBlank()) {
            layout_email.error = getString(R.string.error_field_required)
            requestFocus(et_email)
            false
        } else {
            if (Patterns.EMAIL_ADDRESS.matcher(et_email.textNotNull()).matches()) {
                layout_email.isErrorEnabled = false
                true
            } else {
                layout_email.error = getString(R.string.error_invalid_email)
                false
            }
        }
    }

    private fun validatePass(): Boolean {
        return if (et_password.text.trim().isBlank()) {
            layout_password.error = getString(R.string.error_field_required)
            requestFocus(et_password)
            false
        } else {
            layout_password.isErrorEnabled = false
            true
        }
    }

    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

    private fun updateUsername() {
        launch {
            val username = Prefs(activity).get(NAME, "")

            firestore().collection(ROOT).document(uid())
                    .set(mapOf(NAME to username), SetOptions.merge())
        }
    }

}

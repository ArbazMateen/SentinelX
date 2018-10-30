package com.thkf.sentinelx.fragments


import android.app.Fragment
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.google.firebase.auth.UserProfileChangeRequest
import com.thkf.sentinelx.R
import com.thkf.sentinelx.commons.*
import com.thkf.sentinelx.extensions.Prefs
import com.thkf.sentinelx.extensions.logI
import com.thkf.sentinelx.extensions.textNotNull
import com.thkf.sentinelx.extensions.toastLong
import com.thkf.sentinelx.prefs.FIRST_LOGIN
import kotlinx.android.synthetic.main.fragment_signup.*


class SignUpFragment : Fragment() {

    companion object {
        val TAG = SignUpFragment::class.java.name.toString()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dialog = MaterialDialog.Builder(activity)
                .content("Creating new Account...")
                .progress(true, 0)
                .cancelable(false)
                .theme(Theme.LIGHT)
                .build()

        btn_sign_up.setOnClickListener {
            hideSoftKeyboard(activity)
            if (validateUsername() && validateEmail() && validatePass() && validateConfirmPass()) {
                if (validateMatchPass()) {
                    val username = et_username.text.toString()
                    val email = et_email.text.toString()
                    val password = et_confirm_password.text.toString()

                    if(!isOnline(activity)) {
                        MaterialDialog.Builder(activity)
                                .iconRes(R.drawable.warning)
                                .title("Internet connection error...")
                                .theme(Theme.LIGHT)
                                .show()
                        return@setOnClickListener
                    }

                    dialog.show()

                    auth().createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {

                                    val user = user()!!

                                    if (Prefs(activity).get(FIRST_LOGIN, true)) {
                                        Prefs(activity).put(NAME, username)
                                        Prefs(activity).put(EMAIL, email)
                                        Prefs(activity).put(PASSWORD, password)
                                        Prefs(activity).put(UID, auth().currentUser!!.uid)
                                    }

                                    val profileUpdates = UserProfileChangeRequest.Builder()
                                            .setDisplayName(username).build()

                                    user.updateProfile(profileUpdates).addOnCompleteListener { t ->
                                        if (t.isSuccessful) {
                                            logI("User profile updated.")
                                        }
                                    }

                                    dialog.hide()
                                    auth().signOut()
                                    activity.onBackPressed()
                                } else {
                                    dialog.hide()
                                    toastLong("Cannot create new account!")
                                }
                            }
                            .addOnFailureListener { _ ->
                                dialog.hide()
                                toastLong("Something went wrong! please check and try again.")
                            }
                }
            }
        }

    }

    private fun validateUsername(): Boolean {
        return if (et_username.text.trim().isBlank()) {
            layout_username.error = getString(R.string.error_field_required)
            requestFocus(et_username)
            false
        } else {
            layout_username.isErrorEnabled = false
            true
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

    private fun validateConfirmPass(): Boolean {
        return if (et_confirm_password.text.trim().isBlank()) {
            layout_confirm_password.error = getString(R.string.error_field_required)
            requestFocus(et_confirm_password)
            false
        } else {
            layout_confirm_password.isErrorEnabled = false
            true
        }
    }

    private fun validateMatchPass(): Boolean {
        return if (et_password.text.toString() == et_confirm_password.text.toString()) {
            layout_confirm_password.isErrorEnabled = false
            true
        } else {
            layout_confirm_password.error = getString(R.string.error_password_not_match)
            requestFocus(et_confirm_password)
            false
        }
    }

    private fun requestFocus(view: View) {
        if (view.requestFocus()) {
            activity.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        }
    }

}

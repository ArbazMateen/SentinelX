package com.thkf.sentinelx.extensions

import android.app.Activity
import android.app.Fragment
import android.app.FragmentManager
import android.app.FragmentTransaction
import android.content.Context
import android.util.Log
import android.widget.EditText
import android.widget.Toast


// *************************************************************************************************
//  GENERAL
// *************************************************************************************************

fun executionTime(code: () -> Unit) {
    val start = System.currentTimeMillis()
    code()
    val end = System.currentTimeMillis() - start
    localLogI("Execution Time: $end ms")
}

fun localLogI(text: String) {
    Log.i(">>>>", text)
}

fun localLogW(text: String) {
    Log.w(">>>>", text)
}

fun localLogE(text: String) {
    Log.e(">>>>", text)
}



// *************************************************************************************************
// Any
// *************************************************************************************************

fun Any.logI(text: String) {
    Log.i(">>>> ${this.javaClass.simpleName}", text)
}

fun Any.logW(text: String) {
    Log.w(">>>> ${this.javaClass.simpleName}", text)
}

fun Any.logE(text: String) {
    Log.e(">>>> ${this.javaClass.simpleName}", text)
}

fun toastLong(context: Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_LONG).show()
}

fun toastShort(context: Context, text: String) {
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()
}



// *************************************************************************************************
// Activity
// *************************************************************************************************

fun Activity.toastLong(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}

fun Activity.toastShort(text: String) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}



// *************************************************************************************************
// Fragment
// *************************************************************************************************

fun Fragment.toastLong(text: String) {
    Toast.makeText(activity, text, Toast.LENGTH_LONG).show()
}

fun Fragment.toastShort(text: String) {
    Toast.makeText(activity, text, Toast.LENGTH_SHORT).show()
}



// *************************************************************************************************
// FragmentManager
// *************************************************************************************************

inline fun FragmentManager.transaction(func: FragmentTransaction.() -> Unit) {
    val ft = beginTransaction()
    ft.func()
    ft.commit()
}



// *************************************************************************************************
// EditText
// *************************************************************************************************

fun EditText.textNotNull(): String {
    return if (this.text.trim().isNotBlank()) this.text.toString() else ""
}

fun EditText.textNotNullCap(): String {
    return if (this.text.trim().isNotBlank()) this.text.toString().capitalize() else ""
}



// *************************************************************************************************

package com.thkf.sentinelx.extensions

import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import android.support.annotation.NonNull
import java.util.*
import kotlin.reflect.KProperty

// Abstract Class
// *************************************************************************************************
abstract class PrefDelegate<T>(private val context: Context, private val prefName: String?, val prefKey: String) {

    protected val prefs: SharedPreferences by lazy {
        if (prefName != null)
            context.getSharedPreferences("${context.packageName}_$prefName", Context.MODE_PRIVATE)
        else
            PreferenceManager.getDefaultSharedPreferences(context)
    }

    abstract operator fun getValue(thisRef: Any?, property: KProperty<*>): T
    abstract operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T)
}

// Implementation of Abstract Class
// *************************************************************************************************
class StringPrefDelegate(context: Context, prefName: String?, prefKey: String, private val defaultValue: String)
    : PrefDelegate<String?>(context, prefName, prefKey) {
    override fun getValue(thisRef: Any?, property: KProperty<*>): String
            = prefs.getString(prefKey, defaultValue)

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: String?)
            = prefs.edit().putString(prefKey, value).apply()
}

class IntPrefDelegate(context: Context, prefName: String?, prefKey: String, private val defaultValue: Int)
    : PrefDelegate<Int>(context, prefName, prefKey) {
    override fun getValue(thisRef: Any?, property: KProperty<*>)
            = prefs.getInt(prefKey, defaultValue)

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Int)
            = prefs.edit().putInt(prefKey, value).apply()
}

class FloatPrefDelegate(context: Context, prefName: String?, prefKey: String, private val defaultValue: Float)
    : PrefDelegate<Float>(context, prefName, prefKey) {
    override fun getValue(thisRef: Any?, property: KProperty<*>)
            = prefs.getFloat(prefKey, defaultValue)

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Float)
            = prefs.edit().putFloat(prefKey, value).apply()
}

class BooleanPrefDelegate(context: Context, prefName: String?, prefKey: String, private val defaultValue: Boolean)
    : PrefDelegate<Boolean>(context, prefName, prefKey) {
    override fun getValue(thisRef: Any?, property: KProperty<*>)
            = prefs.getBoolean(prefKey, defaultValue)

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Boolean)
            = prefs.edit().putBoolean(prefKey, value).apply()
}

class LongPrefDelegate(context: Context, prefName: String?, prefKey: String, private val defaultValue: Long)
    : PrefDelegate<Long>(context, prefName, prefKey) {
    override fun getValue(thisRef: Any?, property: KProperty<*>)
            = prefs.getLong(prefKey, defaultValue)

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Long)
            = prefs.edit().putLong(prefKey, value).apply()
}

class StringSetPrefDelegate(context: Context, prefName: String?, prefKey: String, private val defaultValue: Set<String>)
    : PrefDelegate<Set<String>>(context, prefName, prefKey) {
    override fun getValue(thisRef: Any?, property: KProperty<*>): Set<String>
            = prefs.getStringSet(prefKey, defaultValue)

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: Set<String>)
            = prefs.edit().putStringSet(prefKey, value).apply()
}

// delegate functions
// *************************************************************************************************
fun stringPref(context: Context, prefKey: String, defaultValue: String = "")
        = StringPrefDelegate(context, null, prefKey, defaultValue)

fun stringPref(context: Context, prefName: String, prefKey: String, defaultValue: String = "")
        = StringPrefDelegate(context, prefName, prefKey, defaultValue)

fun intPref(context: Context, prefKey: String, defaultValue: Int = 0)
        = IntPrefDelegate(context, null, prefKey, defaultValue)

fun intPref(context: Context, prefName: String, prefKey: String, defaultValue: Int = 0)
        = IntPrefDelegate(context, prefName, prefKey, defaultValue)

fun floatPref(context: Context, prefKey: String, defaultValue: Float = 0f)
        = FloatPrefDelegate(context, null, prefKey, defaultValue)

fun floatPref(context: Context, prefName: String, prefKey: String, defaultValue: Float = 0f)
        = FloatPrefDelegate(context, prefName, prefKey, defaultValue)

fun booleanPref(context: Context, prefKey: String, defaultValue: Boolean = false)
        = BooleanPrefDelegate(context, null, prefKey, defaultValue)

fun booleanPref(context: Context, prefName: String, prefKey: String, defaultValue: Boolean = false)
        = BooleanPrefDelegate(context, prefName, prefKey, defaultValue)

fun longPref(context: Context, prefKey: String, defaultValue: Long = 0L)
        = LongPrefDelegate(context, null, prefKey, defaultValue)

fun longPref(context: Context, prefName: String, prefKey: String, defaultValue: Long = 0L)
        = LongPrefDelegate(context, prefName, prefKey, defaultValue)

fun stringSetPref(context: Context, prefKey: String, defaultValue: Set<String> = HashSet())
        = StringSetPrefDelegate(context, null, prefKey, defaultValue)

fun stringSetPref(context: Context, prefName: String, prefKey: String, defaultValue: Set<String> = HashSet())
        = StringSetPrefDelegate(context, prefName, prefKey, defaultValue)

// Another individual Prefs class
// *************************************************************************************************
class Prefs(@NonNull val context: Context, private val preferencesName: String = "") {

    private val sharedPreferences: SharedPreferences
        get() {
            return if(preferencesName.isEmpty())
                PreferenceManager.getDefaultSharedPreferences(context)
            else
                context.getSharedPreferences("${context.packageName}_$preferencesName", Context.MODE_PRIVATE)
        }

    companion object {
        private val DEFAULT_STRING_VALUE = ""
        private val DEFAULT_INT_VALUE = -1
        private val DEFAULT_DOUBLE_VALUE = -1.0
        private val DEFAULT_FLOAT_VALUE = -1f
        private val DEFAULT_LONG_VALUE = -1L
        private val DEFAULT_BOOLEAN_VALUE = false
    }

    fun get(key: String, defaultValue: String) = getString(key, defaultValue)
    fun get(key: String, defaultValue: Int) = getInt(key, defaultValue)
    fun get(key: String, defaultValue: Long) = getLong(key, defaultValue)
    fun get(key: String, defaultValue: Float) = getFloat(key, defaultValue)
    fun get(key: String, defaultValue: Double) = getDouble(key, defaultValue)
    fun get(key: String, defaultValue: Boolean) = getBoolean(key, defaultValue)
    fun get(key: String, defaultValue: Set<String>) = getStringSet(key, defaultValue)

    fun put(key: String, value: String) = putString(key, value)
    fun put(key: String, value: Int) = putInt(key, value)
    fun put(key: String, value: Long) = putLong(key, value)
    fun put(key: String, value: Float) = putFloat(key, value)
    fun put(key: String, value: Double) = putDouble(key, value)
    fun put(key: String, value: Boolean) = putBoolean(key, value)
    fun put(key: String, value: Set<String>) = putStringSet(key, value)

    fun get(res: Int, defaultValue: String) = getString(context.resources.getString(res), defaultValue)
    fun get(res: Int, defaultValue: Int) = getInt(context.resources.getString(res), defaultValue)
    fun get(res: Int, defaultValue: Long) = getLong(context.resources.getString(res), defaultValue)
    fun get(res: Int, defaultValue: Float) = getFloat(context.resources.getString(res), defaultValue)
    fun get(res: Int, defaultValue: Double) = getDouble(context.resources.getString(res), defaultValue)
    fun get(res: Int, defaultValue: Boolean) = getBoolean(context.resources.getString(res), defaultValue)
    fun get(res: Int, defaultValue: Set<String>) = getStringSet(context.resources.getString(res), defaultValue)

    fun put(res: Int, value: String) = putString(context.resources.getString(res), value)
    fun put(res: Int, value: Int) = putInt(context.resources.getString(res), value)
    fun put(res: Int, value: Long) = putLong(context.resources.getString(res), value)
    fun put(res: Int, value: Float) = putFloat(context.resources.getString(res), value)
    fun put(res: Int, value: Double) = putDouble(context.resources.getString(res), value)
    fun put(res: Int, value: Boolean) = putBoolean(context.resources.getString(res), value)
    fun put(res: Int, value: Set<String>) = putStringSet(context.resources.getString(res), value)

    fun getString(key: String, defaultValue: String = DEFAULT_STRING_VALUE)
            = sharedPreferences.getString(key, defaultValue)

    fun putString(key: String, value: String) {
        sharedPreferences.edit().putString(key, value).apply()
    }

    fun getInt(key: String, defaultValue: Int = DEFAULT_INT_VALUE) = sharedPreferences.getInt(key, defaultValue)

    fun putInt(key: String, value: Int) {
        sharedPreferences.edit().putInt(key, value).apply()
    }

    fun getFloat(key: String, defaultValue: Float = DEFAULT_FLOAT_VALUE) = sharedPreferences.getFloat(key, defaultValue)

    fun putFloat(key: String, value: Float) {
        sharedPreferences.edit().putFloat(key, value).apply()
    }

    fun getLong(key: String, defaultValue: Long = DEFAULT_LONG_VALUE) = sharedPreferences.getLong(key, defaultValue)

    fun putLong(key: String, value: Long) {
        sharedPreferences.edit().putLong(key, value).apply()
    }

    fun getBoolean(key: String, defaultValue: Boolean = DEFAULT_BOOLEAN_VALUE) = sharedPreferences.getBoolean(key, defaultValue)

    fun putBoolean(key: String, value: Boolean) {
        sharedPreferences.edit().putBoolean(key, value).apply()
    }

    fun getDouble(key: String, defaultValue: Double = DEFAULT_DOUBLE_VALUE): Double {
        return if (!contains(key)) defaultValue else java.lang.Double.longBitsToDouble(getLong(key))
    }

    fun putDouble(key: String, value: Double) {
        putLong(key, java.lang.Double.doubleToRawLongBits(value))
    }

    fun getStringSet(key: String, defaultValue: Set<String>) = sharedPreferences.getStringSet(key, defaultValue)

    fun putStringSet(key: String, value: Set<String>) {
        sharedPreferences.edit().putStringSet(key, value).apply()
    }

    fun remove(key: String) {
        sharedPreferences.edit().remove(key).apply()
    }

    // examle
    // key in Prefs(this)
    operator fun contains(key: String): Boolean {
        return sharedPreferences.contains(key)
    }

    fun clear() {
        sharedPreferences.edit().clear().apply()
    }

}
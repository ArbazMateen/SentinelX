package com.thkf.sentinelx.commons

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import com.thkf.sentinelx.models.User


// firestore collections and docs
const val ROOT = "Users"

// Firebase variables name
const val NAME = "name"
const val PASSWORD = "password"
const val EMAIL = "email"
const val IMAGE = "image"
const val UID = "uid"
const val EMAIL_VERIFIED = "email_verified"
const val VERIFIED = "Verified"
const val NOT_VERIFIED = "Not Verified"
const val ROLE = "role"
const val STATUS = "status"
const val LAST_UPDATE = "last_update"
const val LAT = "lat"
const val LON = "lon"
const val BEARING = "bearing"

// Roles
const val ADMIN = "Admin"
const val USER = "User"

// Status
const val ONLINE = "online"
const val OFFLINE = "offline"


const val STORAGE_ROOT = "images"
const val STORAGE_USERS = "users"
const val STORAGE_USER_PROFILE_IMAGE_NAME = "profile_image"


fun auth() = FirebaseAuth.getInstance()

fun signOut() {
    fireDatabase().getReference(statusPath(uid())).setValue(OFFLINE)
    auth().signOut()
}

fun firestore() = FirebaseFirestore.getInstance()

fun fireDatabase() = FirebaseDatabase.getInstance()

fun doc(uid: String) = "$ROOT/$uid"

fun user() = auth().currentUser

fun loginUser(): User {
//    if(SentinelXApplication.userOnline) {
//        return SentinelXApplication.loginUser
//    }
    return User()
}

fun uid(): String {
//    if(SentinelXApplication.userOnline) {
//        return SentinelXApplication.loginUser.uid
//    }
    return auth().currentUser?.uid ?: ""
}

fun displayName(): String {
//    if(SentinelXApplication.userOnline) {
//        return SentinelXApplication.loginUser.name
//    }
    return auth().currentUser?.displayName ?: ""
}

fun email(): String {
//    if(SentinelXApplication.userOnline) {
//        return SentinelXApplication.loginUser.email
//    }
    return auth().currentUser?.email ?: ""
}

fun statusPath(uid: String) = "$ROOT/$uid/$STATUS"


package com.thkf.sentinelx.models

import java.util.*

class User {

    var uid: String = ""
    var image: String = ""
    var name: String = ""
    var email: String = ""
    var email_verified = ""
    var lat: Double = 0.0
    var lon: Double = 0.0
    var bearing: Float = 0.0f
    var role: String = ""
    var last_update: Date? = null
    var status: String = ""

    override fun toString(): String {
        return "\nUser >>> UID: $uid \nName: $name \nEmail: $email \nEmail Verified: $email_verified \n" +
                "Image: $image \nLat: $lat \nLon: $lon \nDate: $last_update \nStatus: $status \n" +
                "Role: $role "
    }

}

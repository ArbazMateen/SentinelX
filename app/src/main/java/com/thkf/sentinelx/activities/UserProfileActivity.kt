package com.thkf.sentinelx.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import android.widget.LinearLayout
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.Theme
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageMetadata
import com.nostra13.universalimageloader.core.ImageLoader
import com.thkf.sentinelx.R
import com.thkf.sentinelx.commons.EMAIL
import com.thkf.sentinelx.commons.EMAIL_VERIFIED
import com.thkf.sentinelx.commons.IMAGE
import com.thkf.sentinelx.commons.LAT
import com.thkf.sentinelx.commons.LON
import com.thkf.sentinelx.commons.NAME
import com.thkf.sentinelx.commons.NOT_VERIFIED
import com.thkf.sentinelx.commons.STORAGE_ROOT
import com.thkf.sentinelx.commons.STORAGE_USERS
import com.thkf.sentinelx.commons.STORAGE_USER_PROFILE_IMAGE_NAME
import com.thkf.sentinelx.commons.VERIFIED
import com.thkf.sentinelx.commons.auth
import com.thkf.sentinelx.commons.doc
import com.thkf.sentinelx.commons.firestore
import com.thkf.sentinelx.commons.isOnline
import com.thkf.sentinelx.commons.uid
import com.thkf.sentinelx.extensions.Prefs
import com.thkf.sentinelx.extensions.logE
import com.thkf.sentinelx.extensions.logI
import com.thkf.sentinelx.extensions.toastLong
import kotlinx.android.synthetic.main.activity_user_profile.*
import kotlinx.android.synthetic.main.content_user_profile.*
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import java.io.ByteArrayOutputStream
import java.util.*


class UserProfileActivity : AppCompatActivity() {

    companion object {
        const val PERMISSION_REQUEST_CODE = 490
        const val CAMERA_IMAGE_REQUEST_CODE = 510
        const val GALLERY_IMAGE_REQUEST_CODE = 520

        const val MB_THRESHOLD = 1.0
        const val MB = 1000000.0
    }

    private var mSelectedImageBitmap: Bitmap? = null
    private var mBytes: ByteArray? = null
    private var mUploadImage = false

    private var permissionsGranted = false
    private var lat = 0.0
    private var lon = 0.0

    private val name: String by lazy {
        Prefs(this).get(NAME, "")
    }
    private val email: String by lazy {
        Prefs(this).get(EMAIL, "")
    }

    private val waitDialog: MaterialDialog by lazy {
        MaterialDialog.Builder(this)
                .progress(true, 0)
                .content("Please wait...")
                .cancelable(false)
                .theme(Theme.LIGHT)
                .build()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        launch(UI) {
            supportActionBar?.title = name
            tv_username.text = name
            tv_email.text = email
        }

        launch {
            val bundle = intent.extras

            if (bundle != null) {
                lat = bundle.getDouble(LAT, 0.0)
                lon = bundle.getDouble(LON, 0.0)
            } else if (savedInstanceState != null) {
                lat = savedInstanceState.getDouble(LAT, 0.0)
                lon = savedInstanceState.getDouble(LON, 0.0)
            }
            logI("onCreate: $lat, $lon")
        }

        fab.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                verifyStorageCameraPermissions()
            } else {
                selectImageDialog()
            }
        }

        password_reset_link.setOnClickListener {
            if (isOnline(this)) {
                auth().sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                toastLong("Password reset link send to your email address.")
                            } else {
                                toastLong("Something went wrong.")
                            }
                        }
            } else {
                toastLong("Pleas check your internet connection.")
            }
        }

    }

    private fun getAddress(): String {
        try {
            val geoCoder = Geocoder(this, Locale.getDefault())
            val address = geoCoder.getFromLocation(lat, lon, 1)
            if (address.size > 0) {
                val lines = address[0].maxAddressLineIndex
                var completeAddress = ""
                (0..lines).forEach {
                    completeAddress += address[0].getAddressLine(it)
                }
                return completeAddress
            }
        } catch (e: Exception) {
            logE("Error: ${e.stackTrace}")
        }
        return "Not Available"
    }

    private fun getLatLon(): String {
        return "$lat, $lon"
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        logI("onResume")
        super.onResume()
        tv_verified.text = if (Prefs(this).getBoolean(EMAIL_VERIFIED)) VERIFIED else NOT_VERIFIED
        launch(UI) {
            tv_coordinates.text = getLatLon()
            tv_address.text = getAddress()
        }

        if (!mUploadImage) {
            ImageLoader.getInstance().displayImage(Prefs(this@UserProfileActivity)
                    .get(IMAGE, ""), user_profile_image)
        }
    }

    override fun onBackPressed() {
        if (mUploadImage) {
            MaterialDialog.Builder(this)
                    .title("Save profile image")
                    .positiveText("Save")
                    .negativeText("Cancel")
                    .theme(Theme.LIGHT)
                    .onPositive { dialog, _ ->
                        dialog.dismiss()
                        // upload image to firestorage
                        waitDialog.show()

                        uploadImage()
//                        setResult(Activity.RESULT_OK, Intent())
//                        super.onBackPressed()
                    }
                    .onNegative { dialog, _ ->
                        dialog.dismiss()
                        super.onBackPressed()
                    }
                    .show()
        } else {
            super.onBackPressed()
        }
    }

    private fun uploadImage() {
        val storageRef = FirebaseStorage.getInstance()
                .reference
                .child("$STORAGE_ROOT/$STORAGE_USERS/${uid()}/$STORAGE_USER_PROFILE_IMAGE_NAME")

        val metaData = StorageMetadata.Builder()
                .setContentType("image/jpg")
                .setContentLanguage("en")
                .build()

        val uploadTask = storageRef.putBytes(mBytes!!, metaData)
        uploadTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
//                val downloadUrl = task.result
//                val downloadUrl = task.result.metadata?.reference?.downloadUrl
                val downloadUrl = task.result.metadata?.reference?.downloadUrl
                logI("Image Url: ${downloadUrl.toString()}")

                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    logI("Image Url: ${uri.toString()}")

                    firestore().document(doc(uid()))
                            .update(mapOf(IMAGE to uri.toString()))
                    Prefs(this).putString(IMAGE, uri.toString())
                    logI("Profile image url set in firestore.")
                }

            }

        }

        launch(UI) {
            delay(5000)
            waitDialog.dismiss()
            finish()
        }
    }

    override fun onSaveInstanceState(outState: Bundle?, outPersistentState: PersistableBundle?) {
        logI("onSaveInstanceState")
        outState?.putDouble(LAT, lat)
        outState?.putDouble(LON, lon)
        super.onSaveInstanceState(outState, outPersistentState)
        logI("onSaveInstanceState: $lat, $lon")
    }

    private fun selectImageFromCamera() {
        val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(takePicture, CAMERA_IMAGE_REQUEST_CODE)
    }

    private fun selectImageFromGallery() {
        val pickPhoto = Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(pickPhoto, GALLERY_IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            CAMERA_IMAGE_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val selectedImage = data?.extras?.get("data") as Bitmap
                    logI("Image: $selectedImage")
                    mUploadImage = true
                    mSelectedImageBitmap = selectedImage
                    user_profile_image.setImageBitmap(mSelectedImageBitmap)
                    compressedImageBitmap()
                } else {
                    toastLong("Something went wrong.")
                }
            }
            GALLERY_IMAGE_REQUEST_CODE -> {
                if (resultCode == Activity.RESULT_OK) {
                    val selectedImage = data?.data
                    logI("Image: $selectedImage")
                    mUploadImage = true
                    mSelectedImageBitmap = decodeImageURI(selectedImage)
                    user_profile_image.setImageBitmap(mSelectedImageBitmap)
                    compressedImageBitmap()
                } else {
                    toastLong("Something went wrong.")
                }
            }
        }
    }

    private fun compressedImageBitmap() {
        launch {
            var bytes: ByteArray? = null
            for (i in 1..10) {
                bytes = getBytesFromBitmap(mSelectedImageBitmap!!, 100 / i)
                logI("megabytes: (" + (11 - i) + "0%) " + bytes.size / MB + " MB")
                if (bytes.size / MB < MB_THRESHOLD) {
                    break
                }
            }
            mBytes = bytes
        }
    }

    private fun getBytesFromBitmap(bitmap: Bitmap, quality: Int): ByteArray {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, stream)
        return stream.toByteArray()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    logI("Permissions granted.")
                    selectImageDialog()
                }
            }
        }
    }

    private fun decodeImageURI(selectedImage: Uri?): Bitmap? {
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

        val cursor = contentResolver.query(selectedImage, filePathColumn, null, null, null)
        cursor!!.moveToFirst()

        val columnIndex = cursor.getColumnIndex(filePathColumn[0])
        val filePath = cursor.getString(columnIndex)
        cursor.close()

        return BitmapFactory.decodeFile(filePath)
    }

    private fun verifyStorageCameraPermissions() {
        val permissions = arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA)

        if (ContextCompat.checkSelfPermission(this.applicationContext,
                        permissions[0]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.applicationContext,
                        permissions[1]) == PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(this.applicationContext,
                        permissions[2]) == PackageManager.PERMISSION_GRANTED) {
            permissionsGranted = true
            selectImageDialog()
        } else {
            ActivityCompat.requestPermissions(this, permissions, PERMISSION_REQUEST_CODE)
        }

    }

    private fun selectImageDialog() {
        val dialog = MaterialDialog.Builder(this)
                .title("Select image from")
                .customView(R.layout.image_picker_options_dialog, false)
                .negativeText("Cancel")
                .onNegative { dialog, _ ->
                    dialog.dismiss()
                }
                .theme(Theme.LIGHT)
                .build()
        val camera = dialog.customView?.findViewById<LinearLayout>(R.id.camera)
        val gallery = dialog.customView?.findViewById<LinearLayout>(R.id.gallery)

        camera?.setOnClickListener {
            logI("Camera clicked")
            dialog.dismiss()
            selectImageFromCamera()
        }

        gallery?.setOnClickListener {
            logI("Gallery clicked")
            dialog.dismiss()
            selectImageFromGallery()
        }

        dialog.show()
    }
}

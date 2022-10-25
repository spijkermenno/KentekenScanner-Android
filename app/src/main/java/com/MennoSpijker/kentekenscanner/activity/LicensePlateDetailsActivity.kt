package com.MennoSpijker.kentekenscanner.activity

import android.Manifest
import android.R.attr.data
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.webkit.MimeTypeMap
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.room.util.CursorUtil.getColumnIndexOrThrow
import com.MennoSpijker.kentekenscanner.R
import com.MennoSpijker.kentekenscanner.Util.FontManager
import com.MennoSpijker.kentekenscanner.Util.KentekenHandler
import com.MennoSpijker.kentekenscanner.Util.Utils
import com.MennoSpijker.kentekenscanner.adapter.LicensePlateDetailsAdapter
import com.MennoSpijker.kentekenscanner.models.LicensePlateDetails
import com.MennoSpijker.kentekenscanner.repositories.LicensePlateRepository
import com.MennoSpijker.kentekenscanner.viewmodel.LicensePlateViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File


open class LicensePlateDetailsActivity : AppCompatActivity() {
    var licenseplateID: Int = 0
    var licenseplate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_license_plate_details)

        licenseplateID = intent.getIntExtra("licenseplateID", 0)
        licenseplate = intent.getStringExtra("licenseplate") ?: ""

        val licenseplateTextView = findViewById<TextView>(R.id.kenteken)
        val detailsRecyclerView = findViewById<RecyclerView>(R.id.detailsRecyclerView)

        val pictureButton = findViewById<TextView>(R.id.pictureButton)
        val openCameraButton = findViewById<Button>(R.id.camera)

        val iconFont: Typeface = FontManager.getTypeface(
            this, FontManager.FONTAWESOME
        )

        openCameraButton.typeface = iconFont;
        openCameraButton.textSize = 20F;

        openCameraButton.setOnClickListener {
            openCamera()
        }

        pictureButton.setOnClickListener {
            openImagePicker()
        }

        licenseplateTextView.text = KentekenHandler.formatLicensePlate(licenseplate)
        // TODO set adapter of $detailsRecyclerView etc


        val viewModel = LicensePlateViewModel(this)

        val adapter = LicensePlateDetailsAdapter()
        val linearLayoutManager = LinearLayoutManager(this)

        detailsRecyclerView.adapter = adapter
        detailsRecyclerView.layoutManager = linearLayoutManager

        viewModel.list.observe(this) {
            Log.d("TAG", "onCreate: ${it.first().licensePlate}")

            var imagesString = ""
            it.first().images.forEach { image ->
                imagesString += "$image,"
            }

            it.first().details.add(0, LicensePlateDetails("imageURL", imagesString))
            adapter.submit(it.first().details)
        }

        viewModel.getLicensePlatesWithUUIDAndLicenseplateID(Utils.getUUID(this), licenseplateID)
    }

    private fun openCamera() {
        if (!checkPermissionForReadExtertalStorage()) {
            requestPermissionForReadExtertalStorage()
        } else {

            val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(takePicture, 0)
        }
    }

    private fun openImagePicker() {
        if (!checkPermissionForReadExtertalStorage()) {
            requestPermissionForReadExtertalStorage()
        } else {

            val pickPhoto = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            )
            startActivityForResult(pickPhoto, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, imageReturnedIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent)
        if (resultCode == RESULT_OK) {
            val selectedImage = imageReturnedIntent?.data
            val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

            selectedImage?.let {
                val cursor: Cursor? = contentResolver.query(
                    selectedImage,
                    filePathColumn, null, null, null
                )
                cursor?.moveToFirst()
                val columnIndex: Int? = cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                cursor?.moveToFirst()


                columnIndex?.let {
                    val picturePath: String = cursor.getString(it)
                    uploadImage(picturePath)
                    cursor.close()
                }
            }
        }
    }

    private val READ_STORAGE_PERMISSION_REQUEST_CODE = 41

    private fun checkPermissionForReadExtertalStorage(): Boolean {
        val result: Int = checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        return result == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermissionForReadExtertalStorage() {
        try {
            ActivityCompat.requestPermissions(
                this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                READ_STORAGE_PERMISSION_REQUEST_CODE
            )
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private fun uploadImage(uri: String) {
        uri.let {
            val file = File(uri)

            val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

            // MultipartBody.Part is used to send also the actual file name
            val body = MultipartBody.Part.createFormData("afbeeldingAuto", file.name, requestFile)

            LicensePlateRepository.uploadFile(licenseplate, body) {
                if (it) {
                    Toast.makeText(
                        this, "Afbeelding geupload, deze zal zichtbaar zijn na goedkeuring.",
                        Toast.LENGTH_LONG
                    ).show()
                } else {
                    Toast.makeText(
                        this, "Er is iets fout gegaan, probeer het opnieuw.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }
}
package com.MennoSpijker.kentekenscanner.activity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.MennoSpijker.kentekenscanner.BuildConfig
import com.MennoSpijker.kentekenscanner.Camera.OcrCaptureActivity
import com.MennoSpijker.kentekenscanner.Factory.NotificationFactory
import com.MennoSpijker.kentekenscanner.R
import com.MennoSpijker.kentekenscanner.Util.FontManager
import com.MennoSpijker.kentekenscanner.Util.KentekenHandler
import com.MennoSpijker.kentekenscanner.Util.Utils
import com.MennoSpijker.kentekenscanner.adapter.LicensePlateDetailsAdapter
import com.MennoSpijker.kentekenscanner.models.LicensePlateDetails
import com.MennoSpijker.kentekenscanner.repositories.LicensePlateRepository
import com.MennoSpijker.kentekenscanner.viewmodel.LicensePlateViewModel
import com.google.firebase.analytics.FirebaseAnalytics
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import org.json.JSONException
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


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

            val apkDate = it.first().details.find { licensePlateDetails ->
                licensePlateDetails.key.equals("vervaldatum_apk")
            }?.content

            apkDate?.let { date ->
                setFabOnClick(date);
            }

            var imagesString = ""
            it.first().images.forEach { image ->
                imagesString += "$image,"
            }

            it.first().details.add(0, LicensePlateDetails("imageURL", imagesString))
            adapter.submit(it.first().details)
        }

        viewModel.getLicensePlatesWithUUIDAndLicenseplateID(Utils.getUUID(this), licenseplateID)
    }

    private fun setFabOnClick(apkDate: String) {
        val fab = findViewById<View>(R.id.fab)

        fab.setOnClickListener { view ->
            try {
                val notificationText =
                    "Pas op! De APK van jouw voertuig met het kenteken  ${
                        KentekenHandler.formatLicensePlate(
                            licenseplate
                        )
                    } vervalt over 45 dagen. (Heb je de APK al verlengd? Dan kun je dit bericht negeren!)"
                val bundle = Bundle()
                bundle.putString(
                    FirebaseAnalytics.Param.ITEM_NAME,
                    KentekenHandler.formatLicensePlate(licenseplate)
                )

                NotificationFactory(this).planNotification(
                    getString(R.string.APK_ALERT),
                    notificationText,
                    licenseplate,
                    NotificationFactory.calculateNotifcationTime(
                        apkDate
                    )
                )
                Toast.makeText(this, R.string.notifcationActivated, Toast.LENGTH_SHORT).show()
            } catch (e: JSONException) {
                e.printStackTrace()
            }
        }
    }

    var mCurrentPhotoPath: String? = null

    @SuppressLint("SimpleDateFormat")
    @Throws(IOException::class)
    open fun createImageFile(): File? {
        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val imageFileName = "JPEG_" + timeStamp + "_"
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val image = File.createTempFile(
            imageFileName,  /* prefix */
            ".jpg",  /* suffix */
            storageDir /* directory */
        )

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.absolutePath
        return image
    }

    private fun openCamera() {
        if (!checkPermissionForReadExtertalStorage()) {
            requestPermissionForReadExtertalStorage()
        } else {

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA),
                    OcrCaptureActivity.RC_HANDLE_CAMERA_PERM)
            }

            val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

            takePictureIntent.putExtra("crop", "true");
            takePictureIntent.putExtra("outputX", 150);
            takePictureIntent.putExtra("outputY", 150);
            takePictureIntent.putExtra("aspectX", 1);
            takePictureIntent.putExtra("aspectY", 1);
            takePictureIntent.putExtra("scale", true);

            // Ensure that there's a camera activity to handle the intent
            if (takePictureIntent.resolveActivity(packageManager) != null) {
                // Create the File where the photo should go
                var photoFile: File? = null
                try {
                    photoFile = createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File
                }
                // Continue only if the File was successfully created
                if (photoFile != null) {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        BuildConfig.APPLICATION_ID + ".provider",
                        photoFile
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, 0)
                }
            }

//            val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//            startActivityForResult(takePicture, 0)
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

            pickPhoto.putExtra("crop", "true");
            pickPhoto.putExtra("outputX", 600);
            pickPhoto.putExtra("outputY", 450);
            pickPhoto.putExtra("aspectX", 4);
            pickPhoto.putExtra("aspectY", 3);
            pickPhoto.putExtra("scale", true);

            startActivityForResult(pickPhoto, 1)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, imageReturnedIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent)

        val selectedImage: Uri?
        val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)

        if (resultCode == RESULT_OK) {
            Log.d("TAG", "onActivityResult: $requestCode")
            when (requestCode) {
                0 -> {
                    selectedImage = Uri.parse(mCurrentPhotoPath)
                    uploadImage(selectedImage.toString())
                }
                1 -> {
                    selectedImage = imageReturnedIntent?.data

                    Log.d("TAG", "onActivityResult: $selectedImage")

                    selectedImage?.let {
                        val cursor: Cursor? = contentResolver.query(
                            selectedImage,
                            filePathColumn, null, null, null
                        )
                        cursor?.moveToFirst()
                        val columnIndex: Int? =
                            cursor?.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                        cursor?.moveToFirst()


                        columnIndex?.let {
                            val picturePath: String = cursor.getString(it)
                            uploadImage(picturePath)
                            cursor.close()
                        }
                    }
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
            Log.d("TAG", "uploadImage: uri $uri")
            val file = File(uri)
            Log.d("TAG", "uploadImage: file $uri")

            val mediatype = "multipart/form-data".toMediaTypeOrNull()
            Log.d("TAG", "uploadImage: mediatype $mediatype")

            val requestFile = file.asRequestBody(mediatype)
            Log.d("TAG", "uploadImage: file req $requestFile")

            // MultipartBody.Part is used to send also the actual file name
            val body =
                MultipartBody.Part.createFormData("afbeeldingAuto", file.name, requestFile)
            Log.d("TAG", "uploadImage: body $body")

            LicensePlateRepository.uploadFile(licenseplate, body) {
                if (it == 200) {
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
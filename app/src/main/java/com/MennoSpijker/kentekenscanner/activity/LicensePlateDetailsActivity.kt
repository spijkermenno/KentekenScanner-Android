package com.MennoSpijker.kentekenscanner.activity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.recyclerview.widget.RecyclerView
import com.MennoSpijker.kentekenscanner.R
import com.MennoSpijker.kentekenscanner.Util.KentekenHandler
import com.MennoSpijker.kentekenscanner.Util.Utils
import com.MennoSpijker.kentekenscanner.repositories.LicensePlateRepository
import com.MennoSpijker.kentekenscanner.responses.RecyclerViewItem
import com.MennoSpijker.kentekenscanner.viewmodel.LicensePlateViewModel
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class LicensePlateDetailsActivity : AppCompatActivity() {
    var licenseplateID: Int = 0
    var licenseplate: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_license_plate_details)

        licenseplateID = intent.getIntExtra("licenseplateID", 0)
        licenseplate = intent.getStringExtra("licenseplate") ?: ""

        val viewModel = LicensePlateViewModel(this)

        viewModel.getLicensePlatesWithUUIDAndLicenseplateID(Utils.getUUID(this), licenseplateID)

        val licenseplateTextView = findViewById<TextView>(R.id.kenteken)
        val detailsRecyclerView = findViewById<RecyclerView>(R.id.detailsRecyclerView)

        licenseplateTextView.text = KentekenHandler.formatLicensePlate(licenseplate)
        // TODO set adapter of $detailsRecyclerView etc
    }

    fun openImageDialog() {
        registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri ->
            val file = File(uri.toString())

            val requestFile = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())

            // MultipartBody.Part is used to send also the actual file name
            val body = MultipartBody.Part.createFormData("afbeeldingAuto", file.name, requestFile)

            LicensePlateRepository.uploadFile(licenseplate , body)
        }
    }
}
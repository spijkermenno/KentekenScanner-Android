package com.MennoSpijker.kentekenscanner.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.MennoSpijker.kentekenscanner.Camera.OcrCaptureActivity
import com.MennoSpijker.kentekenscanner.R
import com.MennoSpijker.kentekenscanner.Util.FileHandling
import com.MennoSpijker.kentekenscanner.Util.FontManager
import com.MennoSpijker.kentekenscanner.Util.KentekenHandler.Companion.formatLicensePlate
import com.MennoSpijker.kentekenscanner.Util.KentekenHandler.Companion.getSideCodeOfLicensePlate
import com.MennoSpijker.kentekenscanner.Util.KentekenHandler.Companion.isLicensePlateValid
import com.MennoSpijker.kentekenscanner.Util.Utils
import com.MennoSpijker.kentekenscanner.adapter.LicensePlateAdapter
import com.MennoSpijker.kentekenscanner.viewmodel.LicensePlateViewModel
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*
import java.util.function.Consumer

class MainActivity : AppCompatActivity() {
    private var bundle: Bundle? = Bundle()

    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var resultRecyclerView: RecyclerView
    private lateinit var licensePlateTextEdit: EditText
    private lateinit var openCameraButton: Button

    @SuppressLint("HardwareIds")
    lateinit var uuid: String;

    private val adapter = LicensePlateAdapter()
    private val viewModel = LicensePlateViewModel(this)

    var buttons = ArrayList<Button?>()

    private fun bind() {
        resultRecyclerView = findViewById(R.id.resultRecyclerView)
        licensePlateTextEdit = findViewById(R.id.kenteken)
        openCameraButton = findViewById(R.id.camera)

        val iconFont: Typeface = FontManager.getTypeface(
            this, FontManager.FONTAWESOME
        )

        openCameraButton.typeface = iconFont;
        openCameraButton.textSize = 20F;

        initLicensePlateHandling()
        setOnClickListener()
    }

    private fun setOnClickListener() {
        openCameraButton.setOnClickListener {
            startCameraIntent()
        }
    }

    private fun initLicensePlateHandling() {
        uuid = Utils.getUUID(this)
        FirebaseAnalytics.getInstance(this).setUserId(uuid)

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL

        resultRecyclerView.adapter = adapter
        resultRecyclerView.layoutManager = linearLayoutManager

        adapter.showLoader()

        viewModel.list.observe(this) {
            adapter.submit(it)
            adapter.clearLoader()
        }
    }

    private fun hideKeyboard() {
        this.currentFocus?.let { view ->
            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bind()

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task: Task<String> ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
            }

        buttons.forEach(Consumer { button: Button? ->
            button!!.typeface = FontManager.getTypeface(this, FontManager.FONTAWESOME)
            button.textSize = 20f
        })
    }

    override fun onStart() {
        super.onStart()
        val context: Context = this

        viewModel.getLicensePlateForUUID(uuid)

        // Run the setup ASYNC for faster first render.
        Thread {
            // Notifications cleanup
            FileHandling(context).cleanUpNotificationList()
            val licencePlateTextField = findViewById<EditText>(R.id.kenteken)
            licencePlateTextField.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    charSequence: CharSequence,
                    i: Int,
                    i1: Int,
                    i2: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    val licencePlate = licencePlateTextField.text.toString()

                    if (licencePlate.length == 6) {
                        val formattedLicencePlate = formatLicensePlate(licencePlate)
                        if (licencePlate != formattedLicencePlate) {
                            licencePlateTextField.setText(formattedLicencePlate)
                            if (isLicensePlateValid(licencePlateTextField.text.toString())) {
                                // run API call
                                val licensePlate = licencePlateTextField.text.toString().uppercase()
                                hideKeyboard()

                                viewModel.addLicensePlateToUUID(licensePlate, uuid)
                                adapter.showLoader()

                            }
                        }
                    }
                }

                override fun afterTextChanged(editable: Editable) {
                }
            })
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task: Task<String?> ->
                    if (!task.isSuccessful) {
                        return@addOnCompleteListener
                    }

                    // Get new FCM registration token
                    val token = task.result

                    // Log
                    Log.d("FCM Token", token!!)
                }
            licencePlateTextField.setOnKeyListener { v: View?, keyCode: Int, event: KeyEvent ->
                if (event.action == KeyEvent.ACTION_DOWN) {
                    when (keyCode) {
                        KeyEvent.KEYCODE_ENTER -> {
                            val licensePlate = licencePlateTextField.text.toString().uppercase()

                            viewModel.addLicensePlateToUUID(licensePlate, uuid)

                            return@setOnKeyListener true
                        }
                        KeyEvent.KEYCODE_DEL -> {
                            var text = licencePlateTextField.text.toString()
                            text = text.replace("-", "")
                            var newText = text
                            if (text.isNotEmpty()) {
                                newText = text.substring(0, text.length - 1)
                            }
                            licencePlateTextField.setText(newText)
                            licencePlateTextField.setSelection(licencePlateTextField.text.length)
                            return@setOnKeyListener true
                        }
                        else -> {}
                    }
                }
                val sideCode = getSideCodeOfLicensePlate(
                    licencePlateTextField.text.toString().uppercase(
                        Locale.getDefault()
                    )
                )
                if (sideCode != -1 && sideCode != -2) {
                    val licensePlate = licencePlateTextField.text.toString().uppercase()

                    viewModel.addLicensePlateToUUID(licensePlate, uuid)
                    return@setOnKeyListener true
                }
                false
            }
            if (intent.getStringExtra("kenteken") != null) {
                val licensePlate = intent.getStringExtra("kenteken").toString().uppercase()

                viewModel.addLicensePlateToUUID(licensePlate, uuid)
                val licencePlateTextView = licencePlateTextField as TextView
                licencePlateTextView.text = formatLicensePlate(licensePlate)
            }
        }.start()
    }

    private fun startCameraIntent() {
        try {
            val intent = Intent(this, OcrCaptureActivity::class.java)
            intent.putExtra(OcrCaptureActivity.AutoFocus, true)
            intent.putExtra(OcrCaptureActivity.UseFlash, false)
            startActivityForResult(intent, RC_OCR_CAPTURE)
        } catch (e: Exception) {
            e.printStackTrace()
            e.message
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_OCR_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                val text = data?.getStringExtra(OcrCaptureActivity.TextBlockObject)
                val editText = findViewById<EditText>(R.id.kenteken)

                text?.let {
                    if (isLicensePlateValid(text)) {
                        adapter.showLoader()
                        viewModel.addLicensePlateToUUID(text, uuid)
                        val licencePlateTextView = editText as TextView
                        licencePlateTextView.text = formatLicensePlate(text)
                    }
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val RC_OCR_CAPTURE = 9003
    }
}
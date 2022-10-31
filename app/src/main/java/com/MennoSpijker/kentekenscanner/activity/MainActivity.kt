package com.MennoSpijker.kentekenscanner.activity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.os.Environment
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
import androidx.core.app.ActivityCompat
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

    lateinit var firebaseAnalytics: FirebaseAnalytics
    lateinit var resultRecyclerView: RecyclerView
    lateinit var licensePlateTextEdit: EditText
    lateinit var openCameraButton: Button

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

    fun setOnClickListener() {
        openCameraButton.setOnClickListener {
            startCameraIntent()
        }
    }

    private fun initLicensePlateHandling() {
        uuid = Utils.getUUID(this)

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
            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(view.windowToken, 0)
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

        Log.e(TAG, "onCreate: ${Utils.getUUID(this)}")
    }

    override fun onStart() {
        super.onStart()
        val context: Context = this

        viewModel.getLicensePlateForUUID(uuid)

        // Run the setup ASYNC for faster first render.
        Thread {
            // Notifications cleanup
            FileHandling(context).cleanUpNotificationList()
            val kentekenTextField = findViewById<EditText>(R.id.kenteken)
            kentekenTextField.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    charSequence: CharSequence,
                    i: Int,
                    i1: Int,
                    i2: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    val kenteken = kentekenTextField.text.toString()

                    if (kenteken.length == 6) {
                        val formatedKenteken = formatLicensePlate(kenteken)
                        if (kenteken != formatedKenteken) {
                            kentekenTextField.setText(formatedKenteken)
                            if (isLicensePlateValid(kentekenTextField.text.toString())) {
                                // run API call
                                val licensePlate = kentekenTextField.text.toString().uppercase()
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
            kentekenTextField.setOnKeyListener { v: View?, keyCode: Int, event: KeyEvent ->
                if (event.action == KeyEvent.ACTION_DOWN) {
                    when (keyCode) {
                        KeyEvent.KEYCODE_ENTER -> {
                            var licensePlate = kentekenTextField.text.toString().uppercase()

                            viewModel.addLicensePlateToUUID(licensePlate, uuid)

                            return@setOnKeyListener true
                        }
                        KeyEvent.KEYCODE_DEL -> {
                            var text = kentekenTextField.text.toString()
                            text = text.replace("-", "")
                            var newText = text
                            if (text.isNotEmpty()) {
                                newText = text.substring(0, text.length - 1)
                            }
                            kentekenTextField.setText(newText)
                            kentekenTextField.setSelection(kentekenTextField.text.length)
                            return@setOnKeyListener true
                        }
                        else -> {}
                    }
                }
                val sideCode = getSideCodeOfLicensePlate(
                    kentekenTextField.text.toString().uppercase(
                        Locale.getDefault()
                    )
                )
                if (sideCode != -1 && sideCode != -2) {
                    var licensePlate = kentekenTextField.text.toString().uppercase()

                    viewModel.addLicensePlateToUUID(licensePlate, uuid)
                    return@setOnKeyListener true
                }
                false
            }
            if (intent.getStringExtra("kenteken") != null) {
                var licensePlate = intent.getStringExtra("kenteken").toString().uppercase()

                viewModel.addLicensePlateToUUID(licensePlate, uuid)
                val kentekenTextView = kentekenTextField as TextView
                kentekenTextView.text = formatLicensePlate(licensePlate)
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
                val textfield = findViewById<EditText>(R.id.kenteken)

                text?.let {
                    if (isLicensePlateValid(text)) {
                        adapter.showLoader()
                        viewModel.addLicensePlateToUUID(text, uuid)
                        val kentekenTextView = textfield as TextView
                        kentekenTextView.text = formatLicensePlate(text)
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
package com.MennoSpijker.kentekenscanner.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.MennoSpijker.kentekenscanner.ConnectionDetector
import com.MennoSpijker.kentekenscanner.FontManager
import com.MennoSpijker.kentekenscanner.OcrCaptureActivity
import com.MennoSpijker.kentekenscanner.R
import com.MennoSpijker.kentekenscanner.Util.FileHandling
import com.MennoSpijker.kentekenscanner.view.KentekenHandler.Companion.formatLicensePlate
import com.MennoSpijker.kentekenscanner.view.KentekenHandler.Companion.getSideCodeOfLicensePlate
import com.MennoSpijker.kentekenscanner.view.KentekenHandler.Companion.isLicensePlateValid
import com.google.android.gms.ads.*
import com.google.android.gms.ads.initialization.InitializationStatus
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.tasks.Task
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.messaging.FirebaseMessaging
import java.util.*
import java.util.function.Consumer

class MainActivity : AppCompatActivity() {
    private var bundle: Bundle? = Bundle()

    lateinit var firebaseAnalytics: FirebaseAnalytics
    lateinit var showHistoryButton: Button
    lateinit var openCameraButton: Button
    lateinit var showFavoritesButton: Button
    lateinit var showAlertsButton: Button
    lateinit var resultRecyclerView: RecyclerView
    var Khandler: KentekenHandler = KentekenHandler(this)
    lateinit var kentekenTextField: EditText

    private val adapter = LicensePlateAdapter()
    private val viewModel = LicensePlateViewModel()

    var buttons = ArrayList<Button?>()

    private fun bind() {
        showHistoryButton = findViewById(R.id.showHistory)
        openCameraButton = findViewById(R.id.camera)
        showFavoritesButton = findViewById(R.id.showFavorites)
        showAlertsButton = findViewById(R.id.showAlerts)
        resultRecyclerView = findViewById(R.id.resultRecyclerView)
        kentekenTextField = findViewById(R.id.kenteken)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        val iconFont = FontManager.getTypeface(
            applicationContext, FontManager.FONTAWESOME
        )

        FontManager.markAsIconContainer(findViewById(R.id.icons_container), iconFont)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bind()

        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.orientation = LinearLayoutManager.VERTICAL

        resultRecyclerView.adapter = adapter
        resultRecyclerView.layoutManager = linearLayoutManager

        viewModel.list.observe(this) {
            adapter.submit(it)
        }

        kentekenTextField.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                if (kentekenTextField.text.length == 6) {
                    val formattedLicencePlate =
                        formatLicensePlate(kentekenTextField.text.toString())
                    if (kentekenTextField.text.toString() != formattedLicencePlate) {
                        kentekenTextField.setText(formattedLicencePlate)
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {}
        })

        buttons.add(showHistoryButton)
        buttons.add(openCameraButton)
        buttons.add(showFavoritesButton)
        buttons.add(showAlertsButton)

        val connectionDetector = ConnectionDetector(this)

        FirebaseMessaging.getInstance().token
            .addOnCompleteListener { task: Task<String> ->
                if (!task.isSuccessful) {
                    Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                    return@addOnCompleteListener
                }

                // Get new FCM registration token
                val token = task.result
                Log.d(TAG, "onCreate: TOKEN: $token")
            }

        buttons.forEach(Consumer { button: Button? ->
            button!!.typeface = FontManager.getTypeface(this, FontManager.FONTAWESOME)
            button.textSize = 20f
        })

        showHistoryButton.setOnClickListener { Khandler.openRecent() }
        openCameraButton.setOnClickListener { startCameraIntent() }
        showFavoritesButton.setOnClickListener { Khandler.openSaved() }
        showAlertsButton.setOnClickListener { Khandler.openNotifications() }

        getAds()
    }

    override fun onResume() {
        super.onResume()
        // Must be run on main UI thread...
    }

    override fun onStart() {
        super.onStart()
        val context: Context = this

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
                    Log.d(TAG, "beforeTextChanged: $charSequence")
                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    val kenteken = kentekenTextField.text.toString()

                    // check if kenteken is 6 characters long
                    if (kenteken.length == 6) {
                        val formatedKenteken = formatLicensePlate(kenteken)
                        if (kenteken != formatedKenteken) {
                            // Set formatted text in kentekenField
                            kentekenTextField.setText(formatedKenteken)
                            // check if kenteken is valid
                            if (isLicensePlateValid(kentekenTextField.text.toString())) {
                                // run API call
                                var licensePlate = kentekenTextField.text.toString().uppercase()

                                licensePlate = licensePlate.replace("-", "")
                                licensePlate = licensePlate.replace(" ", "")
                                licensePlate = licensePlate.replace("\n", "")

                                viewModel.getLicensePlateDetails(licensePlate)
                                //Khandler.run(kentekenTextField)
                            }
                        }
                    }
                }

                override fun afterTextChanged(editable: Editable) {
                    Log.d(TAG, "afterTextChanged: $editable")
                }
            })
            FirebaseMessaging.getInstance().token
                .addOnCompleteListener { task: Task<String?> ->
                    if (!task.isSuccessful) {
                        Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                        return@addOnCompleteListener
                    }

                    // Get new FCM registration token
                    val token = task.result

                    // Log
                    Log.d("FCM Token", token!!)
                }
            kentekenTextField.setOnKeyListener { v: View?, keyCode: Int, event: KeyEvent ->
                Log.d(TAG, "onKey: $keyCode")
                if (event.action == KeyEvent.ACTION_DOWN) {
                    when (keyCode) {
                        KeyEvent.KEYCODE_ENTER -> {
                            var licensePlate = kentekenTextField.text.toString().uppercase()

                            licensePlate = licensePlate.replace("-", "")
                            licensePlate = licensePlate.replace(" ", "")
                            licensePlate = licensePlate.replace("\n", "")

                            viewModel.getLicensePlateDetails(licensePlate)

                            //Khandler.run(kentekenTextField)
                            return@setOnKeyListener true
                        }
                        KeyEvent.KEYCODE_DEL -> {
                            Log.d(TAG, "onKey: KEY EVENT")
                            var text = kentekenTextField.text.toString()
                            text = text.replace("-", "")
                            var newText = text
                            if (text.length > 0) {
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

                    licensePlate = licensePlate.replace("-", "")
                    licensePlate = licensePlate.replace(" ", "")
                    licensePlate = licensePlate.replace("\n", "")

                    viewModel.getLicensePlateDetails(licensePlate)
                    //Khandler.run(kentekenTextField)
                    return@setOnKeyListener true
                }
                false
            }
            if (intent.getStringExtra("kenteken") != null) {
                var licensePlate = intent.getStringExtra("kenteken").toString().uppercase()

                licensePlate = licensePlate.replace("-", "")
                licensePlate = licensePlate.replace(" ", "")
                licensePlate = licensePlate.replace("\n", "")

                viewModel.getLicensePlateDetails(licensePlate)
                val kentekenTextView = kentekenTextField as TextView
                kentekenTextView.text = formatLicensePlate(licensePlate)

                //Khandler.runCamera(intent.getStringExtra("kenteken")!!, kentekenTextField)
            }
        }.start()
    }

    // Code to be executed when the user is about to return
    // to the app after tapping on an ad.
    // Code to be executed when an ad opens an overlay that
    // covers the screen.
    private fun getAds() {
        MobileAds.initialize(this) { initializationStatus: InitializationStatus? -> }
        try {
            val adLayout = findViewById<RelativeLayout>(R.id.adView)
            val advertisementView = AdView(this)
            val rd = Random()
            if (rd.nextBoolean()) {
                advertisementView.adUnitId = "ca-app-pub-4928043878967484/2205259265"
                advertisementView.adSize = AdSize.LARGE_BANNER
                firebaseAnalytics.setUserProperty("banner_size", "LARGE_BANNER")
            } else {
                advertisementView.adUnitId = "ca-app-pub-4928043878967484/5146910390"
                advertisementView.adSize = AdSize.BANNER
                firebaseAnalytics.setUserProperty("banner_size", "SMALL_BANNER")
            }
            adLayout.addView(advertisementView)
            advertisementView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    bundle = Bundle()
                    firebaseAnalytics.logEvent("Ad_loaded", bundle)
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    bundle = Bundle()
                    bundle!!.putString("Message", adError.message)
                    firebaseAnalytics.logEvent("Ad_error", bundle)
                }

                override fun onAdOpened() {
                    // Code to be executed when an ad opens an overlay that
                    // covers the screen.
                }

                override fun onAdClicked() {
                    bundle = Bundle()
                    firebaseAnalytics.logEvent("AD_CLICK", bundle)
                }

                override fun onAdClosed() {
                    // Code to be executed when the user is about to return
                    // to the app after tapping on an ad.
                }
            }
            val adRequest = AdRequest.Builder().build()
            advertisementView.loadAd(adRequest)
        } catch (e: Exception) {
            Log.d(TAG, "getAds: ERROR")
            e.printStackTrace()
        }
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

                var licensePlate = intent.getStringExtra("kenteken").toString().uppercase()

                licensePlate = licensePlate.replace("-", "")
                licensePlate = licensePlate.replace(" ", "")
                licensePlate = licensePlate.replace("\n", "")

                viewModel.getLicensePlateDetails(licensePlate)
                val kentekenTextView = textfield as TextView
                kentekenTextView.text = formatLicensePlate(licensePlate)

                //Khandler.runCamera(text!!, textfield)
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
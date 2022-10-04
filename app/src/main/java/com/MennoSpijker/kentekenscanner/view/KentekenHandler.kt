package com.MennoSpijker.kentekenscanner.view

import com.MennoSpijker.kentekenscanner.ConnectionDetector
import com.MennoSpijker.kentekenscanner.Factory.LicencePlateDataFactory
import android.os.Bundle
import android.annotation.SuppressLint
import android.content.Context
import com.MennoSpijker.kentekenscanner.R
import com.google.firebase.analytics.FirebaseAnalytics
import org.json.JSONObject
import com.MennoSpijker.kentekenscanner.Util.FileHandling
import kotlin.Throws
import org.json.JSONException
import org.json.JSONArray
import android.util.TypedValue
import android.graphics.Typeface
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.*
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class KentekenHandler(
    private val context: MainActivity,
    private val licensePlateHolder: TextView
) {
    fun saveRecentKenteken(kenteken: String?) {
        val otherKentekens = FileHandling(context).recentKenteken
        val wantedFormat = SimpleDateFormat("dd/MM/yyyy", Locale.ENGLISH)
        val date = wantedFormat.format(Date())
        FileHandling(context).writeToFileOnDate(RecentKentekensFile, kenteken, date, otherKentekens)
    }

    fun saveFavoriteKenteken(kenteken: String?) {
        val otherKentekens = FileHandling(context).savedKentekens
        context.firebaseAnalytics.setUserProperty("kenteken", kenteken)
        FileHandling(context).writeToFile(SavedKentekensFile, kenteken, otherKentekens)
    }

    @Throws(JSONException::class)
    fun deleteFavoriteKenteken(kenteken: String) {
        val otherKentekens = FileHandling(context).savedKentekens
        context.firebaseAnalytics.setUserProperty("kenteken", kenteken)
        val temp = JSONObject()
        temp.put("cars", JSONArray())
        for (i in 0 until otherKentekens.getJSONArray("cars").length()) {
            if (otherKentekens.getJSONArray("cars").getString(i) != kenteken) {
                temp.getJSONArray("cars").put(otherKentekens.getJSONArray("cars").getString(i))
            }
        }
        Log.d(TAG, "deleteFavoriteKenteken: $temp")
        FileHandling(context).writeToFile(SavedKentekensFile, temp)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun openRecent() {
        licensePlateHolder.text = ""
        licensePlateHolder.clearFocus()
        val inputManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        result.removeAllViews()
        val scale = context.resources.displayMetrics.density
        val width = (283 * scale + 0.5f).toInt()
        val height = (75 * scale + 0.5f).toInt()
        try {
            val recents = FileHandling(context).recentKenteken
            val lin = LinearLayout(context)
            lin.orientation = LinearLayout.VERTICAL
            val iterator = recents.keys()
            while (iterator.hasNext()) {
                val key = iterator.next()
                val values = recents.getJSONArray(key)
                val dateView = TextView(context)
                dateView.text = key
                dateView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                lin.addView(dateView)
                for (i in 0 until values.length()) {
                    var recent = values.getString(i)
                    recent = recent.replace("/", "")
                    val line = Button(context)
                    line.text = formatLicensePlate(recent)
                    val finalRecent = recent
                    line.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    line.setOnClickListener { v: View? ->
                        runCamera(
                            finalRecent,
                            licensePlateHolder
                        )
                    }
                    line.background = context.getDrawable(R.drawable.kentekenplaat3)
                    val params = LinearLayout.LayoutParams(
                        width,
                        height
                    )
                    params.setMargins(0, 10, 0, 10)
                    params.gravity = 17
                    line.layoutParams = params
                    line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
                    line.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    val left = (45 * scale + 0.5f).toInt()
                    val right = (10 * scale + 0.5f).toInt()
                    val top = (0 * scale + 0.5f).toInt()
                    val bottom = (0 * scale + 0.5f).toInt()
                    line.setPadding(left, top, right, bottom)
                    lin.addView(line)
                }
            }
            result.addView(lin)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun openSaved() {
        licensePlateHolder.text = ""
        licensePlateHolder.clearFocus()
        val inputManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        result.removeAllViews()
        val scale = context.resources.displayMetrics.density
        val width = (283 * scale + 0.5f).toInt()
        val height = (75 * scale + 0.5f).toInt()
        try {
            val recents = FileHandling(context).savedKentekens
            val lin = LinearLayout(context)
            lin.orientation = LinearLayout.VERTICAL
            val iterator = recents.keys()
            val textView = TextView(context)
            textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
            textView.setText(R.string.eigen_auto)
            println(recents.names())
            lin.addView(textView)
            while (iterator.hasNext()) {
                val key = iterator.next()
                val values = recents.getJSONArray(key)
                for (i in 0 until values.length()) {
                    var recent = values.getString(i)
                    recent = recent.replace("/", "")
                    val line = Button(context)
                    line.text = formatLicensePlate(recent)
                    val finalRecent = recent
                    line.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    line.setOnClickListener { v: View? ->
                        runCamera(
                            finalRecent,
                            licensePlateHolder
                        )
                    }
                    line.background = context.getDrawable(R.drawable.kentekenplaat3)
                    val params = LinearLayout.LayoutParams(
                        width,
                        height
                    )
                    params.setMargins(0, 10, 0, 10)
                    params.gravity = 17
                    line.layoutParams = params
                    line.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
                    line.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    val left = (45 * scale + 0.5f).toInt()
                    val right = (10 * scale + 0.5f).toInt()
                    val top = (0 * scale + 0.5f).toInt()
                    val bottom = (0 * scale + 0.5f).toInt()
                    line.setPadding(left, top, right, bottom)
                    lin.addView(line)
                }
            }
            result.addView(lin)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    fun openNotifications() {
        licensePlateHolder.text = ""
        licensePlateHolder.clearFocus()

        // Hide keyboard
        val inputManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0)
        result.removeAllViews()
        val scale = context.resources.displayMetrics.density
        val width = (283 * scale + 0.5f).toInt()
        val height = (75 * scale + 0.5f).toInt()
        try {
            val pendingNotifications = FileHandling(context).pendingNotifications
            val linearLayout = LinearLayout(context)
            linearLayout.orientation = LinearLayout.VERTICAL
            val iterator = pendingNotifications.keys()
            while (iterator.hasNext()) {
                val key = iterator.next()
                val values = pendingNotifications.getJSONArray(key)
                for (i in 0 until values.length()) {
                    val notification = values.getJSONObject(i)
                    val kenteken = notification.getString("kenteken")
                    Log.d(TAG, "openNotifications: $kenteken")
                    val dateView = TextView(context)
                    dateView.text = notification.getString("notificationDate")
                    dateView.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    linearLayout.addView(dateView)
                    val button = Button(context)
                    button.text = formatLicensePlate(kenteken)
                    button.textAlignment = View.TEXT_ALIGNMENT_CENTER
                    button.setOnClickListener { v: View? ->
                        runCamera(
                            kenteken,
                            licensePlateHolder
                        )
                    }
                    button.background = context.getDrawable(R.drawable.kentekenplaat3)
                    val params = LinearLayout.LayoutParams(
                        width,
                        height
                    )
                    params.setMargins(0, 10, 0, 10)
                    params.gravity = 17
                    button.layoutParams = params
                    button.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32f)
                    button.typeface = Typeface.defaultFromStyle(Typeface.BOLD)
                    val left = (45 * scale + 0.5f).toInt()
                    val right = (10 * scale + 0.5f).toInt()
                    val top = (0 * scale + 0.5f).toInt()
                    val bottom = (0 * scale + 0.5f).toInt()
                    button.setPadding(left, top, right, bottom)
                    linearLayout.addView(button)
                }
            }
            result.addView(linearLayout)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val RecentKentekensFile = "recent.json"
        private const val SavedKentekensFile = "favorites.json"
        private const val TAG = "KentekenHandler"

        @JvmStatic
        fun formatLicensePlate(licencePlate: String): String {
            try {
                val sideCode = getSideCodeOfLicensePlate(licencePlate)
                val newLicencePlate = licencePlate.replace("-", "").uppercase(Locale.getDefault())
                return when (sideCode) {
                    1, 2, 3, 4, 5, 6 -> newLicencePlate.substring(
                        0,
                        2
                    ) + '-' + newLicencePlate.substring(2, 4) + '-' + newLicencePlate.substring(
                        4,
                        6
                    )
                    7, 9 -> newLicencePlate.substring(0, 2) + '-' + newLicencePlate.substring(
                        2,
                        5
                    ) + '-' + newLicencePlate[5]
                    8, 10 -> newLicencePlate.substring(0, 1) + '-' + newLicencePlate.substring(
                        1,
                        4
                    ) + '-' + newLicencePlate.substring(4, 6)
                    11, 14 -> newLicencePlate.substring(0, 3) + '-' + newLicencePlate.substring(
                        3,
                        5
                    ) + '-' + newLicencePlate[5]
                    12, 13 -> newLicencePlate.substring(0, 1) + '-' + newLicencePlate.substring(
                        1,
                        3
                    ) + '-' + newLicencePlate.substring(3, 6)
                    else -> newLicencePlate
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
            return licencePlate
        }

        @JvmStatic
        fun getSideCodeOfLicensePlate(licensePlate: String): Int {
            var newLicensePlate = licensePlate
            newLicensePlate = newLicensePlate.replace("-", "").uppercase(Locale.getDefault())

            val patterns: ArrayList<Regex> = ArrayList()
            patterns.add(Regex("^[a-zA-Z]{2}[0-9]{2}[0-9]{2}$"))    // 1 XX-99-99
            patterns.add(Regex("^[0-9]{2}[0-9]{2}[a-zA-Z]{2}$"))    // 2 99-99-XX
            patterns.add(Regex("^[0-9]{2}[a-zA-Z]{2}[0-9]{2}$"))    // 3 99-XX-99
            patterns.add(Regex("^[a-zA-Z]{2}[0-9]{2}[a-zA-Z]{2}$")) // 4 XX-99-XX
            patterns.add(Regex("^[a-zA-Z]{2}[a-zA-Z]{2}[0-9]{2}$")) // 5 XX-XX-99
            patterns.add(Regex("^[0-9]{2}[a-zA-Z]{2}[a-zA-Z]{2}$")) // 6 99-XX-XX
            patterns.add(Regex("^[0-9]{2}[a-zA-Z]{3}[0-9]{1}$"))    // 7 99-XXX-9
            patterns.add(Regex("^[0-9]{1}[a-zA-Z]{3}[0-9]{2}$"))    // 8 9-XXX-99
            patterns.add(Regex("^[a-zA-Z]{2}[0-9]{3}[a-zA-Z]{1}$")) // 9 XX-999-X
            patterns.add(Regex("^[a-zA-Z]{1}[0-9]{3}[a-zA-Z]{2}$")) // 10 X-999-XX
            patterns.add(Regex("^[a-zA-Z]{3}[0-9]{2}[a-zA-Z]{1}$")) // 11 XXX-99-X
            patterns.add(Regex("^[a-zA-Z]{1}[0-9]{2}[a-zA-Z]{3}$")) // 12 X-99-XXX
            patterns.add(Regex("^[0-9]{1}[a-zA-Z]{2}[0-9]{3}$"))    // 13 9-XX-999
            patterns.add(Regex("^[0-9]{3}[a-zA-Z]{2}[0-9]{1}$"))    // 14 999-XX-9

            // except license-plates for diplomats
            val diplomat = Regex("^CD[ABFJNST][0-9]{1,3}$") // for example: CDB1 of CDJ45

            for (i in patterns.indices) {
                if (newLicensePlate.matches(patterns[i])) {
                    return i + 1
                }
            }
            return if (newLicensePlate.matches(diplomat)) {
                -1
            } else -2
        }

        @JvmStatic
        fun isLicensePlateValid(licensePlate: String): Boolean {
            return getSideCodeOfLicensePlate(licensePlate) >= -1
        }
    }
}
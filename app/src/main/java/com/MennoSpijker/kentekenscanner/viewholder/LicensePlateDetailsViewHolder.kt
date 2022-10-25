package com.MennoSpijker.kentekenscanner.viewholder

import android.content.Context
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.MennoSpijker.kentekenscanner.Util.KentekenHandler
import com.MennoSpijker.kentekenscanner.databinding.LicensePlateCardBinding
import com.MennoSpijker.kentekenscanner.databinding.LicensePlateDetailsBinding
import com.MennoSpijker.kentekenscanner.models.LicensePlateDetails
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.*

class LicensePlateDetailsViewHolder(private val binding: LicensePlateDetailsBinding) :
    CustomViewHolder(binding.root) {
    fun bind(licensePlateDetails: LicensePlateDetails) {
        var key = licensePlateDetails.key
        var content = licensePlateDetails.content

        key = key.replace("_", " ")
        key = key.capitalize(Locale.getDefault())

        if (content != null) {

            when (key.lowercase()) {
                "kenteken" -> {
                    content = KentekenHandler.formatLicensePlate(content)
                }
                "vervaldatum apk" -> {
                    key = "Vervaldatum APK"
                }
                else -> {
                    content = content.capitalize(Locale.getDefault())
                }
            }

            if (key.lowercase().contains("datum")) {
                val localDate = LocalDate.parse(content, DateTimeFormatter.ofPattern("dd-MM-yyyy"))
                content = localDate.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
            }

            this.binding.title.text = key
            this.binding.content.text = content
        } else {
            binding.wrapper.visibility = View.GONE
            binding.wrapper.maxHeight = 0
        }
    }
}
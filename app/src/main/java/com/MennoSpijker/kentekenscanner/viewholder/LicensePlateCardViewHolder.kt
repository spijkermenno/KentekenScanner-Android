package com.MennoSpijker.kentekenscanner.viewholder

import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import com.MennoSpijker.kentekenscanner.Util.KentekenHandler
import com.MennoSpijker.kentekenscanner.databinding.LicensePlateCardBinding
import com.MennoSpijker.kentekenscanner.models.LicensePlateDetails
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class LicensePlateCardViewHolder(private val binding: LicensePlateCardBinding) :
    RecyclerView.ViewHolder(binding.root) {
    val context: Context = itemView.context

    fun bind(licensePlateDetails: LicensePlateDetails) {
        var key = licensePlateDetails.key
        var content = licensePlateDetails.content

        key = key.replace("_", " ")
        key = key.capitalize(Locale.getDefault())

        if (content != null) {
            if (key.lowercase().contains("massa")) {
                content += "kg"
            }

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
                content = localDate.toString()
            }

            this.binding.title.text = key
            this.binding.content.text = content
        }
    }
}
package com.MennoSpijker.kentekenscanner.viewholder

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.MennoSpijker.kentekenscanner.Util.KentekenHandler
import com.MennoSpijker.kentekenscanner.databinding.LicensePlateDetailsBinding
import com.MennoSpijker.kentekenscanner.models.LicensePlateDetails
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*

class LicensePlateDetailsViewHolder(private val binding: LicensePlateDetailsBinding) :
    RecyclerView.ViewHolder(binding.root) {
    val context: Context = itemView.context

    fun bind(licensePlateDetails: LicensePlateDetails) {
        if (licensePlateDetails.key == null || licensePlateDetails.content == null) {
            return
        }

        var key = licensePlateDetails.key
        var content = licensePlateDetails.content

        key = key.replace("_", " ")
        key = key.capitalize(Locale.getDefault())

        when(key.lowercase()) {
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
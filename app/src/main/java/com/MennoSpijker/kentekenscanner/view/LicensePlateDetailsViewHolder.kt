package com.MennoSpijker.kentekenscanner.view

import android.content.Context
import androidx.recyclerview.widget.RecyclerView
import com.MennoSpijker.kentekenscanner.databinding.LicensePlateDetailsBinding
import com.MennoSpijker.kentekenscanner.models.LicensePlateDetails
import java.util.*

class LicensePlateDetailsViewHolder(private val binding: LicensePlateDetailsBinding) :
    RecyclerView.ViewHolder(binding.root) {
    val context: Context = itemView.context

    fun bind(licensePlateDetails: LicensePlateDetails) {
        if (licensePlateDetails.key == null || licensePlateDetails.content == null) {
            return
        }

        var key = licensePlateDetails.key!!
        val content = licensePlateDetails.content!!

        key = key.replace("_", " ")
        key = key.capitalize(Locale.getDefault())

        this.binding.title.text = key
        this.binding.content.text = content
    }
}
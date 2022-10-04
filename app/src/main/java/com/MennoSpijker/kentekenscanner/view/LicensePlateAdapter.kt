package com.MennoSpijker.kentekenscanner.view

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.MennoSpijker.kentekenscanner.databinding.LicenseplateCarouselBinding
import com.MennoSpijker.kentekenscanner.responses.LicensePlateResponse

class LicensePlateAdapter : RecyclerView.Adapter<LicensePlateCarouselViewHolder>() {

    var licensePlateResponses = ArrayList<LicensePlateResponse>()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LicensePlateCarouselViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LicenseplateCarouselBinding.inflate(inflater, parent, false)

        return LicensePlateCarouselViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LicensePlateCarouselViewHolder, position: Int) {
        holder.bind(licensePlateResponses[position])
    }

    override fun getItemCount(): Int {
        return licensePlateResponses.size
    }

    fun submit(licensePlateResponse: LicensePlateResponse) {
        if (licensePlateResponses.find { it.licensePlate == licensePlateResponse.licensePlate } == null) {
            this.licensePlateResponses.add(0, licensePlateResponse)
            notifyItemChanged(0)
        }
    }
}
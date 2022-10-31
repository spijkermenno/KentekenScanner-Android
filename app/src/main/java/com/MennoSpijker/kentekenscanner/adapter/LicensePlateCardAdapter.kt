package com.MennoSpijker.kentekenscanner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.MennoSpijker.kentekenscanner.databinding.LicensePlateCardBinding
import com.MennoSpijker.kentekenscanner.models.LicensePlateDetails
import com.MennoSpijker.kentekenscanner.viewholder.LicensePlateCardViewHolder

class LicensePlateCardAdapter : RecyclerView.Adapter<LicensePlateCardViewHolder>() {

    var licensePlateDetails = ArrayList<LicensePlateDetails>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LicensePlateCardViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LicensePlateCardBinding.inflate(inflater, parent, false)

        return LicensePlateCardViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LicensePlateCardViewHolder, position: Int) {
        holder.bind(licensePlateDetails[position])
    }

    override fun getItemCount(): Int {
        return licensePlateDetails.size
    }

    fun submit(details: java.util.ArrayList<LicensePlateDetails>) {
        this.licensePlateDetails.addAll(details)
        notifyItemRangeChanged(0, details.size)
    }
}

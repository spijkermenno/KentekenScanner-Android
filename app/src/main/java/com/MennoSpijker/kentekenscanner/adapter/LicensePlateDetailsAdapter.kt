package com.MennoSpijker.kentekenscanner.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.MennoSpijker.kentekenscanner.databinding.LicensePlateCardBinding
import com.MennoSpijker.kentekenscanner.databinding.LicensePlateDetailsBinding
import com.MennoSpijker.kentekenscanner.models.LicensePlateDetails
import com.MennoSpijker.kentekenscanner.viewholder.LicensePlateCardViewHolder
import com.MennoSpijker.kentekenscanner.viewholder.LicensePlateDetailsViewHolder

class LicensePlateDetailsAdapter : RecyclerView.Adapter<LicensePlateDetailsViewHolder>() {

    var licensePlateDetails = ArrayList<LicensePlateDetails>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LicensePlateDetailsViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = LicensePlateDetailsBinding.inflate(inflater, parent, false)

        return LicensePlateDetailsViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LicensePlateDetailsViewHolder, position: Int) {
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

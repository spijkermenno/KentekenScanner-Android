package com.MennoSpijker.kentekenscanner.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.MennoSpijker.kentekenscanner.databinding.AdvertisementBinding
import com.MennoSpijker.kentekenscanner.databinding.ImageRecyclerViewBinding
import com.MennoSpijker.kentekenscanner.databinding.LicensePlateCardBinding
import com.MennoSpijker.kentekenscanner.databinding.LicensePlateDetailsBinding
import com.MennoSpijker.kentekenscanner.models.LicensePlateDetails
import com.MennoSpijker.kentekenscanner.viewholder.*

class LicensePlateDetailsAdapter : RecyclerView.Adapter<CustomViewHolder>() {

    private val VIEWTYPE_LICENSEPLATE = 1
    private val VIEWTYPE_ADVERTISMENT = 2
    private val VIEWTYPE_IMAGE = 3

    var licensePlateDetails = ArrayList<LicensePlateDetails>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        when (viewType) {
            VIEWTYPE_IMAGE -> {
                val binding = ImageRecyclerViewBinding.inflate(inflater, parent, false)
                return ImageRecyclerViewViewHolder(binding)
            }
            VIEWTYPE_ADVERTISMENT -> {
                val binding = AdvertisementBinding.inflate(inflater, parent, false)
                return AdvertisementBindingViewHolder(binding)
            }
            else -> {
                val binding = LicensePlateDetailsBinding.inflate(inflater, parent, false)
                return LicensePlateDetailsViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        when (holder) {
            is LicensePlateDetailsViewHolder -> {
                holder.bind(licensePlateDetails[position])
            }
            is ImageRecyclerViewViewHolder -> {
                licensePlateDetails[position].content?.let {
                    holder.bind(it)
                }
            }
            is AdvertisementBindingViewHolder -> {
                holder.bind()
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = licensePlateDetails.get(position);

        Log.d("TAG", "getItemViewType: ${item.key}")
        return if (item.key == "imageURL") {
            VIEWTYPE_IMAGE
        } else {
            VIEWTYPE_LICENSEPLATE
        }
    }

    override fun getItemCount(): Int {
        return licensePlateDetails.size
    }

    fun submit(details: java.util.ArrayList<LicensePlateDetails>) {
        this.licensePlateDetails.addAll(details)
        notifyItemRangeChanged(0, details.size)
    }
}

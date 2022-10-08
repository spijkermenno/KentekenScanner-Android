package com.MennoSpijker.kentekenscanner.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.MennoSpijker.kentekenscanner.databinding.AdvertisementBinding
import com.MennoSpijker.kentekenscanner.databinding.EmptyRecyclerViewBinding
import com.MennoSpijker.kentekenscanner.databinding.LicenseplateCarouselBinding
import com.MennoSpijker.kentekenscanner.databinding.SpinnerBinding
import com.MennoSpijker.kentekenscanner.responses.Advertisement
import com.MennoSpijker.kentekenscanner.responses.CustomProgressBar
import com.MennoSpijker.kentekenscanner.responses.LicensePlateResponse
import com.MennoSpijker.kentekenscanner.responses.RecyclerViewItem
import com.MennoSpijker.kentekenscanner.viewholder.*

class LicensePlateAdapter : RecyclerView.Adapter<CustomViewHolder>() {

    var licensePlateResponses = ArrayList<RecyclerViewItem>()

    private val VIEWTYPE_LICENSEPLATE = 1
    private val VIEWTYPE_ADVERTISMENT = 2
    private val VIEWTYPE_PROGRESSBAR = 3

    override fun getItemViewType(position: Int): Int {
        return when (licensePlateResponses[position]) {
            is LicensePlateResponse -> {
                VIEWTYPE_LICENSEPLATE
            }
            is Advertisement -> {
                VIEWTYPE_ADVERTISMENT
            }
            is CustomProgressBar -> {
                VIEWTYPE_PROGRESSBAR
            }
            else -> super.getItemViewType(position)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CustomViewHolder {
        val inflater = LayoutInflater.from(parent.context)

        when (viewType) {
            VIEWTYPE_LICENSEPLATE -> {
                val binding = LicenseplateCarouselBinding.inflate(inflater, parent, false)
                return LicensePlateCarouselViewHolder(binding)
            }
            VIEWTYPE_ADVERTISMENT -> {
                val binding = AdvertisementBinding.inflate(inflater, parent, false)
                return AdvertisementBindingViewHolder(binding)
            }
            VIEWTYPE_PROGRESSBAR -> {
                val binding = SpinnerBinding.inflate(inflater, parent, false)
                return SpinnerViewHolder(binding)
            }
        }

        val binding = EmptyRecyclerViewBinding.inflate(inflater, parent, false)
        return EmptyRecyclerViewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: CustomViewHolder, position: Int) {
        when (holder) {
            is LicensePlateCarouselViewHolder -> {
                holder.bind(licensePlateResponses[position] as LicensePlateResponse)
            }
            is AdvertisementBindingViewHolder -> {
                holder.bind()
            }
            is SpinnerViewHolder -> {
                holder.bind()
            }
        }
    }

    override fun getItemCount(): Int {
        return licensePlateResponses.size
    }

    fun showLoader() {
        this.licensePlateResponses.clear()
        this.licensePlateResponses.add(CustomProgressBar())
    }

    fun clearLoader() {
        if (this.licensePlateResponses.isNotEmpty() && this.licensePlateResponses.first() is CustomProgressBar) {
            this.licensePlateResponses.clear()
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun submit(licensePlateResponses: ArrayList<LicensePlateResponse>) {
        this.licensePlateResponses.clear()

        this.licensePlateResponses.addAll(licensePlateResponses)

        if (this.licensePlateResponses.isEmpty()) {
            this.licensePlateResponses.add(0, Advertisement())
        }

        for (i in licensePlateResponses.size downTo 1 step 3) {
            this.licensePlateResponses.add(i, Advertisement())
        }

        notifyDataSetChanged()
    }
}
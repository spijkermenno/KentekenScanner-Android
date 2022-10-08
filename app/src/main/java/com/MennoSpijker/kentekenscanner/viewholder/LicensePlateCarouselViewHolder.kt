package com.MennoSpijker.kentekenscanner.viewholder

import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.MennoSpijker.kentekenscanner.R
import com.MennoSpijker.kentekenscanner.adapter.ImagePagerAdapter
import com.MennoSpijker.kentekenscanner.adapter.LicensePlateDetailsAdapter
import com.MennoSpijker.kentekenscanner.databinding.LicenseplateCarouselBinding
import com.MennoSpijker.kentekenscanner.responses.LicensePlateResponse

class LicensePlateCarouselViewHolder(private val binding: LicenseplateCarouselBinding) :
    CustomViewHolder(binding.root) {

    fun bind(licensePlateResponse: LicensePlateResponse) {
        this.binding.imageContainer.post {
            val width = this.binding.imageContainer.width
            val height = (this.binding.imageContainer.width / 16) * 9

            val layoutParams = LinearLayout.LayoutParams(
                width,
                height
            )

            this.binding.imageContainer.layoutParams = layoutParams

            binding.imagePager.adapter = ImagePagerAdapter(context, licensePlateResponse.images)
        }

        if (licensePlateResponse.daysTillAPK == null) {
            binding.apkAlert.visibility = View.GONE
            binding.apkWarning.visibility = View.GONE
        } else {
            if (licensePlateResponse.daysTillAPK > 60) {
                binding.apkAlert.visibility = View.GONE
                binding.apkWarning.visibility = View.GONE

            } else if (licensePlateResponse.daysTillAPK in 31..59) {
                // yellow icon
                binding.apkAlert.visibility = View.GONE
            } else {
                // red icon
                binding.apkWarning.visibility = View.GONE
            }
        }

        val recyclerView = this.binding.licenseplateDetailsRecyclerview

        val adapter = LicensePlateDetailsAdapter()
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL

        recyclerView.adapter = adapter
        recyclerView.layoutManager = linearLayoutManager

        adapter.submit(licensePlateResponse.details)

        recyclerView.onFlingListener = null
        PagerSnapHelper().attachToRecyclerView(recyclerView)

        this.binding.indicator.apply {
            attachToRecyclerView(recyclerView)
            setCurrentPosition(0)
        }


        recyclerView.scrollToPosition(0)
    }
}


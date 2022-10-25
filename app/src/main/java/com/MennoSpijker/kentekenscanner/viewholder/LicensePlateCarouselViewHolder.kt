package com.MennoSpijker.kentekenscanner.viewholder

import android.content.Intent
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import com.MennoSpijker.kentekenscanner.activity.LicensePlateDetailsActivity
import com.MennoSpijker.kentekenscanner.adapter.ImagePagerAdapter
import com.MennoSpijker.kentekenscanner.adapter.LicensePlateCardAdapter
import com.MennoSpijker.kentekenscanner.databinding.LicenseplateCarouselBinding
import com.MennoSpijker.kentekenscanner.responses.LicensePlateResponse


class LicensePlateCarouselViewHolder(
    private val binding: LicenseplateCarouselBinding,
) :
    CustomViewHolder(binding.root) {

    private var licencePlateId: Int? = null;

    private fun onClickListener(licenseplate: String) {
        val intent = Intent(context, LicensePlateDetailsActivity::class.java)
        intent.putExtra("licenseplateID", licencePlateId)
        intent.putExtra("licenseplate", licenseplate)
        startActivity(context, intent, null)
    }

    fun bind(licensePlateResponse: LicensePlateResponse) {
        this.licencePlateId = licensePlateResponse.id
        this.binding.informationFab.setOnClickListener { onClickListener(licensePlateResponse.licensePlate) }
        this.binding.informationFabContainer.setOnClickListener {
            onClickListener(
                licensePlateResponse.licensePlate
            )
        }

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

        val recyclerView = this.binding.licenseplateCardRecyclerview

        val adapter = LicensePlateCardAdapter()
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


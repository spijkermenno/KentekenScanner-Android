package com.MennoSpijker.kentekenscanner.view

import android.content.Context
import android.util.Log
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.PagerSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.MennoSpijker.kentekenscanner.R
import com.MennoSpijker.kentekenscanner.databinding.LicenseplateCarouselBinding
import com.MennoSpijker.kentekenscanner.responses.LicensePlateResponse
import com.bumptech.glide.Glide

class LicensePlateCarouselViewHolder(private val binding: LicenseplateCarouselBinding) :
    RecyclerView.ViewHolder(binding.root) {
    val context: Context = itemView.context

    fun bind(licensePlateResponse: LicensePlateResponse) {
        Log.d("TAG", "bind: ${licensePlateResponse.image}")

        this.binding.imageContainer.post {
            val width = this.binding.imageContainer.width
            val height = (this.binding.imageContainer.width / 16) * 9

            val layoutParams = LinearLayout.LayoutParams(
                width,
                height
            )

            this.binding.imageContainer.layoutParams = layoutParams

            Glide.with(context)
                .load(licensePlateResponse.image)
                .placeholder(R.drawable.placeholder_background)
                .into(this.binding.imageView)
        }

        val recyclerView = this.binding.licenseplateDetailsRecyclerview

        val adapter = LicensePlateDetailsAdapter()
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.orientation = LinearLayoutManager.HORIZONTAL

        recyclerView.adapter = adapter
        recyclerView.layoutManager = linearLayoutManager

        adapter.submit(licensePlateResponse.details)

        PagerSnapHelper().attachToRecyclerView(recyclerView)

        this.binding.indicator.apply {
            attachToRecyclerView(recyclerView)
            setCurrentPosition(0)
        }


        recyclerView.scrollToPosition(0)
    }
}


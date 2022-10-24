package com.MennoSpijker.kentekenscanner.viewholder

import android.app.ActionBar
import android.widget.RelativeLayout
import com.MennoSpijker.kentekenscanner.R
import com.MennoSpijker.kentekenscanner.adapter.ImagePagerAdapter
import com.MennoSpijker.kentekenscanner.databinding.EmptyRecyclerViewBinding
import com.MennoSpijker.kentekenscanner.viewholder.CustomViewHolder
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlinx.android.synthetic.main.image_recycler_view.view.*
import kotlin.random.Random

class ImageRecyclerViewViewHolder(val binding: EmptyRecyclerViewBinding) : CustomViewHolder(binding.root) {
    fun bind(images: ArrayList<String>) {
        val imagePager = binding.root.imagePager

       imagePager.adapter = ImagePagerAdapter(context, images)
    }
}

package com.MennoSpijker.kentekenscanner.viewholder

import android.content.res.Resources
import com.MennoSpijker.kentekenscanner.databinding.AdvertisementBinding
import com.MennoSpijker.kentekenscanner.viewholder.CustomViewHolder
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import kotlin.random.Random

class AdvertisementBindingViewHolder(val binding: AdvertisementBinding) :
    CustomViewHolder(binding.root) {
    fun bind() {
        val adview = AdView(binding.root.context)

        adview.adSize = if (Random.nextBoolean()) {
            AdSize.MEDIUM_RECTANGLE
        } else {
            AdSize.LARGE_BANNER
        }

        adview.adUnitId = "ca-app-pub-4928043878967484/5013055585"

        val adRequest = AdRequest.Builder().build()
        adview.loadAd(adRequest)

        binding.holder.addView(adview);
    }
}

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()
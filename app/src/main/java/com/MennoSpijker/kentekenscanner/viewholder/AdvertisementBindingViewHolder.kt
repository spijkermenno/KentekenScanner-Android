package com.MennoSpijker.kentekenscanner.viewholder

import android.app.ActionBar.LayoutParams
import android.content.res.Resources
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.core.view.updateLayoutParams
import com.MennoSpijker.kentekenscanner.R
import com.MennoSpijker.kentekenscanner.databinding.AdvertisementBinding
import com.MennoSpijker.kentekenscanner.viewholder.CustomViewHolder
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import java.util.Arrays
import kotlin.random.Random

class AdvertisementBindingViewHolder(val binding: AdvertisementBinding) :
    CustomViewHolder(binding.root) {
    fun bind() {
        MobileAds.initialize(context) {
            Log.d("TAG", "MobileAds initializing status: ${it.adapterStatusMap.entries.first().key}")
            Log.d("TAG", "MobileAds initializing status: ${it.adapterStatusMap.entries.first().value.initializationState}")
            Log.d("TAG", "MobileAds initializing status: ${it.adapterStatusMap.entries.first().value.description}")
        }

        val adview = AdView(binding.root.context)
        val config = RequestConfiguration.Builder().setTestDeviceIds(listOf("3D790ADAC5F5A1E49E51C60FE8366F59")).build()
        MobileAds.setRequestConfiguration(config)

        val adRequest = AdRequest.Builder().build()

        adview.adListener = object : AdListener() {
            override fun onAdFailedToLoad(adError : LoadAdError) {
                Log.d("TAG", "onAdFailedToLoad: ${adError.responseInfo}")
                Log.d("TAG", "onAdFailedToLoad: ${adError.message}")
                Log.d("TAG", "onAdFailedToLoad: ${adError.cause}")
                Log.d("TAG", "onAdFailedToLoad: ${adError.code}")
                Log.d("TAG", "onAdFailedToLoad: ${adError.domain}")
            }
        }

        val layoutParams = RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)

        if (Random.nextBoolean()) {
            adview.setAdSize(AdSize.MEDIUM_RECTANGLE)
            // 300 x 250
            layoutParams.marginStart =
                binding.root.context.resources.getDimension(R.dimen.default_spacing_0_5x).toInt()
            layoutParams.marginEnd =
                binding.root.context.resources.getDimension(R.dimen.default_spacing_0_5x).toInt()
        } else {
            adview.setAdSize(AdSize.LARGE_BANNER)
            // 320 x 100
            layoutParams.marginStart =
                binding.root.context.resources.getDimension(R.dimen.default_spacing_0_25x).toInt()
            layoutParams.marginEnd =
                binding.root.context.resources.getDimension(R.dimen.default_spacing_0_25x).toInt()
        }

        binding.holder.layoutParams = layoutParams

        adview.adUnitId = "ca-app-pub-4928043878967484/5013055585"


        adview.loadAd(adRequest)

        binding.holder.addView(adview);
    }
}

val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()
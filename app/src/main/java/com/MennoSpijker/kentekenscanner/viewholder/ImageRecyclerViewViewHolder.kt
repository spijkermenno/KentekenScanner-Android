package com.MennoSpijker.kentekenscanner.viewholder

import android.util.Log
import android.widget.LinearLayout
import com.MennoSpijker.kentekenscanner.adapter.ImagePagerAdapter
import com.MennoSpijker.kentekenscanner.databinding.ImageRecyclerViewBinding
import kotlinx.android.synthetic.main.image_recycler_view.view.*

class ImageRecyclerViewViewHolder(val binding: ImageRecyclerViewBinding) :
    CustomViewHolder(binding.root) {
    fun bind(imagesString: String) {

        val images: ArrayList<String> = imagesString.split(",").toCollection(ArrayList())
        val imagePager = binding.root.imagePager

        images.removeLast()

        this.binding.imageContainer.post {
            val width = this.binding.imageContainer.width
            val height = (this.binding.imageContainer.width / 16) * 9

            val layoutParams = LinearLayout.LayoutParams(
                width,
                height
            )

            this.binding.imageContainer.layoutParams = layoutParams

            imagePager.adapter = ImagePagerAdapter(context, images)
        }
    }
}

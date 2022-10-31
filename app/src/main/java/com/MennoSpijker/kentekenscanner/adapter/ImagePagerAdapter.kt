package com.MennoSpijker.kentekenscanner.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.viewpager.widget.PagerAdapter
import androidx.viewpager.widget.ViewPager
import androidx.viewpager2.widget.ViewPager2
import com.MennoSpijker.kentekenscanner.R
import com.bumptech.glide.Glide
import java.util.*
import kotlin.collections.ArrayList


class ImagePagerAdapter(val context: Context, images: ArrayList<String>) : PagerAdapter() {
    var images = ArrayList<String>()
    var layoutInflater: LayoutInflater


    init {
        this.images = images
        layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    }

    override fun getCount(): Int {
        if (images.isEmpty()) {
            return 1
        }
        return images.size
    }

    override fun isViewFromObject(view: View, obj: Any): Boolean {
        return view === obj as LinearLayout
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        // inflating the item.xml
        val itemView: View = layoutInflater.inflate(R.layout.component_image, container, false)

        // referencing the image view from the item.xml file
        val imageView: ImageView = itemView.findViewById(R.id.imageHolder) as ImageView

        if (images.isEmpty()) {
            Glide.with(this.context)
                .load(R.drawable.placeholder)
                .fitCenter()
                .into(imageView)
        } else {
            Glide.with(this.context)
                .load("https://" + images[position])
                .placeholder(R.drawable.placeholder)
                .fitCenter()
                .into(imageView)
        }

        // Adding the View
        Objects.requireNonNull(container).addView(itemView)
        return itemView
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as LinearLayout?)
    }
}
package com.MennoSpijker.kentekenscanner.viewholder

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView


abstract class CustomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    var context: Context

    init {
        context = itemView.context
    }
}

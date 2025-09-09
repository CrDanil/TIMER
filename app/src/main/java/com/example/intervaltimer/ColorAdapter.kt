package com.example.intervaltimer

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.FrameLayout

class ColorAdapter(
    private val context: Context,
    private val colors: IntArray,
    private var selectedColor: Int
) : BaseAdapter() {

    fun setSelectedColor(color: Int) {
        selectedColor = color
        notifyDataSetChanged()
    }

    override fun getCount(): Int = colors.size

    override fun getItem(position: Int): Any = colors[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view: View
        val holder: ViewHolder

        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_color, parent, false)
            holder = ViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as ViewHolder
        }

        val color = colors[position]
        holder.colorView.setBackgroundColor(color)

        // Показываем выделение для выбранного цвета
        if (color == selectedColor) {
            holder.colorView.background.alpha = 128 // Полупрозрачный
        } else {
            holder.colorView.background.alpha = 255 // Полностью непрозрачный
        }

        return view
    }

    private class ViewHolder(view: View) {
        val colorView: FrameLayout = view.findViewById(R.id.colorView)
    }
}
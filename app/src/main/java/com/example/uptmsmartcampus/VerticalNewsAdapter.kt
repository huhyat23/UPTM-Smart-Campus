package com.example.uptmsmartcampus

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class VerticalNewsAdapter(private val newsList: List<NewsItem>) : RecyclerView.Adapter<VerticalNewsAdapter.ViewHolder>() {
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Define views in the item layout
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_news_vertical, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val newsItem = newsList[position]

        // Bind data to the views in the item layout
    }

    override fun getItemCount(): Int = newsList.size
}
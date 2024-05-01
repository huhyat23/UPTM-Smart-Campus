package com.example.uptmsmartcampus

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter
import com.bumptech.glide.Glide
import com.example.uptmsmartcampus.databinding.ItemNewsHorizontalBinding

class HorizontalNewsAdapter(private val newsItems: List<NewsItem>) : PagerAdapter() {

    override fun getCount(): Int {
        return newsItems.size
    }

    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view == `object`
    }

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val binding =
            ItemNewsHorizontalBinding.inflate(LayoutInflater.from(container.context), container, false)
        val newsItem = newsItems[position]

        binding.newsTitleTextView.text = newsItem.title
        Glide.with(container.context)
            .load(newsItem.urlToImage)
            .placeholder(R.drawable.campus_logo)
            .error(R.drawable.campus_logo)
            .centerCrop()
            .into(binding.newsImageView)

        binding.btnNews.setOnClickListener {
            // Handle button click event
            // For example, you can open the news URL in a browser
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(newsItem.url))
            container.context.startActivity(intent)
        }

        container.addView(binding.root)

        return binding.root
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as View)
    }

}


package com.example.uptmsmartcampus

import com.google.gson.annotations.SerializedName

data class NewsResponse(
    @SerializedName("articles") val articles: MutableList<Article>
)



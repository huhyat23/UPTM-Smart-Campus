package com.example.uptmsmartcampus

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface NewsService {
    @GET("https://newsapi.org/v2/top-headlines")
    fun getNews(
        @Query("country") country: String,
        @Query("apiKey") apiKey: String
    ): Call<NewsResponse>
}

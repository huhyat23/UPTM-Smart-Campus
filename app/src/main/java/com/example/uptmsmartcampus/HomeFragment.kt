package com.example.uptmsmartcampus

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import com.example.uptmsmartcampus.databinding.FragmentHomeBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.time.chrono.HijrahDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var dateAndTimeTextView: TextView

    private lateinit var viewPager: ViewPager
    private lateinit var adapter: HorizontalNewsAdapter
    private lateinit var timer: Handler
    private var currentPage: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the TextView
        dateAndTimeTextView = view.findViewById(R.id.dateAndTimeTextView)

        // Update the date and time initially
        updateDateTime()

        // Update the date and time every second
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                updateDateTime()
                handler.postDelayed(this, 1000) // 1 second delay
            }
        })

        viewPager = binding.horizontalViewPager

        // Initialize Handler for auto-scrolling
        timer = Handler()
        timer.postDelayed(updateRunnable, DELAY_MS)

        // Fetch news data
        fetchNewsData()

        // Set click listeners for each menu item
        binding.item1CardView.setOnClickListener {
            navigateToCMSFragment()
        }

        binding.item2CardView.setOnClickListener {
            navigateToEpayFragment()
        }

        binding.item3CardView.setOnClickListener {
            navigateToLMSFragment()
        }

        binding.item4CardView.setOnClickListener {
            navigateToACFragment()
        }

        binding.item5CardView.setOnClickListener {
            navigateToTTFragment()
        }

        binding.item6CardView.setOnClickListener {
            navigateToITHFragment()
        }

        binding.fb.setOnClickListener {
            val url = "https://www.facebook.com/uptm.official/"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        binding.tt.setOnClickListener {
            val url = "https://www.tiktok.com/@uptm_official"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        binding.ig.setOnClickListener {
            val url = "https://www.instagram.com/uptm_official"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }

        binding.yt.setOnClickListener {
            val url = "https://www.youtube.com/@uptm_official"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }

    private fun updateDateTime() {
        val gregorianDateTime = SimpleDateFormat("hh:mm:ss a\n EEEE, d MMMM yyyy", Locale.getDefault()).format(Date())

        val hijriDate = HijrahDate.now()
        val hijriDateTime = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.getDefault()).format(hijriDate)

        val dateTimeString = "$gregorianDateTime\n$hijriDateTime"
        dateAndTimeTextView.text = dateTimeString
    }

    private fun navigateToCMSFragment() {
        replaceFragment(CMSFragment())
    }

    private fun navigateToEpayFragment() {
        val url = "https://epay.kptm.edu.my/login.php?branch=kl"
        val intent = Intent(Intent.ACTION_VIEW)
        intent.data = Uri.parse(url)
        startActivity(intent)
    }

    private fun navigateToLMSFragment() {
        replaceFragment(LMSFragment())
    }

    private fun navigateToACFragment() {
        replaceFragment(ACFragment())
    }

    private fun navigateToTTFragment() {
        replaceFragment(TTFragment())
    }

    private fun navigateToITHFragment() {
        replaceFragment(ITHFragment())
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = requireActivity().supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

    private val updateRunnable = object : Runnable {
        override fun run() {
            if (currentPage == NUM_PAGES) {
                currentPage = 0
            }
            viewPager.setCurrentItem(currentPage++, true)
            timer.postDelayed(this, DELAY_MS)
        }
    }

    private fun fetchNewsData() {
        val apiKey = "a6ae9d9e3eb4420688a1056fc6fe8060"
        val country = "us"

        // Use Retrofit to fetch data
        val retrofit = Retrofit.Builder()
            .baseUrl("https://newsapi.org/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(NewsService::class.java)
        val call = service.getNews(country, apiKey)

        call.enqueue(object : Callback<NewsResponse> {
            override fun onResponse(call: Call<NewsResponse>, response: Response<NewsResponse>) {
                if (response.isSuccessful) {
                    val newsResponse = response.body()
                    val newsItems = newsResponse?.articles?.filter { article ->
                        !article.urlToImage.isNullOrEmpty() // Filter articles with non-null or non-empty urlToImage
                    }?.map { article ->
                        NewsItem(article.title, article.urlToImage!!, article.url)
                    }

                    // Initialize adapter with filtered news items
                    adapter = HorizontalNewsAdapter(newsItems ?: listOf())

                    // Set up ViewPager with adapter
                    viewPager.adapter = adapter
                } else {
                    // Handle error
                }
            }


            override fun onFailure(call: Call<NewsResponse>, t: Throwable) {
                // Handle failure
            }
        })
    }

    companion object {
        private const val DELAY_MS: Long = 6000 // Delay in milliseconds before auto-scroll
        private const val NUM_PAGES = 10 // Number of news items in your ViewPager
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
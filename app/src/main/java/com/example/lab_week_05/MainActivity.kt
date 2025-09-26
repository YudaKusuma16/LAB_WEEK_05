package com.example.lab_week_05

import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import android.widget.ImageView

class MainActivity : AppCompatActivity() {

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.thecatapi.com/v1/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
    }

    private val catApiService by lazy {
        retrofit.create(CatApiService::class.java)
    }

    private val apiResponseView: TextView by lazy {
        findViewById(R.id.api_response)
    }

    private val imageResultView: ImageView by lazy {
        findViewById(R.id.image_result)
    }
    private val imageLoader: ImageLoader by lazy {
        GlideLoader(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // Edge-to-edge handling
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        getCatImageResponse()
    }

    private fun getCatImageResponse() {
        val call = catApiService.searchImages(1, "full")
        call.enqueue(object : Callback<List<ImageData>> {
            override fun onFailure(call: Call<List<ImageData>>, t: Throwable) {
                Log.e(MAIN_ACTIVITY, "Failed to get response", t)
                apiResponseView.text = "Error: ${t.message}"
            }

            override fun onResponse(call: Call<List<ImageData>>, response: Response<List<ImageData>>) {
                if (response.isSuccessful) {
                    val images = response.body()
                    val firstImageUrl = images?.firstOrNull()?.url.orEmpty()

                    if (firstImageUrl.isNotBlank()) {
                        imageLoader.loadImage(firstImageUrl, imageResultView)
                        apiResponseView.text = "Image loaded successfully!"
                    } else {
                        apiResponseView.text = "No image URL found"
                        Log.d(MAIN_ACTIVITY, "Missing image URL")
                    }
                } else {
                    val errorMessage = "Failed to get response\n${response.errorBody()?.string().orEmpty()}"
                    Log.e(MAIN_ACTIVITY, errorMessage)
                    apiResponseView.text = "Error: ${response.code()}"
                }
            }
        })
    }

    companion object {
        const val MAIN_ACTIVITY = "MAIN_ACTIVITY"
    }
}

// Data class untuk response API
data class ImageData(
    val id: String,
    val url: String,
    val width: Int,
    val height: Int
)

interface CatApiService {
    @GET("images/search")
    fun searchImages(
        @Query("limit") limit: Int,
        @Query("size") size: String
    ): Call<List<ImageData>>
}
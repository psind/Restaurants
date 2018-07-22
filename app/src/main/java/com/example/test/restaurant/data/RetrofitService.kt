package com.example.test.restaurant.data

import io.reactivex.Observable
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

/**
 * @ Parteek on 20/07/18.
 */
interface RetrofitService {

    @GET("nearbysearch/json?rankby=distance")
    fun nearbySearch(@Query("location") location: String, @Query("type") type: String, @Query("key") key: String): Observable<Models.PlacesFromTextResponse>

    @GET("nearbysearch/json?rankby=distance")
    fun nearbySearchWithText(@Query("location") location: String, @Query("keyword") type: String, @Query("key") key: String): Observable<Models.PlacesFromTextResponse>

    companion object {
        fun create(): RetrofitService {

            val headerInterceptor = Interceptor {
                val original = it.request()

                val request = original?.newBuilder()
                        ?.method(original.method(), original.body())
                        ?.build()

                it.proceed(request ?: original)
            }

            val httpClient = OkHttpClient.Builder().apply {
                addInterceptor(headerInterceptor)

            }.build()

            val retrofit = Retrofit.Builder()
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .addConverterFactory(GsonConverterFactory.create())
                    .baseUrl("https://maps.googleapis.com/maps/api/place/")
                    .client(httpClient)
                    .build()

            return retrofit.create(RetrofitService::class.java)
        }
    }
}
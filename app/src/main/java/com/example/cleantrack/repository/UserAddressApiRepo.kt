package com.example.cleantrack.repository

import com.example.cleantrack.model.DistrictModel
import com.example.cleantrack.model.MunicipalityModel
import com.example.cleantrack.model.MunicipalityDetailModel
import com.example.cleantrack.model.ProvinceModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface ApiService {
    @GET("provinces")
    suspend fun getProvinces(): List<ProvinceModel>

    @GET("provinces/{provinceId}/districts")
    suspend fun getDistricts(@Path("provinceId") provinceId: Int): List<DistrictModel>

    @GET("districts/{districtId}/municipalities")
    suspend fun getMunicipalities(@Path("districtId") districtId: Int): List<MunicipalityModel>

    // returns municipality detail including ward_count
    @GET("municipalities/{municipalityId}/wards")
    suspend fun getMunicipalityDetail(@Path("municipalityId") municipalityId: Int): MunicipalityDetailModel

    companion object {
        private const val BASE_URL = "https://nepal-location-api.onrender.com/"

        fun create(): ApiService {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BASIC
            }
            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .callTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}
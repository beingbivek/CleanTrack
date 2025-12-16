package com.example.cleantrack.repository

import com.example.cleantrack.model.useraddress.DistrictModel
import com.example.cleantrack.model.useraddress.MunicipalityModel
import com.example.cleantrack.model.useraddress.MunicipalityDetailModel
import com.example.cleantrack.model.useraddress.ProvinceModel
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.concurrent.TimeUnit

interface UserAddressApiRepo {
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

        fun create(): UserAddressApiRepo {
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
                .create(UserAddressApiRepo::class.java)
        }
    }
}
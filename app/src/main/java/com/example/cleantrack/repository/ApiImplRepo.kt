package com.example.cleantrack.repository

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.QueryMap
import com.example.cleantrack.model.map.PlaceAPIResponseModel


interface ApiImplRepo {
    @GET("places/")
    suspend fun getPlaceDetailsResponse(@QueryMap parameters: Map<String, String>): Response<PlaceAPIResponseModel>
}

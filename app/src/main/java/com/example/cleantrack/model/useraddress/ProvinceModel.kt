package com.example.cleantrack.model.useraddress

data class ProvinceModel(
    val id: Int,
    val name: String,
    val districts: List<DistrictModel>
)
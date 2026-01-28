package com.example.cleantrack.model.useraddress

data class DistrictModel(
    val id: Int,
    val name: String,
    val municipalities: List<MunicipalityModel>
)
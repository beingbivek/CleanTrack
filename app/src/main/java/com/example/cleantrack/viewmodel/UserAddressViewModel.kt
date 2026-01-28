package com.example.cleantrack.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleantrack.model.useraddress.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UserAddressViewModel(application: Application) : AndroidViewModel(application) {

    // Store the full nested data here
    private var allProvinceData: List<ProvinceModel> = emptyList()

    // UI State lists
    var provinces by mutableStateOf<List<ProvinceModel>>(emptyList())
        private set
    var districts by mutableStateOf<List<DistrictModel>>(emptyList())
        private set
    var municipalities by mutableStateOf<List<MunicipalityModel>>(emptyList())
        private set
    var wards by mutableStateOf<List<String>>(emptyList())
        private set

    // Selection State
    var selectedProvinceName by mutableStateOf("Select Province")
    var selectedDistrictName by mutableStateOf("Select District")
    var selectedMunicipalityName by mutableStateOf("Select Municipality")
    var selectedWardName by mutableStateOf("Select Ward")

    init {
        loadDataFromAssets()
    }

    private fun loadDataFromAssets() {
        viewModelScope.launch {
            try {
                // Read file in background thread
                val jsonString = withContext(Dispatchers.IO) {
                    getApplication<Application>().assets
                        .open("nepallocationnames.json")
                        .bufferedReader().use { it.readText() }
                }

                // Parse JSON into your nested models
                val type = object : TypeToken<List<ProvinceModel>>() {}.type
                allProvinceData = Gson().fromJson(jsonString, type)

                provinces = allProvinceData
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onProvinceSelected(province: ProvinceModel) {
        selectedProvinceName = province.name
        districts = province.districts // Get districts directly from the province model

        // Reset children
        selectedDistrictName = "Select District"
        selectedMunicipalityName = "Select Municipality"
        municipalities = emptyList()
        selectedWardName = "Select Ward"
        wards = emptyList()
    }

    fun onDistrictSelected(district: DistrictModel) {
        selectedDistrictName = district.name
        municipalities = district.municipalities // Get municipalities directly from district model

        // Reset children
        selectedMunicipalityName = "Select Municipality"
        selectedWardName = "Select Ward"
        wards = emptyList()
    }

    fun onMunicipalitySelected(municipality: MunicipalityModel) {
        selectedMunicipalityName = municipality.name

        // Generate ward list based on the "wards" count in your JSON
        val count = municipality.wards
        wards = (1..count).map { "Ward $it" }
        selectedWardName = "Select Ward"
    }

    fun onWardSelected(wardName: String) {
        selectedWardName = wardName
    }

    fun initializeAddress(pName: String, dName: String, mName: String, wName: String) {
        viewModelScope.launch {
            // 1. Ensure data is loaded before trying to find names
            // If the app just started, loadDataFromAssets might still be running
            if (allProvinceData.isEmpty()) {
                val jsonString = withContext(Dispatchers.IO) {
                    getApplication<Application>().assets
                        .open("nepallocationnames.json")
                        .bufferedReader().use { it.readText() }
                }
                val type = object : TypeToken<List<ProvinceModel>>() {}.type
                allProvinceData = Gson().fromJson(jsonString, type)
                provinces = allProvinceData
            }

            // 2. Find the Province object by name
            val province = allProvinceData.find { it.name == pName }
            if (province != null) {
                selectedProvinceName = province.name
                districts = province.districts

                // 3. Find the District object inside that province
                val district = province.districts.find { it.name == dName }
                if (district != null) {
                    selectedDistrictName = district.name
                    municipalities = district.municipalities

                    // 4. Find the Municipality object inside that district
                    val municipality = district.municipalities.find { it.name == mName }
                    if (municipality != null) {
                        selectedMunicipalityName = municipality.name

                        // 5. Generate the Ward list and select the saved ward
                        val count = municipality.wards
                        wards = (1..count).map { "Ward $it" }
                        selectedWardName = wName
                    }
                }
            }
        }
    }
}
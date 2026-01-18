package com.example.cleantrack.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cleantrack.model.useraddress.DistrictModel
import com.example.cleantrack.model.useraddress.MunicipalityModel
import com.example.cleantrack.model.useraddress.MunicipalityDetailModel
import com.example.cleantrack.model.useraddress.ProvinceModel
import com.example.cleantrack.repository.UserAddressApiRepo
import kotlinx.coroutines.launch

class UserAddressViewModel(
    private val api: UserAddressApiRepo = UserAddressApiRepo.create()
) : ViewModel() {

    // lists from API
    var provinces by mutableStateOf<List<ProvinceModel>>(emptyList())
        private set

    var districts by mutableStateOf<List<DistrictModel>>(emptyList())
        private set

    var municipalities by mutableStateOf<List<MunicipalityModel>>(emptyList())
        private set

    // Ward strings like "Ward 1", "Ward 2", ... generated from ward_count
    var wards by mutableStateOf<List<String>>(emptyList())
        private set

    // selected display text
    var selectedProvinceName by mutableStateOf("Select Province")
        private set
    var selectedDistrictName by mutableStateOf("Select District")
        private set
    var selectedMunicipalityName by mutableStateOf("Select Municipality")
        private set
    var selectedWardName by mutableStateOf("Select Ward")
        private set

    // selected ids
    private var selectedProvinceId: Int? = null
    private var selectedDistrictId: Int? = null
    private var selectedMunicipalityId: Int? = null

    // ui loading / error indicators (optional)
    var loading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        loadProvinces()
    }

    fun loadProvinces() {
        viewModelScope.launch {
            loading = true
            errorMessage = null
            try {
                provinces = api.getProvinces()
            } catch (e: Exception) {
                errorMessage = "Failed to load provinces: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    fun onProvinceSelected(province: ProvinceModel) {
        // set selection
        selectedProvinceId = province.id
        selectedProvinceName = province.name

        // reset deeper selections
        selectedDistrictId = null; selectedDistrictName = "Select District"; districts = emptyList()
        selectedMunicipalityId = null; selectedMunicipalityName = "Select Municipality"; municipalities = emptyList()
        wards = emptyList(); selectedWardName = "Select Ward"

        // load districts
        loadDistricts(province.id)
    }

    private fun loadDistricts(provinceId: Int) {
        viewModelScope.launch {
            loading = true
            errorMessage = null
            try {
                districts = api.getDistricts(provinceId)
            } catch (e: Exception) {
                errorMessage = "Failed to load districts: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    fun onDistrictSelected(district: DistrictModel) {
        selectedDistrictId = district.id
        selectedDistrictName = district.name

        // reset municipality & wards
        selectedMunicipalityId = null; selectedMunicipalityName = "Select Municipality"; municipalities = emptyList()
        wards = emptyList(); selectedWardName = "Select Ward"

        loadMunicipalities(district.id)
    }

    private fun loadMunicipalities(districtId: Int) {
        viewModelScope.launch {
            loading = true
            errorMessage = null
            try {
                municipalities = api.getMunicipalities(districtId)
            } catch (e: Exception) {
                errorMessage = "Failed to load municipalities: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    fun onMunicipalitySelected(municipality: MunicipalityModel) {
        selectedMunicipalityId = municipality.id
        selectedMunicipalityName = municipality.name

        // reset wards
        wards = emptyList()
        selectedWardName = "Select Ward"

        loadWards(municipality.id)
    }

    private fun loadWards(municipalityId: Int) {
        viewModelScope.launch {
            loading = true
            errorMessage = null
            try {
                val detail: MunicipalityDetailModel = api.getMunicipalityDetail(municipalityId)
                val count = detail.ward_count.coerceAtLeast(0)
                wards = if (count > 0) {
                    (1..count).map { "Ward $it" }
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                errorMessage = "Failed to load wards: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    fun onWardSelected(wardName: String) {
        selectedWardName = wardName
    }


    fun initializeAddress(pName: String, dName: String, mName: String, wName: String) {
        viewModelScope.launch {
            loading = true
            try {
                // 1. Load Provinces and find match
                if (provinces.isEmpty()) {
                    provinces = api.getProvinces()
                }
                val province = provinces.find { it.name == pName }

                if (province != null) {
                    selectedProvinceId = province.id
                    selectedProvinceName = province.name

                    // 2. Load Districts and find match
                    districts = api.getDistricts(province.id)
                    val district = districts.find { it.name == dName }

                    if (district != null) {
                        selectedDistrictId = district.id
                        selectedDistrictName = district.name

                        // 3. Load Municipalities and find match
                        municipalities = api.getMunicipalities(district.id)
                        val municipality = municipalities.find { it.name == mName }

                        if (municipality != null) {
                            selectedMunicipalityId = municipality.id
                            selectedMunicipalityName = municipality.name

                            // 4. Load Wards
                            val detail = api.getMunicipalityDetail(municipality.id)
                            val count = detail.ward_count.coerceAtLeast(0)
                            wards = (1..count).map { "Ward $it" }
                            selectedWardName = wName
                        }
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Failed to initialize address: ${e.message}"
            } finally {
                loading = false
            }
        }
    }

    // optional: helper to get currently-selected ids/names for submission
    fun currentSelection(): Map<String, Any?> = mapOf(
        "provinceId" to selectedProvinceId,
        "provinceName" to selectedProvinceName,
        "districtId" to selectedDistrictId,
        "districtName" to selectedDistrictName,
        "municipalityId" to selectedMunicipalityId,
        "municipalityName" to selectedMunicipalityName,
        "wardName" to selectedWardName
    )
}


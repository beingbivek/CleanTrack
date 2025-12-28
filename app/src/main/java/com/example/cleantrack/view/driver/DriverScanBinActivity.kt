package com.example.cleantrack.view.driver

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import com.example.cleantrack.model.BinCollectionModel
import com.example.cleantrack.model.BinModel
import com.example.cleantrack.model.PointsRuleModel
import com.example.cleantrack.viewmodel.ActiveTripViewModel
import com.example.cleantrack.repository.ActiveTripRepoImpl
import com.example.cleantrack.repository.PointsRepoImpl
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class DriverScanBinActivity : ComponentActivity() {
    private lateinit var activeTripViewModel: ActiveTripViewModel
    val pointsRepo = PointsRepoImpl()

    @SuppressLint("ViewModelConstructorInComposable")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Setup the ViewModel
            activeTripViewModel = ActiveTripViewModel(ActiveTripRepoImpl())

            // Your UI
            Button(onClick = { scanLauncher.launch(ScanOptions()) }) {
                Text("Scan Bin QR")
            }
        }
    }

    // QR scan launcher
    private val scanLauncher =
        registerForActivityResult(ScanContract()) { result ->
            if (result.contents != null) {
                val scannedBinId = result.contents
                // Now, validate this bin and proceed to rate it
                validateBin(scannedBinId)
            }
        }

    private fun validateBin(binId: String) {
        // Fetch bin details by ID
        activeTripViewModel.getBinById(binId) { success, message, bin ->
            if (success && bin != null) {
                // Proceed to rate bin
                rateBin(bin)
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun rateBin(bin: BinModel) {
        // Show rating dialog for the driver to rate the bin
        val rating = 5  // Example rating
        val remarks = "Great segregation"
        val segregatedCorrectly = true  // Check if bin was segregated correctly
        val pointsAwarded = 10  // Example: Points awarded for good rating

        // Create a BinCollectionModel
        val collectionModel = BinCollectionModel(
            binId = bin.binId,
            driverId = "Driver ID", // Get driver ID from session or active trip context
            userId = bin.ownerUserId,
            tripId = "Current Trip ID",  // Get the current active trip
            rating = rating,
            remarks = remarks,
            segregatedCorrectly = segregatedCorrectly,
            pointsAwarded = pointsAwarded
        )

        // Save the rating and points
        activeTripViewModel.addBinCollection(collectionModel) { success, message ->
            if (success) {
                pointsRepo.calculatePoints(
                    binType = bin.category,
                    segregatedCorrectly = segregatedCorrectly
                ) { calculatedPoints ->

//                    val updatedCollection = collectionModel.copy(
//                        pointsAwarded = calculatedPoints
//                    )
//
//                    binCollectionRepo.save(updatedCollection)

                    pointsRepo.addPointsToUser(
                        userId = bin.ownerUserId,
                        points = calculatedPoints
                    )
                }
                Toast.makeText(this, "Bin rated successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}

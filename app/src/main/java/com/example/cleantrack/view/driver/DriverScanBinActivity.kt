package com.example.cleantrack.view.driver

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.cleantrack.model.BinCollectionModel
import com.example.cleantrack.model.BinModel
import com.example.cleantrack.repository.*
import com.example.cleantrack.viewmodel.ActiveTripViewModel
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

class DriverScanBinActivity : ComponentActivity() {
    private lateinit var activeTripViewModel: ActiveTripViewModel
    private val pointsRepo = PointsRepoImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize ViewModel immediately
        activeTripViewModel = ActiveTripViewModel(
            ActiveTripRepoImpl(),
            UserRepoImpl(),
            BinRepoImpl(),
            BinCollectionRepoImpl(),
            PointsRepoImpl()
        )

        // 2. IMMEDIATELY LAUNCH SCANNER
        val options = ScanOptions().apply {
            setDesiredBarcodeFormats(ScanOptions.QR_CODE)
            setPrompt("Align the Bin QR Code within the frame")
            setCameraId(0)
            setBeepEnabled(true)
            setBarcodeImageEnabled(true)
            setOrientationLocked(false)
        }
        scanLauncher.launch(options)

        setContent {
            // Loading state UI while camera opens or processing happens
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Color(0xFF009688))
            }
        }
    }

    // QR scan launcher
    private val scanLauncher =
        registerForActivityResult(ScanContract()) { result ->
            if (result.contents != null) {
                val scannedBinId = result.contents
                validateBin(scannedBinId)
            } else {
                // If the user cancels the scan, close this activity and go back to dashboard
                finish()
            }
        }

    private fun validateBin(binId: String) {
        // Get both TRIP_ID and ROUTE_ID from the intent passed from the Dashboard
        val tripId = intent.getStringExtra("TRIP_ID") ?: ""
        val routeId = intent.getStringExtra("ROUTE_ID") ?: ""

        if (tripId.isEmpty() || routeId.isEmpty()) {
            Toast.makeText(this, "Error: Trip or Route info missing", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Now calling the updated ViewModel method with the routeId parameter
        activeTripViewModel.checkAndValidateBin(tripId, routeId, binId) { allowed, message ->
            if (allowed) {
                val routeName = intent.getStringExtra("ROUTE_NAME") ?: ""

                val intent = Intent(this, BinCollectionActivity::class.java).apply {
                    putExtra("BIN_ID", binId)
                    putExtra("TRIP_ID", tripId)
                    putExtra("ROUTE_ID", routeId)
                    putExtra("ROUTE_NAME", routeName) // 🔥 PASS IT
                }
                startActivity(intent)
                finish()
            } else {
                // This will now show "Access Denied: This bin belongs to Route X..."
                Toast.makeText(this, message, Toast.LENGTH_LONG).show()
                finish()
            }
        }
    }

    // Logic for rating if you handle it within this flow later
    private fun rateBin(bin: BinModel) {
        val rating = 5
        val remarks = "Great segregation"
        val segregatedCorrectly = true
        val pointsAwarded = 10

        val collectionModel = BinCollectionModel(
            binId = bin.binId,
            driverId = "Driver ID",
            userId = bin.ownerUserId,
            tripId = "Current Trip ID",
            rating = rating,
            remarks = remarks,
            segregatedCorrectly = segregatedCorrectly,
            pointsAwarded = pointsAwarded
        )

        activeTripViewModel.addBinCollection(collectionModel) { success, message ->
            if (success) {
                pointsRepo.calculatePoints(
                    binType = bin.category,
                    segregatedCorrectly = segregatedCorrectly
                ) { calculatedPoints ->
                    pointsRepo.addPointsToUser(
                        userId = bin.ownerUserId,
                        points = calculatedPoints,

                    )

                }
                Toast.makeText(this, "Bin rated successfully", Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
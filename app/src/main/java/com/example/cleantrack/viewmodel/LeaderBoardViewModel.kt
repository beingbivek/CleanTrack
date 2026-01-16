package com.example.cleantrack.viewmodel

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.cleantrack.model.LeaderBoardUser
import com.example.cleantrack.repository.PointsRepo

class LeaderboardViewModel(private val repository: PointsRepo) : ViewModel() {

    // Ensure this name matches what the Activity calls
    val leaderboardState = mutableStateOf<List<LeaderBoardUser>>(emptyList())
    val isLoading = mutableStateOf(true)

    init {
        fetchLeaderboard()
    }

    fun fetchLeaderboard() {
        isLoading.value = true
        repository.getLeaderboardData { data ->
            leaderboardState.value = data
            isLoading.value = false
        }
    }
}
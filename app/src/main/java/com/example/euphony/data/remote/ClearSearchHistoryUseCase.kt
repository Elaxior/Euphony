package com.example.euphony.domain.usecase

import com.example.euphony.data.local.dao.SearchHistoryDao

class ClearSearchHistoryUseCase(
    private val searchHistoryDao: SearchHistoryDao
) {
    suspend operator fun invoke() {
        searchHistoryDao.clearAllSearches()
    }
}

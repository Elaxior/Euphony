package com.example.euphony.domain.usecase

import com.example.euphony.data.local.dao.SearchHistoryDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GetRecentSearchesUseCase(
    private val searchHistoryDao: SearchHistoryDao
) {
    operator fun invoke(limit: Int = 10): Flow<List<String>> {
        return searchHistoryDao.getRecentSearches(limit)
            .map { entities -> entities.map { it.query } }
    }
}

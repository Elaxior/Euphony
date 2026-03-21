package com.example.euphony.domain.usecase

import com.example.euphony.data.local.dao.SearchHistoryDao
import com.example.euphony.data.local.entity.SearchHistoryEntity

class AddSearchToHistoryUseCase(
    private val searchHistoryDao: SearchHistoryDao
) {
    suspend operator fun invoke(query: String) {
        if (query.isBlank()) return

        // Delete existing entry with same query to update timestamp
        searchHistoryDao.deleteSearchByQuery(query)

        // Insert new entry
        val searchEntity = SearchHistoryEntity(
            query = query.trim(),
            timestamp = System.currentTimeMillis()
        )
        searchHistoryDao.insertSearch(searchEntity)
    }
}

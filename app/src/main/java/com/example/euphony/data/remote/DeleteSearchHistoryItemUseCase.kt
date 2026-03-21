package com.example.euphony.domain.usecase

import com.example.euphony.data.local.dao.SearchHistoryDao

class DeleteSearchHistoryItemUseCase(
    private val searchHistoryDao: SearchHistoryDao
) {
    suspend operator fun invoke(query: String) {
        searchHistoryDao.deleteSearchByQuery(query)
    }
}

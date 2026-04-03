package com.example.euphony.ui.screens.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.euphony.domain.usecase.AddSearchToHistoryUseCase
import com.example.euphony.domain.usecase.ClearSearchHistoryUseCase
import com.example.euphony.domain.usecase.DeleteSearchHistoryItemUseCase
import com.example.euphony.domain.usecase.GetRecentSearchesUseCase
import com.example.euphony.domain.usecase.AddToQueueUseCase
import com.example.euphony.domain.usecase.SearchSongsUseCase

class SearchViewModelFactory(
    private val searchSongsUseCase: SearchSongsUseCase,
    private val addSearchToHistoryUseCase: AddSearchToHistoryUseCase,
    private val getRecentSearchesUseCase: GetRecentSearchesUseCase,
    private val clearSearchHistoryUseCase: ClearSearchHistoryUseCase,
    private val deleteSearchHistoryItemUseCase: DeleteSearchHistoryItemUseCase,
    private val addToQueueUseCase: AddToQueueUseCase
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SearchViewModel::class.java)) {
            return SearchViewModel(
                searchSongsUseCase,
                addSearchToHistoryUseCase,
                getRecentSearchesUseCase,
                clearSearchHistoryUseCase,
                deleteSearchHistoryItemUseCase,
                addToQueueUseCase
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
    }
}

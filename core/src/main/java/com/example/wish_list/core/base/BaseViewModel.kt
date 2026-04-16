package com.example.wish_list.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {
    protected fun launchInScope(
        block: suspend () -> Unit
    ) {
        viewModelScope.launch {
            block()
        }
    }
}

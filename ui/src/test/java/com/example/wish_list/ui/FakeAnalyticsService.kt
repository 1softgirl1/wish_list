package com.example.wish_list.ui

import com.example.wish_list.ui.analytics.AnalyticsService

class FakeAnalyticsService : AnalyticsService {
    data class Event(val name: String, val params: Map<String, Any>)
    data class Error(val message: String, val throwable: Throwable?)

    val events = mutableListOf<Event>()
    val errors = mutableListOf<Error>()

    override fun trackEvent(name: String, params: Map<String, Any>) {
        events += Event(name = name, params = params)
    }

    override fun trackError(message: String, error: Throwable?) {
        errors += Error(message = message, throwable = error)
    }
}

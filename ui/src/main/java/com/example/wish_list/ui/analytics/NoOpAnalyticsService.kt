package com.example.wish_list.ui.analytics

object NoOpAnalyticsService : AnalyticsService {
    override fun trackEvent(name: String, params: Map<String, Any>) = Unit

    override fun trackError(message: String, error: Throwable?) = Unit
}

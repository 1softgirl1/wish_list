package com.example.wish_list

import com.example.wish_list.ui.analytics.AnalyticsService
import io.appmetrica.analytics.AppMetrica

class AppMetricaAnalyticsService : AnalyticsService {
    override fun trackEvent(name: String, params: Map<String, Any>) {
        AppMetrica.reportEvent(name, params)
    }

    override fun trackError(message: String, error: Throwable?) {
        if (error == null) {
            AppMetrica.reportEvent(
                "analytics_error",
                mapOf("message" to message)
            )
            return
        }
        AppMetrica.reportError(message, error)
    }
}

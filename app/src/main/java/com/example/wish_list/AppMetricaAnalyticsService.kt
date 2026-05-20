package com.example.wish_list

import com.example.wish_list.ui.analytics.AnalyticsService
import io.appmetrica.analytics.AppMetrica
import javax.inject.Inject

class AppMetricaAnalyticsService @Inject constructor() : AnalyticsService {
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

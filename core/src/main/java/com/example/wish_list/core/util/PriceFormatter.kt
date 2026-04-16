package com.example.wish_list.core.util

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

object PriceFormatter {
    private val symbols = DecimalFormatSymbols(Locale.US)
    private val formatter = DecimalFormat("#,##0.00", symbols)

    fun formatOrDash(price: Double?): String {
        return if (price == null) "-" else formatter.format(price)
    }
}

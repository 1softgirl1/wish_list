package com.example.wish_list.feature.publicwishlist

object ShareCodeSanitizer {
    fun sanitize(input: String): String = input.trim().uppercase()
}

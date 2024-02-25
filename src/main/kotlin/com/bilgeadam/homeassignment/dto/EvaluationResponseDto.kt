package com.bilgeadam.homeassignment.dto

data class EvaluationResponseDto(
    var mostSpeeches: String? = null,
    var mostSecurity: String? = null,
    var leastWordy: String? = null,
    var errorMessage: String? = null
)
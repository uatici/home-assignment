package com.bilgeadam.homeassignment.controller

import com.bilgeadam.homeassignment.dto.EvaluationResponseDto
import com.bilgeadam.homeassignment.exception.ValidationErrorException
import com.bilgeadam.homeassignment.service.EvaluationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
class EvaluationController(val evaluationService: EvaluationService) {

    @GetMapping("/evaluation")
    fun evaluate(@RequestParam qparams: Map<String, String>): ResponseEntity<EvaluationResponseDto> {
        for (key in qparams.keys) {
            isValidUrlParameter(key)
        }
        val result = evaluationService.evaluate(qparams)
        return ResponseEntity.ok(result)
    }

    private fun isValidUrlParameter(key: String) {
        val keyRegex = Regex("^url[1-9]\\d*$")
        if (!keyRegex.matches(key)) {
            throw ValidationErrorException(
                    "Validation Error: query param $key is not valid"
            )
        }
    }
}
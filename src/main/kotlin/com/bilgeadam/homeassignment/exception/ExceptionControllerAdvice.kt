package com.bilgeadam.homeassignment.exception

import com.bilgeadam.homeassignment.dto.EvaluationResponseDto
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler

@ControllerAdvice
class ExceptionControllerAdvice {

    @ExceptionHandler(ValidationErrorException::class)
    fun handleValidationErrorException(ex: ValidationErrorException): ResponseEntity<EvaluationResponseDto> {
        return ResponseEntity(EvaluationResponseDto(errorMessage = ex.message), HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleGeneralErrorException(ex: RuntimeException): ResponseEntity<EvaluationResponseDto> {
        return ResponseEntity(EvaluationResponseDto(errorMessage = ex.message), HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(FileContentNotCorrectException::class)
    fun handleFileContentNotCorrectException(ex: FileContentNotCorrectException): ResponseEntity<EvaluationResponseDto> {
        return ResponseEntity(EvaluationResponseDto(errorMessage = ex.message), HttpStatus.INTERNAL_SERVER_ERROR)
    }
}
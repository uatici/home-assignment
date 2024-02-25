package com.bilgeadam.homeassignment.fileprocess

interface FileProcessor {

    fun isEligible(extension: String): Boolean
    fun process(url: String, workingDirectory: String): Map<String, LongArray>
    fun fileType(): Set<String>
}
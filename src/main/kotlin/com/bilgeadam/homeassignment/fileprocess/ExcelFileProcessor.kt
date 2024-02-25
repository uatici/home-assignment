package com.bilgeadam.homeassignment.fileprocess

import org.springframework.stereotype.Component

@Component
class ExcelFileProcessor(): FileProcessor {
    override fun isEligible(extension: String): Boolean {
        return fileType().contains(extension)
    }

    override fun process(url: String, workingDirectory: String): Map<String, LongArray> {
        TODO("Not yet implemented")
    }

    override fun fileType(): Set<String> {
        return setOf("xls, xlsx")
    }


}
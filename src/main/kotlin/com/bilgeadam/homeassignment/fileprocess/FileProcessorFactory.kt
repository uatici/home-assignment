package com.bilgeadam.homeassignment.fileprocess

import com.bilgeadam.homeassignment.exception.FileContentNotCorrectException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class FileProcessorFactory {

    @Autowired
    lateinit var processors: List<FileProcessor>

    fun get(extension: String):FileProcessor {
        return processors.stream().filter { i -> i.isEligible(extension) }.findFirst().orElseThrow { FileContentNotCorrectException("couldn't found available processor, file extension is not valid") }
    }
}
package com.bilgeadam.homeassignment.fileprocess

import java.util.concurrent.Callable

class FileProcessorWorker(private val fileProcessor: FileProcessor,
                          private val url: String, private val workingDirectory: String) : Callable<Map<String, LongArray>> {

    @Throws(Exception::class)
    override fun call(): Map<String, LongArray> {
        return fileProcessor.process(url, workingDirectory)
    }
}
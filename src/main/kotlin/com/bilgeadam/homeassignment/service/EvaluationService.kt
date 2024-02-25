package com.bilgeadam.homeassignment.service

import com.bilgeadam.homeassignment.config.AppConfiguration
import com.bilgeadam.homeassignment.dto.EvaluationResponseDto
import com.bilgeadam.homeassignment.exception.CellContentNotCorrectException
import com.bilgeadam.homeassignment.exception.FileContentNotCorrectException
import com.bilgeadam.homeassignment.exception.LineContentNotCorrectException
import com.bilgeadam.homeassignment.fileprocess.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

@Service
class EvaluationService(private val appConfiguration: AppConfiguration,
        private val fileProcessorFactory: FileProcessorFactory) {

    private val logger: Logger = LoggerFactory.getLogger(EvaluationService::class.java)

    fun evaluate(qparams: Map<String, String>): EvaluationResponseDto {
        var workingDirectory: String? = null
        try {
            val path: Path = Paths.get(Constant.DOWNLOAD_FOLDER, UUID.randomUUID().toString())
            Files.createDirectories(path)
            workingDirectory = path.toString()

            val executorService: ExecutorService = Executors.newFixedThreadPool(qparams.size)
            val futureList = ArrayList<Future<Map<String, LongArray>>>()
            val fileProcessResultList = ArrayList<Map<String, LongArray>>()
            for (url in qparams.values) {
                val extension = getFileExtension(url)
                val fileProcessor: FileProcessor = fileProcessorFactory.get(extension)
                futureList.add(executorService.submit(FileProcessorWorker(fileProcessor,url, workingDirectory)))
            }
            for (future in futureList) {
                try {
                    fileProcessResultList.add(future.get())
                } catch (ex: Exception) {
                    if (appConfiguration.ignoreFile) {
                        logger.warn(ex.message)
                    } else {
                        throw ex
                    }
                }
            }
            val mergedList = mergeFileResults(fileProcessResultList)
            val result = generateStatistics(mergedList)
            return result
        } catch (ex: Exception) {
            when (val cause: Throwable? = ex.cause) {
                is CellContentNotCorrectException, is LineContentNotCorrectException, is FileContentNotCorrectException -> throw FileContentNotCorrectException(cause.message)
                else -> throw RuntimeException(ex)
            }
        } finally {
            workingDirectory?.let {
                try {
                    Files.walk(Paths.get(workingDirectory))
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .forEach(File::delete)
                } catch (ex: IOException) {
                    logger.warn("Exception occurred while deleting folder: {} ", workingDirectory, ex)
                }
            }
        }
    }

    private fun mergeFileResults(fileProcessResultList: ArrayList<Map<String, LongArray>>): Map<String, LongArray> {
        val mergedMap = HashMap<String, LongArray>()

        for (map in fileProcessResultList) {
            for ((key, value) in map) {
                if (mergedMap.containsKey(key)) {
                    val existingValue = mergedMap[key]!!
                    for (i in value.indices) {
                        existingValue[i] += value[i] // Değerlerin toplanması
                    }
                    mergedMap[key] = existingValue
                } else {
                    mergedMap[key] = value.clone()
                }
            }
        }

        return mergedMap
    }

    private fun generateStatistics(mergedFileResults: Map<String, LongArray>): EvaluationResponseDto {
        val mostSecuritySpeakers = ArrayList<String>()
        val mostSpeechesSpeakers = ArrayList<String>()
        val leastWordySpeakers = ArrayList<String>()

        var maxIndex0 = Long.MIN_VALUE
        var maxIndex1 = Long.MIN_VALUE
        var maxIndex2 = Long.MAX_VALUE
        for ((key, values) in mergedFileResults) {
            if (values[0] > maxIndex0) {
                maxIndex0 = values[0]
                mostSecuritySpeakers.clear()
                mostSecuritySpeakers.add(key)
            } else if (values[0] == maxIndex0) {
                mostSecuritySpeakers.add(key)
            }
            if (values[1] > maxIndex1) {
                maxIndex1 = values[1]
                mostSpeechesSpeakers.clear()
                mostSpeechesSpeakers.add(key)
            } else if (values[1] == maxIndex1) {
                mostSpeechesSpeakers.add(key)
            }
            if (values[2] < maxIndex2) {
                maxIndex2 = values[2]
                leastWordySpeakers.clear()
                leastWordySpeakers.add(key)
            } else if (values[2] == maxIndex2) {
                leastWordySpeakers.add(key)
            }
        }

        var mostSpeeches: String? = null
        var mostSecurity: String? = null
        var leastWordy: String? = null

        if (mostSpeechesSpeakers.size == 1) {
            mostSpeeches = mostSpeechesSpeakers.first()
        }

        if (mostSecuritySpeakers.size == 1) {
            mostSecurity = mostSecuritySpeakers.first()
        }

        if (leastWordySpeakers.size == 1) {
            leastWordy = leastWordySpeakers.first()
        }

        return EvaluationResponseDto(mostSpeeches, mostSecurity, leastWordy)
    }

    private fun getFileExtension(url: String?): String {
        var extension = ""

        if (!url.isNullOrEmpty()) {
            val lastDotIndex = url.lastIndexOf('.')
            val lastSlashIndex = url.lastIndexOf('/')
            if (lastDotIndex > lastSlashIndex && lastDotIndex != -1) {
                extension = url.substring(lastDotIndex + 1)
            }
        }

        return extension
    }
}

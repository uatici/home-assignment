package com.bilgeadam.homeassignment.fileprocess

import com.bilgeadam.homeassignment.config.AppConfiguration
import com.bilgeadam.homeassignment.exception.CellContentNotCorrectException
import com.bilgeadam.homeassignment.exception.FileContentNotCorrectException
import com.bilgeadam.homeassignment.exception.LineContentNotCorrectException
import com.bilgeadam.homeassignment.service.DownloadService
import com.opencsv.CSVParserBuilder
import com.opencsv.CSVReaderBuilder
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.util.StringUtils
import java.io.IOException
import java.net.URLEncoder
import java.nio.file.Files
import java.nio.file.Paths
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

@Component
class CsvFileProcessor(var downloadService: DownloadService, var appConfiguration: AppConfiguration) : FileProcessor {

    override fun fileType(): Set<String> {
        return setOf("csv")
    }

    override fun isEligible(extension: String): Boolean {
        return fileType().contains(extension)
    }

    companion object {
        val EXPECTED_HEADER = arrayOf("Speaker", "Topic", "Date", "Words")
        const val SPEAKER = 0
        const val TOPIC = 1
        const val DATE = 2
        const val WORDS = 3
    }

    private val logger = LoggerFactory.getLogger(CsvFileProcessor::class.java)
    override fun process(url: String, workingDirectory: String): Map<String, LongArray> {
        try {
            downloadService.downloadFile(url, workingDirectory)
            val speakerData = mutableMapOf<String, LongArray>()
            Files.newBufferedReader(Paths.get(workingDirectory, URLEncoder.encode(url, Constant.ENCODE_UTF8))).use { reader ->
                val parser = CSVParserBuilder().withSeparator(';').withIgnoreQuotations(true).build()
                CSVReaderBuilder(reader).withCSVParser(parser).build().use { csvReader ->
                    val headerColumns = csvReader.readNext()
                    if (headerColumns == null || headerColumns.size != 4 || !headerColumns.contentEquals(EXPECTED_HEADER)) {
                        throw FileContentNotCorrectException("Header content not correct for file $url")
                    }
                    var line: Array<String>?
                    while (csvReader.readNext().also { line = it } != null) {
                        try {
                            if (line?.size != 4 || !StringUtils.hasText(line?.get(SPEAKER)) || !StringUtils.hasText(line?.get(TOPIC)) ||
                                    !StringUtils.hasText(line?.get(DATE)) || !StringUtils.hasText(line?.get(WORDS))
                            ) {
                                throw LineContentNotCorrectException(
                                        "Line content not expected for file $url and line: ${
                                            line?.joinToString(";")
                                        }"
                                )
                            }
                            val year = try {
                                LocalDate.parse(line?.get(DATE), DateTimeFormatter.ofPattern(Constant.DATE_FORMAT)).year
                            } catch (e: DateTimeParseException) {
                                throw CellContentNotCorrectException(
                                        "Cell content not expected for file $url and line: ${
                                            line?.joinToString(";")
                                        } cell: ${line?.get(DATE)}"
                                )
                            }

                            val words = try {
                                line?.get(WORDS)?.toLong()
                            } catch (e: NumberFormatException) {
                                throw CellContentNotCorrectException(
                                        "Cell content not expected for file $url and line: ${
                                            line?.joinToString(";")
                                        } cell: ${line?.get(WORDS)}"
                                )
                            } ?: 0

                            if (speakerData.containsKey(line?.get(SPEAKER))) {
                                val value = speakerData[line?.get(SPEAKER)]!!
                                if (Constant.SEARCH_YEAR == year) {
                                    value[DATE - 1]++
                                }
                                value[WORDS - 1] += words
                                if (line?.get(TOPIC)?.equals(Constant.SEARCH_TOPIC, ignoreCase = true) == true) {
                                    value[TOPIC - 1]++
                                }
                            } else {
                                val value = LongArray(3)
                                if (Constant.SEARCH_YEAR == year) {
                                    value[DATE - 1]++
                                }
                                value[WORDS - 1] += words
                                if (line?.get(TOPIC)?.equals(Constant.SEARCH_TOPIC, ignoreCase = true) == true) {
                                    value[TOPIC - 1]++
                                }
                                line?.get(SPEAKER)?.let { speakerData.put(it, value) }
                            }
                        } catch (ex: CellContentNotCorrectException) {
                            if (appConfiguration.ignoreCell) {
                                logger.warn(ex.message)
                            } else {
                                throw ex
                            }
                        } catch (ex: LineContentNotCorrectException) {
                            if (appConfiguration.ignoreLine) {
                                logger.warn(ex.message)
                            } else {
                                throw ex
                            }
                        }
                    }
                }
            }
            return speakerData
        } catch (ex: CellContentNotCorrectException) {
            throw ex
        } catch (ex: LineContentNotCorrectException) {
            throw ex
        } catch (ex: FileContentNotCorrectException) {
            throw ex
        } catch (ex: IOException) {
            throw RuntimeException(ex)
        }
    }


}
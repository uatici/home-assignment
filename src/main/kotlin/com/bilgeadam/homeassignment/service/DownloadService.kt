package com.bilgeadam.homeassignment.service

import com.bilgeadam.homeassignment.fileprocess.Constant
import org.springframework.stereotype.Service
import java.io.FileOutputStream
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException
import java.net.URLEncoder
import java.nio.channels.Channels

@Service
class DownloadService {

    @Throws(URISyntaxException::class, IOException::class)
    fun downloadFile(fileUrl: String, workingDirectory: String) {
        val url = URI(fileUrl).toURL()
        val fileName = "$workingDirectory/${URLEncoder.encode(fileUrl, Constant.ENCODE_UTF8)}"
        val readableByteChannel = Channels.newChannel(url.openStream())

        FileOutputStream(fileName).use { fileOutputStream ->
            val fileChannel = fileOutputStream.channel
            fileChannel.transferFrom(readableByteChannel, 0, Long.MAX_VALUE)
        }
    }
}
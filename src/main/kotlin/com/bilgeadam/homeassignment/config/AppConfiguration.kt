package com.bilgeadam.homeassignment.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "application", ignoreInvalidFields = true)
class AppConfiguration {
    var ignoreCell: Boolean = true
    var ignoreLine: Boolean = true
    var ignoreFile: Boolean = true
}
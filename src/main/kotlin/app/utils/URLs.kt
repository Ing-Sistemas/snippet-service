package com.example.springboot.app.utils

import ch.qos.logback.core.util.OptionHelper.getEnv
import org.springframework.boot.autoconfigure.couchbase.CouchbaseProperties.Env

object URLs {
    const val BASE_URL = "http://"//create host and port
    const val API_URL = "/api"
}
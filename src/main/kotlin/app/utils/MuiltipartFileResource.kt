package com.example.springboot.app.utils
import org.springframework.core.io.InputStreamResource
import org.springframework.web.multipart.MultipartFile

class MultipartFileResource(private val multipartFile: MultipartFile) : InputStreamResource(multipartFile.inputStream) {
    override fun getFilename(): String? {
        return multipartFile.originalFilename
    }

    override fun contentLength(): Long {
        return multipartFile.size
    }
}
package com.example.springboot.app.utils

import com.fasterxml.jackson.annotation.JsonProperty

data class AuthUserResponse(
    @JsonProperty("users") val users: List<AuthUserDTO>,
)

package com.healthjournal.data.remote.interceptor

import com.healthjournal.BuildConfig
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor constructor() : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder()
            .addHeader("x-api-key", BuildConfig.CLAUDE_API_KEY)
            .addHeader("anthropic-version", "2023-06-01")
            .addHeader("content-type", "application/json")
            .build()
        return chain.proceed(request)
    }
}

package me.squeezymo.core.network.interceptor

import okhttp3.Headers
import okhttp3.Interceptor
import okhttp3.Response

class ModifyHeadersInterceptor(
    private val modify: Headers.Builder.() -> Headers.Builder
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        return chain.proceed(
            request
                .newBuilder()
                .headers(
                    modify(
                        request
                            .headers
                            .newBuilder()
                    ).build()
                )
                .build()
        )
    }

}

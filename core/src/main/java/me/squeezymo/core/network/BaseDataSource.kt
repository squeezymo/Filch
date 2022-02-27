package me.squeezymo.core.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.reflect.KClass

abstract class BaseDataSource(
    private val httpClientBuilder: OkHttpClient.Builder,
    private val gsonBuilder: GsonBuilder
) {

    private var _gson: Gson? = null
    private val gson: Gson
        get() = _gson ?: createGson(gsonBuilder).also { _gson = it }

    protected fun <T : Any> createApiHandle(
        baseUrl: String,
        apiClass: KClass<T>,
        buildHttpClient: (OkHttpClient.Builder) -> OkHttpClient = OkHttpClient.Builder::build,
        buildRetrofit: (Retrofit.Builder) -> Retrofit = Retrofit.Builder::build
    ): T {
        return buildRetrofit(
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(buildHttpClient(httpClientBuilder))
                .addConverterFactory(GsonConverterFactory.create(gson))
        ).create(apiClass.java)
    }

    protected open fun createGson(gsonBuilder: GsonBuilder): Gson {
        return gsonBuilder.create()
    }

}

package me.rerere.awara.util

import android.util.Log
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import java.io.IOException
import java.lang.reflect.Type
import java.time.Instant
import java.time.format.DateTimeFormatter

private const val TAG = "SerializationConverter"

val JsonInstance = Json {
    ignoreUnknownKeys = true
    isLenient = true
    explicitNulls = false
    prettyPrint = true
}

class SerializationConverterFactory(private val json: Json) : Converter.Factory() {
    companion object {
        fun create(json: Json = JsonInstance): SerializationConverterFactory = SerializationConverterFactory(json)
    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *> {
        val serializer = serializer(type)
        return SerializationResponseBodyConverter(json, serializer)
    }

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody> {
        val serializer = serializer(type)
        return SerializationRequestBodyConverter(json, serializer)
    }
}


internal class SerializationRequestBodyConverter<T>(
    private val json: Json,
    private val type: KSerializer<T>
) : Converter<T, RequestBody> {
    companion object {
        private val MEDIA_TYPE = "application/json; charset=UTF-8".toMediaTypeOrNull()
    }

    @Throws(IOException::class)
    override fun convert(value: T): RequestBody {
        val string = json.encodeToString(type, value)
        return string.toRequestBody(MEDIA_TYPE)
    }
}

internal class SerializationResponseBodyConverter<T>(
    private val json: Json,
    private val type: KSerializer<T>
) : Converter<ResponseBody, T> {

    @Throws(IOException::class)
    override fun convert(body: ResponseBody): T {
        val string = body.string()
        return kotlin.runCatching { json.decodeFromString(type, string) }
            .onFailure {
                Log.w(TAG, "Failed to decode JSON response (${string.length} chars)", it)
            }
            .getOrThrow()
    }
}

// format: 2023-03-20T01:49:29.000Z
object InstantSerializer : KSerializer<Instant> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(DateTimeFormatter.ISO_INSTANT.format(value))
    }

    override fun deserialize(decoder: Decoder): Instant {
        return Instant.from(DateTimeFormatter.ISO_INSTANT.parse(decoder.decodeString()))
    }
}


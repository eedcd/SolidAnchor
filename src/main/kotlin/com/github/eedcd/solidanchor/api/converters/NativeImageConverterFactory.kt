/*
 * This file is part of SolidAnchor
 *
 * Copyright (c) 2015 - 2026 CCBlueX
 *
 * SolidAnchor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * SolidAnchor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with SolidAnchor. If not, see <https://www.gnu.org/licenses/>.
 */

package net.solidanchor.api.converters

import com.mojang.blaze3d.platform.NativeImage
import net.solidanchor.api.core.HttpClient
import net.solidanchor.api.core.asRequestBody
import net.solidanchor.utils.render.readNativeImage
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.Buffer
import retrofit2.Converter
import retrofit2.Retrofit
import java.lang.reflect.Type

object NativeImageConverterFactory : Converter.Factory() {

    override fun requestBodyConverter(
        type: Type,
        parameterAnnotations: Array<out Annotation>,
        methodAnnotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<*, RequestBody>? = if (type == NativeImage::class.java) {
        Converter<NativeImage, _> {
            val buffer = Buffer()
            it.writeToChannel(buffer)
            buffer.asRequestBody(HttpClient.MediaTypes.IMAGE_PNG)
        }
    } else {
        null
    }

    override fun responseBodyConverter(
        type: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): Converter<ResponseBody, *>? = if (type == NativeImage::class.java) {
        Converter<_, NativeImage> {
            it.source().readNativeImage()
        }
    } else {
        null
    }

}

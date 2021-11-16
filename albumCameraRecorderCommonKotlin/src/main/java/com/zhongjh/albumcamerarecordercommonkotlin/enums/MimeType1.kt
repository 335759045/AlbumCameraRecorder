package com.zhongjh.albumcamerarecordercommonkotlin.enums

import android.content.ContentResolver
import android.net.Uri
import android.text.TextUtils
import android.webkit.MimeTypeMap
import androidx.collection.ArraySet
import java.util.*

/**
 * @author zhongjh
 */
enum class MimeType1(
        /**
         * 类型名称
         */
        private val mMimeTypeName: String,
        /**
         * 保存上面所有类型
         */
        private val mExtensions: Set<String>) {
    // ============== 图片 ==============
    JPEG("image/jpeg", arraySetOf(
            "jpg",
            "jpeg"
    )),
    PNG("image/png", arraySetOf(
            "png"
    )),
    GIF("image/gif", arraySetOf(
            "gif"
    )),
    BMP("image/x-ms-bmp", arraySetOf(
            "bmp"
    )),
    WEBP("image/webp", arraySetOf(
            "webp"
    )),  // ============== 音频 ==============
    MP3("video/mp3", arraySetOf(
            "mp3"
    )),  // ============== 视频 ==============
    MPEG("video/mpeg", arraySetOf(
            "mpeg",
            "mpg"
    )),
    MP4("video/mp4", arraySetOf(
            "mp4",
            "m4v"
    )),
    QUICKTIME("video/quicktime", arraySetOf(
            "mov"
    )),
    THREEGPP("video/3gpp", arraySetOf(
            "3gp",
            "3gpp"
    )),
    THREEGPP2("video/3gpp2", arraySetOf(
            "3g2",
            "3gpp2"
    )),
    MKV("video/x-matroska", arraySetOf(
            "mkv"
    )),
    WEBM("video/webm", arraySetOf(
            "webm"
    )),
    TS("video/mp2ts", arraySetOf(
            "ts"
    )),
    AVI("video/avi", arraySetOf(
            "avi"
    ));

    override fun toString(): String {
        return mMimeTypeName
    }

    /**
     * 检查是否有图片或者视频适合的类型
     * @param resolver 数据共享器
     * @param uri 文件uri
     * @return boolean
     */
    fun checkType(resolver: ContentResolver, uri: Uri?): Boolean {
        val map = MimeTypeMap.getSingleton()
        if (uri == null) {
            return false
        }
        // 获取类型
        val type = map.getExtensionFromMimeType(resolver.getType(uri))
        var path: String? = null
        var pathParsed = false
        for (extension in mExtensions) {
            if (extension == type) {
                // 如果有符合的类型，直接返回true
                return true
            }
            if (!pathParsed) {
                path = BasePhotoMetadataUtils.getPath(resolver, uri)
                if (!TextUtils.isEmpty(path)) {
                    path = path.toLowerCase(Locale.US)
                }
                pathParsed = true
            }
            // 判断字符串是否以指定类型后缀结尾
            if (path != null && path.endsWith(extension)) {
                return true
            }
        }
        // 如果类型或者地址后缀都不一样则范围false
        return false
    }

    companion object {
        fun ofAll(): Set<MimeType> {
            return EnumSet.allOf(MimeType::class.java)
        }

        fun of(type: MimeType?, vararg rest: MimeType?): Set<MimeType> {
            return EnumSet.of(type, *rest)
        }

        fun ofImage(): Set<MimeType> {
            return EnumSet.of(JPEG, PNG, GIF, BMP, WEBP)
        }

        fun ofVideo(): Set<MimeType> {
            return EnumSet.of(MPEG, MP4, QUICKTIME, THREEGPP, THREEGPP2, MKV, WEBM, TS, AVI)
        }

        fun isImage(mimeType: String?): Boolean {
            return mimeType?.startsWith("image") ?: false
        }

        fun isVideo(mimeType: String?): Boolean {
            return mimeType?.startsWith("video") ?: false
        }

        fun isGif(mimeType: String?): Boolean {
            return if (mimeType == null) {
                false
            } else mimeType == MimeType.GIF.toString()
        }

        private fun arraySetOf(vararg suffixes: String): Set<String> {
            return ArraySet(Arrays.asList(*suffixes))
        }
    }

}
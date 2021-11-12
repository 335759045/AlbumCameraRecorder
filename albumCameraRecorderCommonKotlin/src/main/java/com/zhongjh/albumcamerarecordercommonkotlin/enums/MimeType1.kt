package com.zhongjh.albumcamerarecordercommonkotlin.enums

import android.content.ContentResolver
import android.net.Uri
import android.text.TextUtils
import android.webkit.MimeTypeMap
import androidx.collection.ArraySet
import com.zhongjh.albumcamerarecordercommonkotlin.utils.BasePhotoMetadataUtils
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
    JPEG("image/jpeg", MimeType1.arraySetOf(
            "jpg",
            "jpeg"
    )),
    PNG("image/png", MimeType1.Companion.arraySetOf(
            "png"
    )),
    GIF("image/gif", MimeType1.Companion.arraySetOf(
            "gif"
    )),
    BMP("image/x-ms-bmp", MimeType1.Companion.arraySetOf(
            "bmp"
    )),
    WEBP("image/webp", MimeType1.Companion.arraySetOf(
            "webp"
    )),  // ============== 音频 ==============
    MP3("video/mp3", MimeType1.Companion.arraySetOf(
            "mp3"
    )),  // ============== 视频 ==============
    MPEG("video/mpeg", MimeType1.Companion.arraySetOf(
            "mpeg",
            "mpg"
    )),
    MP4("video/mp4", MimeType1.Companion.arraySetOf(
            "mp4",
            "m4v"
    )),
    QUICKTIME("video/quicktime", MimeType1.Companion.arraySetOf(
            "mov"
    )),
    THREEGPP("video/3gpp", MimeType1.Companion.arraySetOf(
            "3gp",
            "3gpp"
    )),
    THREEGPP2("video/3gpp2", MimeType1.Companion.arraySetOf(
            "3g2",
            "3gpp2"
    )),
    MKV("video/x-matroska", MimeType1.Companion.arraySetOf(
            "mkv"
    )),
    WEBM("video/webm", MimeType1.Companion.arraySetOf(
            "webm"
    )),
    TS("video/mp2ts", MimeType1.Companion.arraySetOf(
            "ts"
    )),
    AVI("video/avi", MimeType1.Companion.arraySetOf(
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
        fun ofAll(): Set<MimeType1> {
            return EnumSet.allOf(MimeType1::class.java)
        }

        fun of(type: MimeType1?, vararg rest: MimeType1?): Set<MimeType1> {
            return EnumSet.of(type, *rest)
        }

        fun ofImage(): Set<MimeType1> {
            return EnumSet.of(JPEG, MimeType1.PNG, MimeType1.GIF, MimeType1.BMP, MimeType1.WEBP)
        }

        fun ofVideo(): Set<MimeType1> {
            return EnumSet.of(MimeType1.MPEG, MimeType1.MP4, MimeType1.QUICKTIME, MimeType1.THREEGPP, MimeType1.THREEGPP2, MimeType1.MKV, MimeType1.WEBM, MimeType1.TS, MimeType1.AVI)
        }

        fun isImage(MimeType1: String?): Boolean {
            return MimeType1?.startsWith("image") ?: false
        }

        fun isVideo(MimeType1: String?): Boolean {
            return MimeType1?.startsWith("video") ?: false
        }

        fun isGif(MimeType1: String?): Boolean {
            return if (MimeType1 == null) {
                false
            } else MimeType1 == GIF.toString()
        }

        private fun arraySetOf(vararg suffixes: String): Set<String> {
            return ArraySet(Arrays.asList(*suffixes))
        }
    }

}
package com.zhongjh.albumcamerarecordercommonkotlin.utils

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.FileProvider
import com.zhongjh.albumcamerarecordercommonkotlin.entity.SaveStrategy
import java.io.File
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

/**
 * 有关多媒体的文件操作
 *
 * @author zhongjh
 * @date 2018/8/23
 */
class MediaStoreCompat(private val context: Context, private val saveStrategy: SaveStrategy) {

    /**
     * 创建文件
     *
     * @param type    0是图片 1是视频 2是音频
     * @param isCache 是否缓存文件夹
     * @return 文件
     */
    fun createFile(type: Int, isCache: Boolean): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmssS", Locale.getDefault()).format(Date())
        val fileName = when (type) {
            0 -> String.format("JPEG_%s.jpg", timeStamp)
            1 -> String.format("VIDEO_%s.mp4", timeStamp)
            2 -> String.format("AUDIO_%s.mp3", timeStamp)
            else -> String.format("XX_%s.xx", timeStamp)
        }
        return createFile(fileName, type, isCache)
    }

    /**
     * 通过名字创建文件
     *
     * @param fileName 文件名
     * @param type     0是图片 1是视频 2是音频
     * @param isCache  是否缓存文件夹
     * @return 文件
     */
    fun createFile(fileName: String, type: Int, isCache: Boolean): File {

    }

    fun getUri(path: String): Uri {
        return FileProvider.getUriForFile(context, saveStrategy.authority, File(path))
    }

    fun getUri() : Uri {
        return FileProvider.getUriForFile(context, saveStrategy.authority, File(saveStrategy.directory))
    }

    companion object {
        /**
         * 检查设备是否具有相机特性。
         *
         * @param context 检查相机特征的上下文。
         * @return 如果设备具有相机特性，则为真。否则为假。
         */
        fun hasCameraFeature(context: Context): Boolean {
            val pm = context.applicationContext.packageManager
            return pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)
        }
    }

}
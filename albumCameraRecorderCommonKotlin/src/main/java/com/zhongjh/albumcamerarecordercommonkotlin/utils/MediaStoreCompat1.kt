package com.zhongjh.albumcamerarecordercommonkotlin.utils

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.zhongjh.albumcamerarecordercommonkotlin.entity.SaveStrategy
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.ref.WeakReference
import java.text.SimpleDateFormat
import java.util.*

/**
 * 有关多媒体的文件操作
 *
 * @author zhongjh
 * @date 2018/8/23
 */
class MediaStoreCompat1(context: Context) {
    private val mContext: WeakReference<Context>

    /**
     * 设置目录
     */
    /**
     * 设置目录
     */
    var saveStrategy: SaveStrategy? = null

    /**
     * 创建文件
     *
     * @param type    0是图片 1是视频 2是音频
     * @param isCache 是否缓存文件夹
     * @return 文件
     */
    fun createFile(type: Int, isCache: Boolean): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmssS", Locale.getDefault()).format(Date())
        val fileName: String
        fileName = when (type) {
            0 -> String.format("JPEG_%s.jpg", timeStamp)
            1 -> String.format("VIDEO_%s.mp4", timeStamp)
            2 -> String.format("AUDIO_%s.mp3", timeStamp)
            else -> String.format("AUDIO_%s.mp3", timeStamp)
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
    fun createFile(fileName: String?, type: Int, isCache: Boolean): File {
        val storageDir: File?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // 29以上的版本都必须是私有的或者公共目录
            if (isCache) {
                storageDir = File(mContext.get()!!.externalCacheDir!!.path + File.separator + saveStrategy!!.directory)
                if (!storageDir.exists()) {
                    storageDir.mkdirs()
                }
            } else {
                storageDir = when (type) {
                    0 -> mContext.get()!!.getExternalFilesDir(Environment.DIRECTORY_PICTURES + File.separator + saveStrategy!!.directory)
                    1 -> mContext.get()!!.getExternalFilesDir(Environment.DIRECTORY_MOVIES + File.separator + saveStrategy!!.directory)
                    2 -> mContext.get()!!.getExternalFilesDir(Environment.DIRECTORY_MUSIC + File.separator + saveStrategy!!.directory)
                    else -> mContext.get()!!.getExternalFilesDir(Environment.DIRECTORY_MUSIC + File.separator + saveStrategy!!.directory)
                }
            }
        } else {
            if (isCache) {
                storageDir = File(mContext.get()!!.externalCacheDir!!.path + File.separator + saveStrategy!!.directory)
                if (!storageDir.exists()) {
                    storageDir.mkdirs()
                }
            } else {
                if (saveStrategy!!.isPublic) {
                    storageDir = Environment.getExternalStoragePublicDirectory(
                            saveStrategy!!.directory)
                    assert(storageDir != null)
                    if (!storageDir.exists()) {
                        storageDir.mkdirs()
                    }
                } else {
                    storageDir = mContext.get()!!.getExternalFilesDir(saveStrategy!!.directory)
                }
            }
        }

        // Avoid joining path components manually
        return File(storageDir, fileName)
    }

    /**
     * @param bitmap  保存bitmap到file
     * @param isCache 是否缓存文件夹
     * @return 返回file的路径
     */
    fun saveFileByBitmap(bitmap: Bitmap, isCache: Boolean): File? {
        val file: File
        file = createFile(0, isCache)
        try {
            assert(file != null)
            val out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
            out.flush()
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return file
    }

    /**
     * 绑定路径
     */
    fun getUri(path: String?): Uri {
        return FileProvider.getUriForFile(mContext.get()!!, saveStrategy!!.authority, File(path))
    }

    val uri: Uri
        get() = FileProvider.getUriForFile(mContext.get()!!, saveStrategy!!.authority, File(saveStrategy!!.directory))

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

    init {
        mContext = WeakReference(context)
    }
}
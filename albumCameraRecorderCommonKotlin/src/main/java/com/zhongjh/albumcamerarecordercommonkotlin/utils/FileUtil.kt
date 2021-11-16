package com.zhongjh.albumcamerarecordercommonkotlin.utils

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.StrictMode
import android.provider.MediaStore
import android.util.Log
import androidx.core.content.ContextCompat
import com.zhongjh.albumcamerarecordercommonkotlin.enums.MultimediaTypes
import java.io.File

/**
 *
 * @author zhongjh
 * @date 2021/11/16
 * File的有关工具，取自于 https://github.com/baishixian/Share2/blob/master/share2/src/main/java/gdut/bsx/share2/FileUtil.java
 * 主要用来解决部分手机需要系统的原声uri才可以启动默认的播放器
 */
class FileUtil {

    companion object {
        private const val TAG = "FileUtil"

        /**
         * Get file uri
         * @param context context
         * @param shareContentType shareContentType [MultimediaTypes]
         * @param file file
         * @return Uri
         */
        fun getFileUri(context: Context?, @MultimediaTypes shareContentType: Int, file: File?): Uri? {
            if (context == null) {
                Log.e(TAG, "getFileUri current activity is null.")
                return null
            }
            if (file == null || !file.exists()) {
                Log.e(TAG, "getFileUri file is null or not exists.")
                return null
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.e(TAG, "getFileUri miss WRITE_EXTERNAL_STORAGE permission.")
                return null
            }
            var uri: Uri? = null
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                uri = Uri.fromFile(file)
            } else {
                when (shareContentType) {
                    MultimediaTypes.PICTURE -> uri = getImageContentUri(context, file)
                    MultimediaTypes.VIDEO -> uri = getVideoContentUri(context, file)
                    MultimediaTypes.AUDIO -> uri = getAudioContentUri(context, file)
                    MultimediaTypes.BLEND -> uri = getFileContentUri(context, file)
                    MultimediaTypes.ADD -> {}
                    else -> {}
                }
            }
            if (uri == null) {
                uri = forceGetFileUri(file)
            }
            return uri
        }

        /**
         * forceGetFileUri
         * @param shareFile shareFile
         * @return Uri
         */
        @SuppressLint("DiscouragedPrivateApi")
        private fun forceGetFileUri(shareFile: File): Uri {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    val rMethod = StrictMode::class.java.getDeclaredMethod("disableDeathOnFileUriExposure")
                    rMethod.invoke(null)
                } catch (e: Exception) {
                    Log.e(TAG, Log.getStackTraceString(e));
                }
            }
            return Uri.parse("file://" + shareFile.absolutePath)
        }

        /**
         * getFileContentUri
         * @param context context
         * @param file file
         * @return Uri
         */
        @SuppressLint("Range")
        private fun getFileContentUri(context: Context, file: File): Uri? {
            val volumeName = "external"
            val filePath = file.absolutePath
            val projection = arrayOf(MediaStore.Files.FileColumns._ID)
            var uri: Uri? = null
            val cursor = context.contentResolver.query(MediaStore.Files.getContentUri(volumeName), projection,
                    MediaStore.Images.Media.DATA + "=? ", arrayOf(filePath), null)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val id = cursor.getInt(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID))
                    uri = MediaStore.Files.getContentUri(volumeName, id.toLong())
                }
                cursor.close()
            }
            return uri
        }

        /**
         * Gets the content:// URI from the given corresponding path to a file
         *
         * @param context context
         * @param imageFile imageFile
         * @return content Uri
         */
        @SuppressLint("Range")
        private fun getImageContentUri(context: Context, imageFile: File): Uri? {
            val filePath = imageFile.absolutePath
            val cursor = context.contentResolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, arrayOf(MediaStore.Images.Media._ID), MediaStore.Images.Media.DATA + "=? ", arrayOf(filePath), null)
            var uri: Uri? = null
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
                    val baseUri = Uri.parse("content://media/external/images/media")
                    uri = Uri.withAppendedPath(baseUri, "" + id)
                }
                cursor.close()
            }
            if (uri == null) {
                val values = ContentValues()
                values.put(MediaStore.Images.Media.DATA, filePath)
                uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            }
            return uri
        }

        /**
         * Gets the content:// URI from the given corresponding path to a file
         *
         * @param context context
         * @param videoFile videoFile
         * @return content Uri
         */
        @SuppressLint("Range")
        private fun getVideoContentUri(context: Context, videoFile: File): Uri? {
            var uri: Uri? = null
            val filePath = videoFile.absolutePath
            val cursor = context.contentResolver.query(MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.Video.Media._ID), MediaStore.Video.Media.DATA + "=? ",
                    arrayOf(filePath), null)
            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
                    val baseUri = Uri.parse("content://media/external/video/media")
                    uri = Uri.withAppendedPath(baseUri, "" + id)
                }
                cursor.close()
            }
            if (uri == null) {
                val values = ContentValues()
                values.put(MediaStore.Video.Media.DATA, filePath)
                uri = context.contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
            }
            return uri

        }

        /**
         * Gets the content:// URI from the given corresponding path to a file
         *
         * @param context context
         * @param audioFile audioFile
         * @return content Uri
         */
        @SuppressLint("Range")
        private fun getAudioContentUri(context: Context, audioFile: File): Uri? {
            var uri: Uri? = null
            var filePath = audioFile.absolutePath
            var cursor = context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    arrayOf(MediaStore.Audio.Media._ID), MediaStore.Audio.Media.DATA + "=? ",
                    arrayOf(filePath), null)

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    val id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID))
                    val baseUri = Uri.parse("content://media/external/audio/media")
                    uri = Uri.withAppendedPath(baseUri, "" + id)
                }
                cursor.close()
            }
            if (uri == null) {
                val values = ContentValues()
                values.put(MediaStore.Audio.Media.DATA, filePath)
                uri = context.contentResolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, values)
            }
            return uri
        }

    }

}
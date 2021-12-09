package com.zhongjh.albumcamerarecordercommonkotlin.coordinator

import com.zhongjh.albumcamerarecordercommonkotlin.listener.VideoEditListener
import java.util.*

/**
 * 视频编辑协调者
 *
 * @author zhongjh
 */
interface VideoEditCoordinator {
    /**
     * 赋值事件
     *
     * @param videoMergeListener 事件
     */
    fun setVideoMergeListener(videoMergeListener: VideoEditListener?)

    /**
     * 赋值事件
     *
     * @param videoCompressListener 事件
     */
    fun setVideoCompressListener(videoCompressListener: VideoEditListener?)

    /**
     * 合并视频
     *
     * @param newPath 合并后的新视频地址
     * @param paths   多个视频的集合
     * @param txtPath 多个视频的集合地址文本，用 ffmpeg 才能合并
     */
    fun merge(newPath: String?, paths: ArrayList<String?>?, txtPath: String?)

    /**
     * 压缩视频
     *
     * @param oldPath      压缩前的文件地址
     * @param compressPath 压缩后的文件地址
     */
    fun compress(oldPath: String?, compressPath: String?)

    /**
     * 销毁合并事件
     */
    fun onMergeDestroy()

    /**
     * 销毁压缩事件
     */
    fun onCompressDestroy()
}
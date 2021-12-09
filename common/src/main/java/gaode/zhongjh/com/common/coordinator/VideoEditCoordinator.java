package gaode.zhongjh.com.common.coordinator;

import java.util.ArrayList;

import gaode.zhongjh.com.common.listener.VideoEditListener;

/**
 * 视频编辑协调者
 *
 * @author zhongjh
 */
public interface VideoEditCoordinator {

    /**
     * 赋值事件
     *
     * @param videoMergeListener 事件
     */
    void setVideoMergeListener(VideoEditListener videoMergeListener);

    /**
     * 赋值事件
     *
     * @param videoCompressListener 事件
     */
    void setVideoCompressListener(VideoEditListener videoCompressListener);

    /**
     * 合并视频
     *
     * @param newPath 合并后的新视频地址
     * @param paths   多个视频的集合
     * @param txtPath 多个视频的集合地址文本，用 ffmpeg 才能合并
     */
    void merge(String newPath, ArrayList<String> paths, String txtPath);

    /**
     * 压缩视频
     *
     * @param oldPath      压缩前的文件地址
     * @param compressPath 压缩后的文件地址
     */
    void compress(String oldPath, String compressPath);

    /**
     * 销毁合并事件
     */
    void onMergeDestroy();

    /**
     * 销毁压缩事件
     */
    void onCompressDestroy();

}

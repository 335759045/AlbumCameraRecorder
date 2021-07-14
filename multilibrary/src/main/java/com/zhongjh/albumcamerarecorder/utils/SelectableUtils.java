package com.zhongjh.albumcamerarecorder.utils;

import com.zhongjh.albumcamerarecorder.settings.GlobalSpec;

/**
 * 这是一个判断能选择xx个图片、视频、音频的判断逻辑封装
 * 根据当前设置值来呈现相应的功能：
 * 1： maxSelectable有值maxImageSelectable无值，可选择的图片上限和所有数据的上限总和以maxSelectable为标准
 * 2： maxSelectable无值maxImageSelectable有值，可选择的图片上限以maxImageSelectable为准，其他例如视频音频也是以各自的上限为准
 * 3： maxSelectable有值maxImageSelectable有值，可选择的图片上限以maxImageSelectable为准，但是最终总和数据以maxSelectable为标准
 *
 * @author zhongjh
 * @date 2021/7/13
 */
public class SelectableUtils {

    /**
     * 相册是否有效启动
     *
     * @return 是否有效
     */
    public static boolean albumValid() {
        if (GlobalSpec.getInstance().albumSetting != null) {
            if (GlobalSpec.getInstance().maxImageSelectable != null && GlobalSpec.getInstance().maxVideoSelectable != null) {
                return GlobalSpec.getInstance().maxImageSelectable > 0 || GlobalSpec.getInstance().maxVideoSelectable > 0;
            } else if (GlobalSpec.getInstance().maxImageSelectable != null && GlobalSpec.getInstance().maxImageSelectable > 0) {
                return true;
            } else if (GlobalSpec.getInstance().maxVideoSelectable != null && GlobalSpec.getInstance().maxVideoSelectable > 0) {
                return true;
            } else {
                return GlobalSpec.getInstance().maxSelectable != null && GlobalSpec.getInstance().maxSelectable > 0;
            }
        }
        return false;
    }

    /**
     * 拍摄是否有效启动
     *
     * @return 是否有效
     */
    public static boolean cameraValid() {
        if (GlobalSpec.getInstance().cameraSetting != null) {
            if (GlobalSpec.getInstance().maxImageSelectable != null && GlobalSpec.getInstance().maxVideoSelectable != null) {
                return GlobalSpec.getInstance().maxImageSelectable > 0 || GlobalSpec.getInstance().maxVideoSelectable > 0;
            } else if (GlobalSpec.getInstance().maxImageSelectable != null && GlobalSpec.getInstance().maxImageSelectable > 0) {
                return true;
            } else if (GlobalSpec.getInstance().maxVideoSelectable != null && GlobalSpec.getInstance().maxVideoSelectable > 0) {
                return true;
            } else {
                return GlobalSpec.getInstance().maxSelectable != null && GlobalSpec.getInstance().maxSelectable > 0;
            }
        }
        return false;
    }

    /**
     * 录音是否有效启动
     *
     * @return 是否有效
     */
    public static boolean recorderValid() {
        if (GlobalSpec.getInstance().recorderSetting != null) {
            if (GlobalSpec.getInstance().maxAudioSelectable != null && GlobalSpec.getInstance().maxAudioSelectable > 0) {
                return true;
            } else {
                return GlobalSpec.getInstance().maxSelectable != null && GlobalSpec.getInstance().maxSelectable > 0;
            }
        } else {
            return false;
        }
    }

    /**
     * 图片是否已经达到最大数量
     *
     * @param imageCount 当前图片数量
     * @return 是否达到最大数量
     */
    public static boolean isImageMaxCount(int imageCount) {
        if (GlobalSpec.getInstance().maxImageSelectable != null) {
            return imageCount == GlobalSpec.getInstance().maxImageSelectable;
        } else if (GlobalSpec.getInstance().maxSelectable != null) {
            return imageCount == GlobalSpec.getInstance().maxSelectable;
        }
        return true;
    }

    /**
     * 视频是否已经达到最大数量
     *
     * @param videoCount 当前视频数量
     * @return 是否达到最大数量
     */
    public static boolean isVideoMaxCount(int videoCount) {
        if (GlobalSpec.getInstance().maxVideoSelectable != null) {
            return videoCount == GlobalSpec.getInstance().maxVideoSelectable;
        } else if (GlobalSpec.getInstance().maxImageSelectable != null) {
            return videoCount == GlobalSpec.getInstance().maxImageSelectable;
        }
        return true;
    }

    /**
     * @return 返回最多能选择的图片+视频数量
     */
    public static int getImageVideoMaxCount() {
        if (GlobalSpec.getInstance().maxImageSelectable != null && GlobalSpec.getInstance().maxVideoSelectable != null) {
            return GlobalSpec.getInstance().maxImageSelectable + GlobalSpec.getInstance().maxVideoSelectable;
        } else if (GlobalSpec.getInstance().maxImageSelectable != null) {
            return GlobalSpec.getInstance().maxImageSelectable;
        } else if (GlobalSpec.getInstance().maxVideoSelectable != null) {
            return GlobalSpec.getInstance().maxVideoSelectable;
        } else if (GlobalSpec.getInstance().maxSelectable != null) {
            return GlobalSpec.getInstance().maxSelectable;
        }
        return 0;
    }

    /**
     * @return 返回最多能选择的图片
     */
    public static int getImageMaxCount() {
        if (GlobalSpec.getInstance().maxImageSelectable != null) {
            return GlobalSpec.getInstance().maxImageSelectable;
        } else if (GlobalSpec.getInstance().maxSelectable != null) {
            return GlobalSpec.getInstance().maxSelectable;
        } else {
            return 0;
        }
    }

    /**
     * @return 返回最多能选择的视频数量
     */
    public static int getVideoMaxCount() {
        if (GlobalSpec.getInstance().maxVideoSelectable != null) {
            return GlobalSpec.getInstance().maxVideoSelectable;
        } else if (GlobalSpec.getInstance().maxSelectable != null) {
            return GlobalSpec.getInstance().maxSelectable;
        } else {
            return 0;
        }
    }

    /**
     * @return 返回最多能选择的音频数量
     */
    public static int getAudioMaxCount() {
        if (GlobalSpec.getInstance().maxAudioSelectable != null) {
            return GlobalSpec.getInstance().maxAudioSelectable;
        } else if (GlobalSpec.getInstance().maxSelectable != null) {
            return GlobalSpec.getInstance().maxSelectable;
        } else {
            return 0;
        }
    }

    /**
     * @return 返回图片/视频是否只剩下一个选择
     */
    public static boolean getSingleImageVideo() {
        if (GlobalSpec.getInstance().maxImageSelectable != null && GlobalSpec.getInstance().maxVideoSelectable != null) {
            return GlobalSpec.getInstance().maxImageSelectable == 1 && GlobalSpec.getInstance().maxVideoSelectable == 1;
        } else if (GlobalSpec.getInstance().maxImageSelectable != null) {
            return GlobalSpec.getInstance().maxImageSelectable == 1;
        } else if (GlobalSpec.getInstance().maxVideoSelectable != null) {
            return GlobalSpec.getInstance().maxVideoSelectable == 1;
        } else if (GlobalSpec.getInstance().maxSelectable != null) {
            return GlobalSpec.getInstance().maxSelectable == 1;
        }
        return false;
    }


}

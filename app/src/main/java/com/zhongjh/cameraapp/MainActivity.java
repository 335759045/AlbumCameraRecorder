package com.zhongjh.cameraapp;

import android.app.Activity;
import android.content.Intent;

import androidx.databinding.DataBindingUtil;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.zhongjh.albumcamerarecorder.album.filter.BaseFilter;
import com.zhongjh.albumcamerarecorder.settings.AlbumSetting;
import com.zhongjh.albumcamerarecorder.settings.CameraSetting;
import com.zhongjh.albumcamerarecorder.settings.GlobalSetting;
import com.zhongjh.albumcamerarecorder.settings.MultiMediaSetting;
import com.zhongjh.albumcamerarecorder.settings.RecorderSetting;
import com.zhongjh.cameraapp.databinding.ActivityMainBinding;
import com.zhongjh.progresslibrary.entity.MultiMediaView;
import com.zhongjh.progresslibrary.listener.MaskProgressLayoutListener;
import com.zhongjh.progresslibrary.widget.MaskProgressLayout;
import com.zhongjh.videoedit.VideoEditManager;

import java.util.ArrayList;
import java.util.Set;

import gaode.zhongjh.com.common.entity.SaveStrategy;
import gaode.zhongjh.com.common.enums.MimeType;
import gaode.zhongjh.com.common.enums.MultimediaTypes;

/**
 * 配置版
 *
 * @author zhongjh
 */
public class MainActivity extends BaseActivity {

    ActivityMainBinding mBinding;

    GlobalSetting mGlobalSetting;
    AlbumSetting mAlbumSetting;

    /**
     * @param activity 要跳转的activity
     */
    public static void newInstance(Activity activity) {
        activity.startActivity(new Intent(activity, MainActivity.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        // 以下为点击事件
        mBinding.mplImageList.setMaskProgressLayoutListener(new MaskProgressLayoutListener() {

            @Override
            public void onItemAdd(View view, MultiMediaView multiMediaView, int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
                // 点击添加
                boolean isOk = getPermissions(false);
                if (isOk) {
                    openMain(alreadyImageCount, alreadyVideoCount, alreadyAudioCount);
                }
            }

            @Override
            @SuppressWarnings({"unchecked", "rawtypes"})
            public void onItemImage(View view, MultiMediaView multiMediaView) {
                // 点击详情
                if (multiMediaView.getType() == MultimediaTypes.PICTURE) {
                    // 判断如果是图片类型就预览当前所有图片
                    MultiMediaSetting.openPreviewImage(MainActivity.this, (ArrayList) mBinding.mplImageList.getImages(), multiMediaView.getPosition());
                } else if (multiMediaView.getType() == MultimediaTypes.VIDEO) {
                    // 判断如果是视频类型就预览视频
                    MultiMediaSetting.openPreviewVideo(MainActivity.this, (ArrayList) mBinding.mplImageList.getVideos());
                }
            }

            @Override
            public void onItemStartUploading(MultiMediaView multiMediaView) {
                // 开始模拟上传 - 指刚添加后的。这里可以使用你自己的上传事件
                MyTask timer = new MyTask(multiMediaView);
                timers.put(multiMediaView, timer);
                timer.schedule();
            }

            @Override
            public void onItemClose(View view, MultiMediaView multiMediaView) {
                // 停止上传
                MyTask myTask = timers.get(multiMediaView);
                if (myTask != null) {
                    myTask.cancel();
                    timers.remove(multiMediaView);
                }
            }

            @Override
            public void onItemAudioStartDownload(String url) {

            }

            @Override
            public void onItemVideoStartDownload(String url) {

            }

        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGlobalSetting != null) {
            mGlobalSetting.onDestroy();
        }
        if (mAlbumSetting != null) {
            mAlbumSetting.onDestroy();
        }
    }

    @Override
    protected MaskProgressLayout getMaskProgressLayout() {
        return mBinding.mplImageList;
    }

    /**
     * @param alreadyImageCount 已经存在显示的几张图片
     * @param alreadyVideoCount 已经存在显示的几个视频
     * @param alreadyAudioCount 已经存在显示的几个音频
     *                          打开窗体
     */
    @Override
    protected void openMain(int alreadyImageCount, int alreadyVideoCount, int alreadyAudioCount) {
        // 拍摄有关设置
        CameraSetting cameraSetting = initCameraSetting();

        // 相册设置
        mAlbumSetting = initAlbumSetting();

        // 录音机设置
        RecorderSetting recorderSetting = new RecorderSetting();

        //  全局
        Set<MimeType> mimeTypes = null;
        if (mBinding.rbAllAll.isChecked()) {
            mimeTypes = MimeType.ofAll();
        } else if (mBinding.rbAllVideo.isChecked()) {
            mimeTypes = MimeType.ofVideo();
        } else if (mBinding.rbAllImage.isChecked()) {
            mimeTypes = MimeType.ofImage();
        }

        mGlobalSetting = MultiMediaSetting.from(MainActivity.this).choose(mimeTypes);
        // 默认从第二个开始
        mGlobalSetting.defaultPosition(1);
        // 启动过场动画，从下往上动画
        mGlobalSetting.isCutscenes(mBinding.cbIsCutscenes.isChecked());
        // 是否支持编辑图片，预览相册、拍照处拥有编辑功能
        mGlobalSetting.isImageEdit(mBinding.cbIsEdit.isChecked());
        if (mBinding.cbAlbum.isChecked())
        // 开启相册功能
        {
            mGlobalSetting.albumSetting(mAlbumSetting);
        }
        if (mBinding.cbCamera.isChecked())
        // 开启拍摄功能
        {
            mGlobalSetting.cameraSetting(cameraSetting);
        }
        if (mBinding.cbRecorder.isChecked())
        // 开启录音功能
        {
            mGlobalSetting.recorderSetting(recorderSetting);
        }

        // 自定义失败信息
        mGlobalSetting.setOnMainListener(errorMessage -> Toast.makeText(MainActivity.this.getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show());

        // 自定义路径，如果其他子权限设置了路径，那么以子权限为准
        if (!TextUtils.isEmpty(mBinding.etAllFile.getText().toString())) {
            // 设置路径和7.0保护路径等等，只影响录制拍照的路径，选择路径还是按照当前选择的路径
            mGlobalSetting.allStrategy(
                    new SaveStrategy(true, "com.zhongjh.cameraapp.fileprovider", mBinding.etAllFile.getText().toString()));
        }
        if (!TextUtils.isEmpty(mBinding.etPictureFile.getText().toString())) {
            // 设置路径和7.0保护路径等等，只影响录制拍照的路径，选择路径还是按照当前选择的路径
            mGlobalSetting.pictureStrategy(
                    new SaveStrategy(true, "com.zhongjh.cameraapp.fileprovider", mBinding.etPictureFile.getText().toString()));
        }
        if (!TextUtils.isEmpty(mBinding.etAudioFile.getText().toString())) {
            // 设置路径和7.0保护路径等等，只影响录制拍照的路径，选择路径还是按照当前选择的路径
            mGlobalSetting.audioStrategy(
                    new SaveStrategy(true, "com.zhongjh.cameraapp.fileprovider", mBinding.etAudioFile.getText().toString()));
        }
        if (!TextUtils.isEmpty(mBinding.etVideoFile.getText().toString())) {
            // 设置路径和7.0保护路径等等，只影响录制拍照的路径，选择路径还是按照当前选择的路径
            mGlobalSetting.videoStrategy(
                    new SaveStrategy(true, "com.zhongjh.cameraapp.fileprovider", mBinding.etVideoFile.getText().toString()));
        }

        // 加载图片框架
        mGlobalSetting.imageEngine(new Glide4Engine())
                .maxSelectablePerMediaType(
                        mBinding.etMaxCount.getText().toString().isEmpty() ? null :
                        Integer.parseInt(mBinding.etMaxCount.getText().toString()) - (alreadyImageCount + alreadyVideoCount + alreadyAudioCount),
                        Integer.parseInt(mBinding.etAlbumCount.getText().toString()) - alreadyImageCount,
                        Integer.parseInt(mBinding.etVideoCount.getText().toString()) - alreadyVideoCount,
                        Integer.parseInt(mBinding.etAudioCount.getText().toString()) - alreadyAudioCount)
                .forResult(REQUEST_CODE_CHOOSE);
    }

    /**
     * @return 拍摄设置
     */
    private CameraSetting initCameraSetting() {
        CameraSetting cameraSetting = new CameraSetting();
        Set<MimeType> mimeTypeCameras;
        if (mBinding.cbCameraImage.isChecked() && mBinding.cbCameraVideo.isChecked()) {
            mimeTypeCameras = MimeType.ofAll();
            // 支持的类型：图片，视频
            cameraSetting.mimeTypeSet(mimeTypeCameras);
        } else if (mBinding.cbCameraImage.isChecked()) {
            mimeTypeCameras = MimeType.ofImage();
            // 支持的类型：图片，视频
            cameraSetting.mimeTypeSet(mimeTypeCameras);
        } else if (mBinding.cbCameraVideo.isChecked()) {
            mimeTypeCameras = MimeType.ofVideo();
            // 支持的类型：图片，视频
            cameraSetting.mimeTypeSet(mimeTypeCameras);
        }
        // 最长录制时间
        cameraSetting.duration(Integer.parseInt(mBinding.etCameraDuration.getText().toString()));
        // 最短录制时间限制，单位为毫秒，即是如果长按在1500毫秒内，都暂时不开启录制
        cameraSetting.minDuration(Integer.parseInt(mBinding.etMinCameraDuration.getText().toString()));

        if (mBinding.cbVideoEdit.isChecked()) {
            // 启动这个即可开启视频编辑功能
            cameraSetting.videoEdit(new VideoEditManager());
        }
        return cameraSetting;
    }

    /**
     * @return 相册设置
     */
    private AlbumSetting initAlbumSetting() {
        AlbumSetting albumSetting = new AlbumSetting(!mBinding.cbMediaTypeExclusive.isChecked());
        Set<MimeType> mimeTypeAlbum;
        if (mBinding.cbAlbumImage.isChecked() && mBinding.cbAlbumVideo.isChecked()) {
            mimeTypeAlbum = MimeType.ofAll();
            // 支持的类型：图片，视频
            albumSetting.mimeTypeSet(mimeTypeAlbum);
        } else if (mBinding.cbAlbumImage.isChecked()) {
            mimeTypeAlbum = MimeType.ofImage();
            // 支持的类型：图片，视频
            albumSetting.mimeTypeSet(mimeTypeAlbum);
        } else if (mBinding.cbAlbumVideo.isChecked()) {
            mimeTypeAlbum = MimeType.ofVideo();
            // 支持的类型：图片，视频
            albumSetting.mimeTypeSet(mimeTypeAlbum);
        }

        albumSetting
                // 如果选择的媒体只有图像或视频，是否只显示一种媒体类型
                .showSingleMediaType(mBinding.cbShowSingleMediaTypeTrue.isChecked())
                // 是否显示多选图片的数字
                .countable(mBinding.cbCountableTrue.isChecked())
                // 自定义过滤器
                .addFilter(new GifSizeFilter(Integer.parseInt(mBinding.etAddFilterMinWidth.getText().toString()), Integer.parseInt(mBinding.etAddFilterMinHeight.getText().toString()), Integer.parseInt(mBinding.etMaxSizeInBytes.getText().toString()) * BaseFilter.K * BaseFilter.K))
                // 九宫格大小 ,建议这样使用getResources().getDimensionPixelSize(R.dimen.grid_expected_size)
                .gridExpectedSize(dip2px(Integer.parseInt(mBinding.etGridExpectedSize.getText().toString())))
                // 图片缩放比例
                .thumbnailScale(0.85f)
                .setOnSelectedListener((uriList, pathList) -> {
                    // 每次选择的事件
                    Log.e("onSelected", "onSelected: pathList=" + pathList);
                })
                // 开启原图
                .originalEnable(mBinding.cbOriginalEnableTrue.isChecked())
                // 最大原图size,仅当originalEnable为true的时候才有效
                .maxOriginalSize(Integer.parseInt(mBinding.etMaxOriginalSize.getText().toString()))
                .setOnCheckedListener(isChecked -> {
                    // 是否勾选了原图
                    Log.e("isChecked", "onCheck: isChecked=" + isChecked);
                });
        return albumSetting;
    }

}

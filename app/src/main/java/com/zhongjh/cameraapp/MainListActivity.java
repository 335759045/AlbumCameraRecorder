package com.zhongjh.cameraapp;

import androidx.databinding.DataBindingUtil;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.zhongjh.albumcamerarecorder.settings.AlbumSetting;
import com.zhongjh.albumcamerarecorder.settings.GlobalSetting;
import com.zhongjh.albumcamerarecorder.settings.MultiMediaSetting;
import com.zhongjh.cameraapp.databinding.ActivityMainListBinding;

import java.util.ArrayList;

import gaode.zhongjh.com.common.entity.SaveStrategy;
import gaode.zhongjh.com.common.enums.MimeType;

/**
 * list配置
 *
 * @author zhongjh
 * @date 2019/4/25
 */
public class MainListActivity extends AppCompatActivity {

    ActivityMainListBinding mBinding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_list);
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main_list);

        // 简单版
        mBinding.btnSimple.setOnClickListener(v -> MainSimpleActivity.newInstance(MainListActivity.this));

        // 配置版
        mBinding.btnConfigure.setOnClickListener(v -> MainActivity.newInstance(MainListActivity.this));

        // 多种样式版
        mBinding.btnTheme.setOnClickListener(v -> MainThemeActivity.newInstance(MainListActivity.this));

        // 默认有数据的
        mBinding.btnOpenSee.setOnClickListener(v -> MainSeeActivity.newInstance(MainListActivity.this));

        // 独立预览相片功能
        mBinding.btnPreview.setOnClickListener(v -> {
            // 这个可以放在Application初始化类型、解析图片类，也可以不需要，但是如果你要单独使用预览图功能，必须提前设置这个
            MultiMediaSetting.init().choose(MimeType.ofAll()).imageEngine(new Glide4Engine())
            // 设置路径和7.0保护路径等等，只影响录制拍照的路径，选择路径还是按照当前选择的路径
            .allStrategy(new SaveStrategy(true, "com.zhongjh.cameraapp.fileprovider", "preview"));
            ArrayList<Integer> list = new ArrayList<>();
            list.add(R.drawable.ic_camera_enhance_black_24dp);
            list.add(R.drawable.ic_play_arrow_white_24dp);
            MultiMediaSetting.openPreviewResourceId(MainListActivity.this, list, 0);
        });
    }

}

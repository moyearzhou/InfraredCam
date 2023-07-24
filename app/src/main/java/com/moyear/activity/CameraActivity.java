package com.moyear.activity;

import static com.moyear.utils.ImageUtils.yuvImage2JpegData;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Toast;


import com.bumptech.glide.Glide;
import com.google.android.material.tabs.TabLayout;
import com.hcusbsdk.Interface.JavaInterface;
import com.moyear.BasicConfig;
import com.moyear.Constant;
import com.moyear.OnUsbOperateCallback;
import com.moyear.R;
import com.moyear.UsbReceiver;
import com.moyear.core.Infrared;
import com.moyear.core.StreamBytes;
import com.moyear.databinding.ActivityCameraBinding;
import com.moyear.view.ShootView;
import com.moyear.view.ThermalCameraView;
import com.moyear.view.ShutterTouchEventListener;
import com.moyear.viewmodel.CameraActivityViewModel;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

public class CameraActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static String TAG = "CameraActivity";

    public static int MODE_TAKE_PHOTO = 111;
    public static int MODE_TAKE_VIDEO = 222;

    private ActivityCameraBinding mBinding;

    //预览类对象
    private ThermalCameraView thermalCameraView = null;

    private SurfaceView mSurfaceView = null;

    private SurfaceHolder mHolder = null;

    private CameraActivityViewModel viewModel;

    private final Long[] recordTime = new Long[]{0L};
    private Handler handler;

    private UsbReceiver usbReceiver = new UsbReceiver();

    private boolean isRecordingTiming = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CameraActivityViewModel.class);

        mBinding = ActivityCameraBinding.inflate(getLayoutInflater());
        setContentView(mBinding.getRoot());

        initCameraView();

        initCamModeTab();

        mBinding.btnMore.setOnClickListener(view -> showMoreMenu(view));

        mBinding.imgGallery.setOnClickListener(view -> {
            Intent intent = new Intent(CameraActivity.this, GalleryActivity.class);
            startActivity(intent);
        });

        mBinding.btnConfig.setOnClickListener(view -> {
            Toast.makeText(this, "代码待写！！", Toast.LENGTH_SHORT).show();
        });

        mBinding.shootView.setShutterTouchListener(new ShutterTouchEventListener() {
            @Override
            public void takePicture() {
                takeCapture();
            }

            @Override
            public void videoStart() {
                takeRecord();
            }

            @Override
            public void videoEnd() {
                endRecording();
            }
        });

        viewModel.getLatestCaptureInfo().observe(this, captureInfo -> {
            if (captureInfo == null) return;
            updateLatestCapture(captureInfo);
        });

        viewModel.getCameraMode().observe(this, mode -> {
            if (mode == MODE_TAKE_PHOTO) {
                switchToPictureMode();
            } else if (mode == MODE_TAKE_VIDEO) {
                switchToVideoMode();
            }
        });

        viewModel.getCurUserId().observe(this, curUserId -> {
            if (curUserId == JavaInterface.USB_INVALID_USER_ID) {
                showEmptyCameraView();
            } else {
                hideEmptyCameraView();
            }
        });
    }

    private void endRecording() {
        mBinding.shootView.setCameraMode(ShootView.OPTION_TAKE_VIDEO);

        thermalCameraView.endRecord();

        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        isRecordingTiming = false;

        recordTime[0] = 0L;
        updateTimeText(recordTime[0]);
    }

    private void takeRecord() {
        if (viewModel.getUserId() == JavaInterface.USB_INVALID_USER_ID) {
            Toast.makeText(this, "尚未连接到usb相机！", Toast.LENGTH_SHORT).show();
            return;
        }

        mBinding.shootView.setCameraMode(ShootView.OPTION_VIDEO_RECORDING);

        thermalCameraView.startRecord();

        isRecordingTiming = true;
        handler = new Handler();

        updateRecordTime();
    }

    private void updateRecordTime() {
        if (handler == null) return;

        handler.postDelayed(() -> {
            recordTime[0] += 1000;
            updateTimeText(recordTime[0]);

            if (isRecordingTiming) {
                updateRecordTime();
            }
        }, 1000);
    }

    private void updateTimeText(Long elapsedTime) {
        Long hours = TimeUnit.MILLISECONDS.toHours(elapsedTime);
        Long minutes = TimeUnit.MILLISECONDS.toMinutes(elapsedTime);
        Long seconds = TimeUnit.MILLISECONDS.toSeconds(elapsedTime) - TimeUnit.MINUTES.toSeconds(minutes);

        if (hours > 1) {
            mBinding.txtRecordTime.setText(String.format("%02d:%02d:%02d", hours, minutes, seconds));
        } else {
            mBinding.txtRecordTime.setText(String.format("%02d:%02d", minutes, seconds));
        }
    }

    private void switchToVideoMode() {
        Animation fadeIn = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        mBinding.layoutRecodeTimer.startAnimation(fadeIn);
        mBinding.layoutRecodeTimer.setVisibility(View.VISIBLE);

        mBinding.shootView.setCameraMode(ShootView.OPTION_TAKE_VIDEO);
        thermalCameraView.setCaptureMode(ThermalCameraView.CaptureMode.MODE_RECORD);
    }

    private void switchToPictureMode() {
        Animation fadeOut = AnimationUtils.loadAnimation(this, android.R.anim.fade_out);
        mBinding.layoutRecodeTimer.startAnimation(fadeOut);
        mBinding.layoutRecodeTimer.setVisibility(View.GONE);

        mBinding.shootView.setCameraMode(ShootView.OPTION_TAKE_PHOTO);
        thermalCameraView.setCaptureMode(ThermalCameraView.CaptureMode.MODE_CAPTURE);
    }

    private void initCamModeTab() {
        String[] tabs = new String[]{getString(R.string.take_photo), getString(R.string.take_video)};

        TabLayout tableLayout = mBinding.tabMode;
        tableLayout.setTabTextColors(Color.WHITE, Color.parseColor("#ffC13132"));
        for (String title : tabs) {
            TabLayout.Tab newTab = tableLayout.newTab();
            newTab.setText(title);

            tableLayout.addTab(newTab);
        }

        tableLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                CharSequence text = tab.getText();
                if (Objects.equals(text, getString(R.string.take_photo))) {
                    viewModel.getCameraMode().setValue(MODE_TAKE_PHOTO);
                } else if (Objects.equals(text, getString(R.string.take_video))) {
                    viewModel.getCameraMode().setValue(MODE_TAKE_VIDEO);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void updateLatestCapture(Infrared.CaptureInfo captureInfo) {
        File img = Infrared.findCaptureImageFile(captureInfo);

        Glide.with(this)
                .load(img)
                .centerCrop()
                .into(mBinding.imgGallery);
    }

    private void showMoreMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenuInflater().inflate(R.menu.menu_camera_operate, popupMenu.getMenu());

        popupMenu.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.show_info:
                    showSdkInfo();
                    break;
                case R.id.settings:
                    Intent intent2 = new Intent(CameraActivity.this, SettingsActivity.class);
                    startActivity(intent2);
                    break;
            }
            return false;
        });
        popupMenu.show();
    }

    private void showSdkInfo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        String msg = "UserId： " + viewModel.getUserId() + "\n" +
                "Sdk Version: " + viewModel.getSdkVersion() + "\n" +
                "设备数量: " + viewModel.getDeviceCount(this) + "\n" +
                "Last Error: " + viewModel.getUsbLastError();

        builder.setTitle("信息")
                .setMessage(msg)
                .setPositiveButton("确定", null)
                .show();
    }

    private void takeCapture() {
        if (viewModel.getUserId() == JavaInterface.USB_INVALID_USER_ID) {
            Toast.makeText(this, "尚未连接到usb相机！", Toast.LENGTH_SHORT).show();
            return;
        }

        byte[] data = thermalCameraView.getCurrentFrame();
        StreamBytes streamBytes = StreamBytes.fromBytes(data);

        byte[] jpegData = new byte[0];
        // 将yuv数据转换成jpg数据，并显示在SurfaceView上
        byte[] dataYUV = streamBytes.getYuvBytes();
        if (dataYUV != null) {
            int yuvImgWidth = BasicConfig.yuvImgWidth;
            int yuvImgHeight = BasicConfig.yuvImgHeight;
            jpegData = yuvImage2JpegData(dataYUV, new Size(yuvImgWidth, yuvImgHeight));
        }

        try {
            viewModel.saveCapture(data, jpegData);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Log.i(TAG, "Action: ========Take a capture!==========");
    }

    private void initCameraView() {
        thermalCameraView = new ThermalCameraView(this);

        //surfaceview
        mSurfaceView = mBinding.surfaceView;
        mSurfaceView.setZOrderOnTop(true);

        mHolder = mSurfaceView.getHolder(); //得到surfaceView的holder，类似于surfaceView的控制器
        mHolder.setFormat(PixelFormat.TRANSLUCENT);

        //把输送给surfaceView的视频画面，直接显示到屏幕上,不要维持它自身的缓冲区
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mHolder.addCallback(this);

        //读写文件权限动态申请  //高版本SDK下AndroidManifest.xml中配置的读写权限不起作用
        checkPermission();
        //初始化SDK
        initUSbSdk();

        usbReceiver.setOnUsbOperateCallback(new OnUsbOperateCallback() {
            @Override
            public void onAttach(@NonNull UsbDevice usbDevice) {
                Toast.makeText(CameraActivity.this, "USB设备接入", Toast.LENGTH_SHORT).show();
                checkCameraConnection();
            }

            @Override
            public void onDetach(@NonNull UsbDevice usbDevice) {
                Toast.makeText(CameraActivity.this, "USB设备拔出", Toast.LENGTH_SHORT).show();

                viewModel.logoutDevice();

                showEmptyCameraView();
            }
        });

        checkCameraConnection();

        // 获取上次拍摄的照片并显示在左下角
        viewModel.fetchLastCapture();
    }

    /**
     * 检查相机的连接性，如果未连接则连接到usb相机,并显示画面
     */
    @SuppressLint("NewApi")
    private void checkCameraConnection() {
        //枚举设备信息
        try {
            //登录设备
            connectToCamera();
            // 开始预览并显示画面
            startPreview();
        } catch (Exception e) {
            Log.e(Constant.TAG_DEBUG, "Error: " + e);

            Looper.prepare();
            Toast.makeText(CameraActivity.this, "Error:" + e, Toast.LENGTH_SHORT).show();
            Looper.loop();
        }
    }

    /**
     * 连接并登录相机，整个流程按照：USB_Init, USB_GetDeviceCount, USB_EnumDevices, USB_Login的顺序完成
     * @return
     * @throws Exception
     */
    private boolean connectToCamera() throws Exception {
        // 检查Sdk是否初始化
        if (!viewModel.isUsbSdkInit()) {
            initUSbSdk();
        }

        return viewModel.loginDevice(this);
    }

    private void initUSbSdk() {
        checkPermission(Manifest.permission.CAMERA);

        //初始化USB SDK
        if (viewModel.initUsbSdk()) {
            Log.i(Constant.TAG_DEBUG, "USB_Init Success! Current SDK Version: " + viewModel.getSdkVersion());
        } else {
            Log.e(Constant.TAG_DEBUG, "USB_Init Failed!");
        }

        //开启USBSDK日志，参数说明见使用手册接口说明
        if (viewModel.saveSdkLog()) {
            Log.i(Constant.TAG_DEBUG, "USB_SetLogToFile Success!");
        } else {
            Log.e(Constant.TAG_DEBUG, "USB_SetLogToFile failed! error:" + viewModel.getUsbLastError());
        }
    }

    public void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 判断是否有这个权限，是返回PackageManager.PERMISSION_GRANTED，否则是PERMISSION_DENIED
            // 这里我们要给应用授权所以是!= PackageManager.PERMISSION_GRANTED
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "未获得读写权限");
                // 如果应用之前请求过此权限但用户拒绝了请求,且没有选择"不再提醒"选项 (后显示对话框解释为啥要这个权限)，此方法将返回 true。
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Log.i(TAG, "用户永久拒绝权限申请");
                } else {
                    Log.i(TAG, "申请权限");
                    // requestPermissions以标准对话框形式请求权限。123是识别码（任意设置的整型），用来识别权限。应用无法配置或更改此对话框。
                    //当应用请求权限时，系统将向用户显示一个对话框。当用户响应时，系统将调用应用的 onRequestPermissionsResult() 方法，向其传递用户响应。您的应用必须替换该方法，以了解是否已获得相应权限。回调会将您传递的相同请求代码传递给 requestPermissions()。
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                }
            }
            Log.i(TAG, "已获得读写权限");
        } else {
            Log.i(TAG, "无需动态申请");
        }
    }

    //指定权限动态申请
    public void checkPermission(String sPermission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 判断是否有这个权限，是返回PackageManager.PERMISSION_GRANTED，否则是PERMISSION_DENIED
            // 这里我们要给应用授权所以是!= PackageManager.PERMISSION_GRANTED
            if (ContextCompat.checkSelfPermission(this, sPermission) != PackageManager.PERMISSION_GRANTED) {
                Log.i(TAG, "未获得权限");
                // 如果应用之前请求过此权限但用户拒绝了请求,且没有选择"不再提醒"选项 (后显示对话框解释为啥要这个权限)，此方法将返回 true。
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, sPermission)) {
                    Log.i(TAG, "用户永久拒绝权限申请");
                } else {
                    Log.i(TAG, "申请权限");
                    // requestPermissions以标准对话框形式请求权限。123是识别码（任意设置的整型），用来识别权限。应用无法配置或更改此对话框。
                    //当应用请求权限时，系统将向用户显示一个对话框。当用户响应时，系统将调用应用的 onRequestPermissionsResult() 方法，向其传递用户响应。您的应用必须替换该方法，以了解是否已获得相应权限。回调会将您传递的相同请求代码传递给 requestPermissions()。
                    ActivityCompat.requestPermissions(this, new String[]{sPermission}, 100);
                }
            }
            Log.i(TAG, "已获得权限");
        } else {
            Log.i(TAG, "无需动态申请");
        }
    }

    //开始预览
    private boolean startPreview() {
        int userId = viewModel.getUserId();

        if (userId == JavaInterface.USB_INVALID_USER_ID) {
            Log.i(TAG, "Can not to tartPreview()! for UserID is invalid: " + userId);
            return false;
        }

        thermalCameraView.setUserID(userId);//确定预览的设备

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);

        // 设置屏幕分辨率的长宽为屏幕像素的一半
        thermalCameraView.setScreenResolution(metric.widthPixels / 2, metric.heightPixels / 2);

        if (thermalCameraView.startPreview(mHolder)) {
            //预览成功
            Log.i(TAG, "StartPreview Success! iUserID:" + viewModel.getUserId());
            return true;
        } else {
            Log.e(TAG, "StartPreview failed! error:" + viewModel.getUsbLastError());
            Toast.makeText(this, "StartPreview failed! error:" + viewModel.getUsbLastError(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    //清理USBSDK资源
    private void cleanupUsbSdk() {
        if (viewModel.cleanUsbSdk()) {
            Log.i(TAG, "USB_Cleanup Success!");
        } else {
            Log.e(TAG, "USB_Cleanup Failed!");
            Toast.makeText(this, "USB_Cleanup Failed!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        thermalCameraView.stopPreview();
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(usbReceiver, filter);

        thermalCameraView.startPreview(mHolder);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.logoutDevice();
        cleanupUsbSdk();

        unregisterReceiver(usbReceiver);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

    }

    private void showEmptyCameraView() {
        mBinding.layoutEmptyCamera.setVisibility(View.VISIBLE);
    }

    private void hideEmptyCameraView() {
        mBinding.layoutEmptyCamera.setVisibility(View.GONE);
    }

}
package com.moyear.activity;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.hcusbsdk.Interface.JavaInterface;
import com.hcusbsdk.Interface.USB_DEVICE_INFO;
import com.hcusbsdk.Interface.USB_DEVICE_REG_RES;
import com.hcusbsdk.Interface.USB_USER_LOGIN_INFO;
import com.moyear.Config;
import com.moyear.R;
import com.moyear.Transfer;
import com.moyear.Upgrade;
import com.moyear.view.ThermalCameraView;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback {

    private static final String MTK_NODE_FILE = "/sys/devices/platform/1000b000.pinctrl/mt_gpio";
    private static final String MTK_VDD5V_FILE = "/sys/devices/platform/winteam_gpio/vdd5v_en";
    private static final String MTK_USB_DEVICE_FILE = "/sys/devices/platform/winteam_gpio/usb_device_en";
    private static final String MTK_IIRCAMERA_FILE = "/sys/devices/platform/winteam_gpio/usb_cam_en";

    private USB_DEVICE_INFO[] m_struDevInfoList = new USB_DEVICE_INFO[JavaInterface.MAX_DEVICE_NUM];    //设备信息列表
    private int m_dwDevCount = 0; //设备个数
    private boolean m_bInit = false; //是否初始化
    private int m_dwLoginDevIndex = 0; //默认登录第一个设备
    private int m_dwCurUserID = JavaInterface.USB_INVALID_USER_ID; //当前登录的设备句柄
    private long m_dwCommand = 0; //当前选择的配置功能
    private boolean m_bPreview = false; //是否开启预览

    //分辨率
    private static final String[] m_arrResolutionType = {"240 * 320(热成像)", "640 * 480(前端)", "640 * 360(传显)"};
    //配置选项
   /* private static final String[] m_arrConfigType = {"设置视频参数", "获取设备信息", "设备重启", "恢复默认",
            "获取硬件服务参数", "设置硬件服务参数", "获取系统本地时间", "设置系统本地时间", "获取图像亮度参数", "设置图像亮度参数",
            "获取图像对比度参数", "设置图像对比度参数", "一键背景校正", "诊断信息导出", "一键手动校正", "获取图像增强参数",
            "设置图像增强参数", "获取视频调整参数", "设置视频调整参数", "获取测温基本参数", "设置测温基本参数",
            "获取测温模式", "设置测温模式", "获取测温规则参数", "设置测温规则参数", "获取热成像相关算法版本信息",
            "获取热成像码流参数", "设置热成像码流参数", "获取测温修正参数", "设置测温修正参数", "获取黑体参数", "设置黑体参数",
            "获取体温补偿参数", "设置体温补偿参数", "获取热图", "区域最高温信息查询", "获取全屏测温参数", "设置全屏测温参数",
            "测温标定文件导出", "测温标定文件导入", "获取专家测温规则", "设置专家测温规则", "获取专家测温校正参数",
            "设置专家测温校正参数", "专家测温校正开始", "设置图像WDR", "获取音频输入状态", "获取温升参数", "设置温升参数",
            "设置专家测温校正参数", "专家测温校正开始", "获取温升参数", "设置温升参数", "获取环境温度校正参数", "设置环境温度校正参数",
            "带条件诊断信息导出(内部使用)"};*/

    private static final String[] m_arrConfigType = {
//            "设置视频参数", "获取设备信息", "设备重启", "恢复默认",
//            "获取硬件服务参数", "设置硬件服务参数", "获取系统本地时间", "设置系统本地时间", "获取图像亮度参数", "设置图像亮度参数",
            "获取图像对比度参数", "设置图像对比度参数", "一键背景校正", "诊断信息导出", "一键手动校正", "获取图像增强参数",
//            "设置图像增强参数",
            "获取视频调整参数", "设置视频调整参数",
            "获取测温基本参数", "设置测温基本参数",
            "获取测温模式", "设置测温模式", "获取测温规则参数", "设置测温规则参数", "获取热成像相关算法版本信息",
            "获取热成像码流参数", "设置热成像码流参数",
//            "获取测温修正参数", "设置测温修正参数", "获取黑体参数", "设置黑体参数",
//            "获取体温补偿参数", "设置体温补偿参数", "获取热图", "区域最高温信息查询", "获取全屏测温参数", "设置全屏测温参数",
            "测温标定文件导出", "测温标定文件导入", "获取专家测温规则", "设置专家测温规则",
            "获取专家测温校正参数",
            "设置专家测温校正参数", "专家测温校正开始",
//            "设置图像WDR", "获取音频输入状态", "获取温升参数", "设置温升参数",
//            "设置专家测温校正参数", "专家测温校正开始", "获取温升参数", "设置温升参数", "获取环境温度校正参数", "设置环境温度校正参数",
//            "带条件诊断信息导出(内部使用)"
    };
    //文件传输选项
    private static final String[] m_arrTransferType = {"日志文件导出", "音频数据导出", "设备加密"};
    //配置类对象
    private Config m_objConfig = new Config();
    //预览类对象
    private ThermalCameraView m_objPreview = null;
    //升级类对象
    private Upgrade m_objUpgrade = null;
    //文件传输对象
    private Transfer m_objTransfer = new Transfer();

    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";//可自定义
    private UsbDevice m_usbDevice = null;
    private UsbDeviceConnection m_DevConnect = null;

    private SurfaceView m_pSurfaceView = null;
    private SurfaceHolder m_pHolder = null;

    private TextView maxTmpTv = null;
    private TextView streamType = null;
    private TextView minTmpTv = null;
    private Timer timer = new Timer();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //创建MAX_DEVICE_NUM个设备信息对象
        for (int i = 0; i < JavaInterface.MAX_DEVICE_NUM; i++) {
            m_struDevInfoList[i] = new USB_DEVICE_INFO();
        }

        //初始化界面控件
        InitControl();

        m_objPreview = new ThermalCameraView(this);
        m_objUpgrade = new Upgrade(this);

        //读写文件权限动态申请  //高版本SDK下AndroidManifest.xml中配置的读写权限不起作用
        CheckPermission();
        //网络权限动态申请     //高版本SDK下AndroidManifest.xml中配置的网络权限不起作用
        CheckNetworkPermission();

        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                updateTmpValue();
            }
        }, 0, 1000);

        //setIIRCameraMode();

    }

    //初始化界面控件
    private boolean InitControl() {
        //Button监听
        Button btn_init = (Button) findViewById(R.id.btn_init);
        btn_init.setOnClickListener(new ButtonClickListener());

        Button btn_cleanup = (Button) findViewById(R.id.btn_cleanup);
        btn_cleanup.setOnClickListener(new ButtonClickListener());

        Button btn_exit = (Button) findViewById(R.id.btn_exit);
        btn_exit.setOnClickListener(new ButtonClickListener());

        Button btn_enum = (Button) findViewById(R.id.btn_enum);
        btn_enum.setOnClickListener(new ButtonClickListener());

        Button btn_login = (Button) findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new ButtonClickListener());

        Button btn_logout = (Button) findViewById(R.id.btn_logout);
        btn_logout.setOnClickListener(new ButtonClickListener());

        Button btn_upgrade = (Button) findViewById(R.id.btn_upgrade);
        btn_upgrade.setOnClickListener(new ButtonClickListener());

        Button btn_upgradeState = (Button) findViewById(R.id.btn_upgradeState);
        btn_upgradeState.setOnClickListener(new ButtonClickListener());

        Button btn_upgradeClose = (Button) findViewById(R.id.btn_upgradeClose);
        btn_upgradeClose.setOnClickListener(new ButtonClickListener());

        Button btn_startPreview = (Button) findViewById(R.id.btn_startPreview);
        btn_startPreview.setOnClickListener(new ButtonClickListener());

        Button btn_fileTransfer = (Button) findViewById(R.id.btn_fileTransfer);
        btn_fileTransfer.setOnClickListener(new ButtonClickListener());

        Button btn_config = (Button) findViewById(R.id.btn_config);
        btn_config.setOnClickListener(new ButtonClickListener());

        //初始化配置下拉列表
        Spinner spinConfigType = (Spinner) findViewById(R.id.spinner_config);
        ArrayAdapter<String> adaConfigType = new ArrayAdapter<String>(this
                , android.R.layout.simple_spinner_item, m_arrConfigType);
        adaConfigType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinConfigType.setAdapter(adaConfigType);
        spinConfigType.setOnItemSelectedListener(new OnItemSelectedListenerConfig());

        //初始化分辨率下拉列表
        Spinner spinResolutionType = (Spinner) findViewById(R.id.spinner_resolution);
        ArrayAdapter<String> adaResolutionType = new ArrayAdapter<String>(this
                , android.R.layout.simple_spinner_item, m_arrResolutionType);
        adaResolutionType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinResolutionType.setAdapter(adaResolutionType);
        spinResolutionType.setOnItemSelectedListener(new OnItemSelectedListenerConfig());

        //文件传输下拉列表
        Spinner spinTransferType = (Spinner) findViewById(R.id.spinner_transfer);
        ArrayAdapter<String> adaTransferType = new ArrayAdapter<String>(this
                , android.R.layout.simple_spinner_item, m_arrTransferType);
        adaTransferType.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinTransferType.setAdapter(adaTransferType);
        spinTransferType.setOnItemSelectedListener(new OnItemSelectedListenerConfig());

        //surfaceview
        m_pSurfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        m_pHolder = m_pSurfaceView.getHolder(); //得到surfaceView的holder，类似于surfaceView的控制器
        //把输送给surfaceView的视频画面，直接显示到屏幕上,不要维持它自身的缓冲区
        m_pHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        m_pHolder.addCallback(this);

        maxTmpTv = (TextView) findViewById(R.id.max_tmp_tv);
        streamType = (TextView) findViewById(R.id.stream_type);
        minTmpTv = (TextView) findViewById(R.id.min_tmp_tv);
        return true;
    }


    //初始化相关
    @SuppressLint("SdCardPath")
    private boolean InitUsbSdk() {
        CheckPermission(Manifest.permission.CAMERA);
        //初始化USBSDK
        if (JavaInterface.getInstance().USB_Init()) {
            Log.i("[USBDemo]", "USB_Init Success!");
        } else {
            Log.e("[USBDemo]", "USB_Init Failed!");
            Toast.makeText(this, "USB_Init Failed!", Toast.LENGTH_SHORT).show();
            return false;
        }

        //获取USBSDK版本
        String version = String.format("%08x", JavaInterface.getInstance().USB_GetSDKVersion());
        Log.i("[USBDemo]", "USB_GetSDKVersion :" + version);
        Toast.makeText(this, "USB_GetSDKVersion :" + version, Toast.LENGTH_SHORT).show();
        TextView textview = (TextView) findViewById(R.id.ver_textView);
        textview.setText("SDK Version: " + version);

        //开启USBSDK日志，参数说明见使用手册接口说明
        if (JavaInterface.getInstance().USB_SetLogToFile(JavaInterface.INFO_LEVEL, new String("/mnt/sdcard/sdklog/"), 1)) {
            Log.i("[USBDemo]", "USB_SetLogToFile Success!");
        } else {
            Log.e("[USBDemo]", "USB_SetLogToFile failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            Toast.makeText(this, "USB_SetLogToFile failed! error:" + JavaInterface.getInstance().USB_GetLastError(), Toast.LENGTH_SHORT).show();
            return false;
        }

        m_bInit = true;
        return true;
    }

    //清理USBSDK资源
    private void CleanupUsbSdk() {
        if (JavaInterface.getInstance().USB_Cleanup()) {
            Log.i("[USBDemo]", "USB_Cleanup Success!");
        } else {
            Log.e("[USBDemo]", "USB_Cleanup Failed!");
            Toast.makeText(this, "USB_Init Failed!", Toast.LENGTH_SHORT).show();
        }
        m_bInit = false;
    }

    //获取设备信息
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private boolean GetDeviceInfo() {
        //获取设备个数，第一次调用会申请设备权限，获取FD失败，用户确认权限后重新枚举，才能获取FD
        m_dwDevCount = JavaInterface.getInstance().USB_GetDeviceCount(this);
        m_dwDevCount = JavaInterface.getInstance().USB_GetDeviceCount(this);
        if (m_dwDevCount > 0) {
            Log.i("[USBDemo]", "USB_GetDeviceCount Device count is :" + m_dwDevCount);
            Toast.makeText(this, "USB_GetDeviceCount Device count is :" + m_dwDevCount, Toast.LENGTH_SHORT).show();
        } else if (m_dwDevCount == 0) {
            Log.i("[USBDemo]", "USB_GetDeviceCount Device count is :" + m_dwDevCount);
            Toast.makeText(this, "USB_GetDeviceCount Device count is :" + m_dwDevCount, Toast.LENGTH_SHORT).show();
            return false;
        } else {
            Log.e("[USBDemo]", "USB_GetDeviceCount failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            Toast.makeText(this, "USB_GetDeviceCount failed! error:" + JavaInterface.getInstance().USB_GetLastError(), Toast.LENGTH_SHORT).show();
            return false;
        }

        //获取设备信息
        if (JavaInterface.getInstance().USB_EnumDevices(m_dwDevCount, m_struDevInfoList)) {
            //打印设备信息
            for (int i = 0; i < m_dwDevCount; i++) {
                Log.i("[USBDemo]", "USB_EnumDevices Device info is dwIndex:" + m_struDevInfoList[i].dwIndex +
                        " dwVID:" + m_struDevInfoList[i].dwVID +
                        " dwPID:" + m_struDevInfoList[i].dwPID +
                        " szManufacturer:" + m_struDevInfoList[i].szManufacturer +
                        " szDeviceName:" + m_struDevInfoList[i].szDeviceName +
                        " szSerialNumber:" + m_struDevInfoList[i].szSerialNumber +
                        " byHaveAudio:" + m_struDevInfoList[i].byHaveAudio);
            }
        } else {
            Log.e("[USBDemo]", "USB_EnumDevices failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            Toast.makeText(this, "USB_EnumDevices failed! error:" + JavaInterface.getInstance().USB_GetLastError(), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //系统没有root过，通过demo层获取设备Fd
    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean LoginDeviceWithFd() {

        //GetDevFd();  //获取设备描述符fd
        USB_USER_LOGIN_INFO struUserLoginInfo = new USB_USER_LOGIN_INFO();
        struUserLoginInfo.dwTimeout = 5000;
        struUserLoginInfo.dwDevIndex = m_struDevInfoList[m_dwLoginDevIndex].dwIndex;
        struUserLoginInfo.dwVID = m_struDevInfoList[m_dwLoginDevIndex].dwVID;
        struUserLoginInfo.dwPID = m_struDevInfoList[m_dwLoginDevIndex].dwPID;
        struUserLoginInfo.dwFd = m_struDevInfoList[m_dwLoginDevIndex].dwFd;
//        struUserLoginInfo.byLoginMode = 0;
//        struUserLoginInfo.szUserName = "admin"; //如果是门禁设备，需要输入用户名和密码
//        struUserLoginInfo.szPassword = "12345"; //如果是门禁设备，需要输入用户名和密码

        USB_DEVICE_REG_RES struDeviceRegRes = new USB_DEVICE_REG_RES();

        //获取设备信息
        m_dwCurUserID = JavaInterface.getInstance().USB_Login(struUserLoginInfo, struDeviceRegRes);
        if (m_dwCurUserID != JavaInterface.USB_INVALID_USER_ID) {
            //登录成功
            Log.i("[USBDemo]", "LoginDeviceWithFd Success! iUserID:" + m_dwCurUserID +
                    " dwDevIndex:" + struUserLoginInfo.dwDevIndex +
                    " dwVID:" + struUserLoginInfo.dwVID +
                    " dwPID:" + struUserLoginInfo.dwPID +
                    " dwFd:" + struUserLoginInfo.dwFd);
            Toast.makeText(this, "LoginDeviceWithFd Success! iUserID:" + m_dwCurUserID, Toast.LENGTH_SHORT).show();
        } else {
            Log.e("[USBDemo]", "LoginDeviceWithFd failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " dwDevIndex:" + struUserLoginInfo.dwDevIndex +
                    " dwVID:" + struUserLoginInfo.dwVID +
                    " dwPID:" + struUserLoginInfo.dwPID +
                    " dwFd:" + struUserLoginInfo.dwFd);
            Toast.makeText(this, "LoginDeviceWithFd failed! error:" + JavaInterface.getInstance().USB_GetLastError(), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    //注销设备
    private boolean LogoutDevice() {
        if (JavaInterface.getInstance().USB_Logout(m_dwCurUserID)) {
            //登录成功
            Log.i("[USBDemo]", "USB_Logout Success! iUserID:" + m_dwCurUserID);
            Toast.makeText(this, "USB_Logout Success! iUserID:" + m_dwCurUserID, Toast.LENGTH_SHORT).show();
            m_dwCurUserID = JavaInterface.USB_INVALID_USER_ID;
            return true;
        } else {
            Log.e("[USBDemo]", "USB_Logout failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            Toast.makeText(this, "USB_Logout failed! error:" + JavaInterface.getInstance().USB_GetLastError(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    //升级设备
    private boolean UpgradeDevice() {
        m_objUpgrade.SetUserID(m_dwCurUserID);

        if (m_objUpgrade.StartUpgrade()) {
            Log.i("[USBDemo]", "StartUpgrade Success! iUserID:" + m_dwCurUserID);
            Toast.makeText(this, "StartUpgrade Success! iUserID:" + m_dwCurUserID, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Log.e("[USBDemo]", "StartUpgrade failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            Toast.makeText(this, "StartUpgrade failed! error:" + JavaInterface.getInstance().USB_GetLastError(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    //获取升级状态
    private boolean GetUpgradeState() {
        if (m_objUpgrade.GetUpgradeState()) {
            Log.i("[USBDemo]", "GetUpgradeState Success! iUserID:" + m_dwCurUserID);
            Toast.makeText(this, "GetUpgradeState Success! iUserID:" + m_dwCurUserID, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Log.e("[USBDemo]", "GetUpgradeState failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            Toast.makeText(this, "GetUpgradeState failed! error:" + JavaInterface.getInstance().USB_GetLastError(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    //关闭升级
    private boolean CloseUpgrade() {
        if (m_objUpgrade.StopUpgrade()) {
            Log.i("[USBDemo]", "StopUpgrade Success! iUserID:" + m_dwCurUserID);
            Toast.makeText(this, "StopUpgrade Success! iUserID:" + m_dwCurUserID, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Log.e("[USBDemo]", "StopUpgrade failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            Toast.makeText(this, "StopUpgrade failed! error:" + JavaInterface.getInstance().USB_GetLastError(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    //开始预览
    private boolean StartPreview() {
        m_objPreview.setUserID(m_dwCurUserID);//确定预览的设备

        DisplayMetrics metric = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metric);
        m_objPreview.setScreenResolution(metric.widthPixels / 2, metric.heightPixels / 2);

        if (m_objPreview.startPreview(m_pHolder)) {
            //预览成功
            Log.i("[USBDemo]", "StartPreview Success! iUserID:" + m_dwCurUserID);
            Toast.makeText(this, "StartPreview Success! iUserID:" + m_dwCurUserID, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Log.e("[USBDemo]", "StartPreview failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            Toast.makeText(this, "StartPreview failed! error:" + JavaInterface.getInstance().USB_GetLastError(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    //停止预览
    private boolean StopPreview() {
        if (m_objPreview.stopPreview()) {
            //关闭成功
            Log.i("[USBDemo]", "StopPreview Success! iUserID:" + m_dwCurUserID);
            Toast.makeText(this, "StopPreview Success! iUserID:" + m_dwCurUserID, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            Log.e("[USBDemo]", "StopPreview failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            Toast.makeText(this, "StopPreview failed! error:" + JavaInterface.getInstance().USB_GetLastError(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    //参数配置
    private boolean ConfigDevice() {
        if (m_objConfig.Config(m_dwCurUserID, m_dwCommand)) {
            //配置成功
            Log.i("[USBDemo]", "Config Success! m_dwCommand:" + m_dwCommand);
            Toast.makeText(this, "Config Success! m_dwCommand:" + m_dwCommand, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            //配置失败
            Log.e("[USBDemo]", "Config failed! m_dwCommand:" + m_dwCommand +
                    " error:" + JavaInterface.getInstance().USB_GetLastError());
            Toast.makeText(this, "Config failed! m_dwCommand:" + m_dwCommand + " error:" + JavaInterface.getInstance().USB_GetLastError(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    //文件传输
    private boolean FileTransfer() {
        if (m_objTransfer.Transfer(m_dwCurUserID, m_dwCommand)) {
            //传输成功
            Log.i("[USBDemo]", "Transfer Success! m_dwCommand:" + m_dwCommand);
            Toast.makeText(this, "Transfer Success! m_dwCommand:" + m_dwCommand, Toast.LENGTH_SHORT).show();
            return true;
        } else {
            //传输失败
            Log.e("[USBDemo]", "Transfer failed! m_dwCommand:" + m_dwCommand +
                    " error:" + JavaInterface.getInstance().USB_GetLastError());
            Toast.makeText(this, "Transfer failed! m_dwCommand:" + m_dwCommand + " error:" + JavaInterface.getInstance().USB_GetLastError(), Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    //按钮监听函数
    private class ButtonClickListener implements View.OnClickListener {
        @SuppressLint("NewApi")
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.btn_init:
                    if (m_bInit) {
                        Log.i("[USBDemo]", "Init Success");
                        Toast.makeText(MainActivity.this, "Init Success", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //初始化USBSDK
                    InitUsbSdk();
                    break;
                case R.id.btn_enum:
                    if (!m_bInit) {
                        Log.i("[USBDemo]", "No Init");
                        Toast.makeText(MainActivity.this, "No Init", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //枚举设备信息
                    GetDeviceInfo();
                    break;
                case R.id.btn_login:
                    if (!m_bInit) {
                        Log.i("[USBDemo]", "No Init");
                        Toast.makeText(MainActivity.this, "No Init", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    //登录设备
                    LoginDeviceWithFd();
                    break;
                case R.id.btn_logout:
                    if (!m_bInit) {
                        Log.i("[USBDemo]", "No Init");
                        Toast.makeText(MainActivity.this, "No Init", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //注销设备
                    LogoutDevice();
                    break;
                case R.id.btn_upgrade:
                    if (!m_bInit) {
                        Log.i("[USBDemo]", "No Init");
                        Toast.makeText(MainActivity.this, "No Init", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //注销设备
                    UpgradeDevice();
                    break;
                case R.id.btn_upgradeState:
                    if (!m_bInit) {
                        Log.i("[USBDemo]", "No Init");
                        Toast.makeText(MainActivity.this, "No Init", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //注销设备
                    GetUpgradeState();
                    break;
                case R.id.btn_upgradeClose:
                    if (!m_bInit) {
                        Log.i("[USBDemo]", "No Init");
                        Toast.makeText(MainActivity.this, "No Init", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //注销设备
                    CloseUpgrade();
                    break;
                case R.id.btn_startPreview:
                    if (!m_bInit) {
                        Log.i("[USBDemo]", "No Init");
                        Toast.makeText(MainActivity.this, "No Init", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!m_bPreview) {
                        //开始预览
                        if (StartPreview()) {
                            Button btn_startPreview = (Button) findViewById(R.id.btn_startPreview);
                            btn_startPreview.setText("停止预览");
                            m_bPreview = true;
                        }
                    } else {
                        //关闭预览
                        if (StopPreview()) {
                            Button btn_startPreview = (Button) findViewById(R.id.btn_startPreview);
                            btn_startPreview.setText("开始预览");
                            m_bPreview = false;
                        }
                    }
                    break;
                case R.id.btn_fileTransfer:
                    if (!m_bInit) {
                        Log.i("[USBDemo]", "No Init");
                        Toast.makeText(MainActivity.this, "No Init", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //文件传输
                    FileTransfer();
                    break;
                case R.id.btn_config:
                    if (!m_bInit) {
                        Log.i("[USBDemo]", "No Init");
                        Toast.makeText(MainActivity.this, "No Init", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //参数配置
                    ConfigDevice();
                    break;
                case R.id.btn_cleanup:
                    if (!m_bInit) {
                        Log.i("[USBDemo]", "No Init");
                        Toast.makeText(MainActivity.this, "No Init", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    //清理USBSDK资源
                    CleanupUsbSdk();
                    break;
                case R.id.btn_exit:
                    if (m_bInit) {
                        //清理USBSDK资源
                        CleanupUsbSdk();
                    }
                    //setUsbMode();
                    Runtime.getRuntime().exit(0);
                    break;


                default:
                    break;
            }
        }
    }

    //Spinner事件
    private class OnItemSelectedListenerConfig implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            if (parent.getId() == R.id.spinner_resolution) {
                Spinner spin = (Spinner) findViewById(R.id.spinner_resolution);
                long iIndex = spin.getSelectedItemId();
                switch ((int) iIndex) {
                    case 0:
//                        m_objPreview.SetStreamResolution(240, 320);
                        break;
                    case 1:
//                        m_objPreview.SetStreamResolution(640, 480);
                        break;
                    case 2:
//                        m_objPreview.SetStreamResolution(640, 360);
                        break;
                    default:
                        break;
                }
            } else if (parent.getId() == R.id.spinner_config) {
                Spinner spin = (Spinner) findViewById(R.id.spinner_config);
                String szConfigName = spin.getSelectedItem().toString();
                switch (szConfigName) {
                    case "设置视频参数":
                        m_dwCommand = JavaInterface.USB_SET_VIDEO_PARAM;
                        break;
                    case "获取设备信息":
                        m_dwCommand = JavaInterface.USB_GET_SYSTEM_DEVICE_INFO;
                        break;
                    case "设备重启":
                        m_dwCommand = JavaInterface.USB_SET_SYSTEM_REBOOT;
                        break;
                    case "恢复默认":
                        m_dwCommand = JavaInterface.USB_SET_SYSTEM_RESET;
                        break;
                    case "获取硬件服务参数":
                        m_dwCommand = JavaInterface.USB_GET_SYSTEM_HARDWARE_SERVER;
                        break;
                    case "设置硬件服务参数":
                        m_dwCommand = JavaInterface.USB_SET_SYSTEM_HARDWARE_SERVER;
                        break;
                    case "获取系统本地时间":
                        m_dwCommand = JavaInterface.USB_GET_SYSTEM_LOCALTIME;
                        break;
                    case "设置系统本地时间":
                        m_dwCommand = JavaInterface.USB_SET_SYSTEM_LOCALTIME;
                        break;
                    case "获取图像亮度参数":
                        m_dwCommand = JavaInterface.USB_GET_IMAGE_BRIGHTNESS;
                        break;
                    case "设置图像亮度参数":
                        m_dwCommand = JavaInterface.USB_SET_IMAGE_BRIGHTNESS;
                        break;
                    case "获取图像对比度参数":
                        m_dwCommand = JavaInterface.USB_GET_IMAGE_CONTRAST;
                        break;
                    case "设置图像对比度参数":
                        m_dwCommand = JavaInterface.USB_SET_IMAGE_CONTRAST;
                        break;
                    case "一键背景校正":
                        m_dwCommand = JavaInterface.USB_SET_IMAGE_BACKGROUND_CORRECT;
                        break;
                    case "诊断信息导出":
                        m_dwCommand = JavaInterface.USB_GET_SYSTEM_DIAGNOSED_DATA;
                        break;
                    case "一键手动校正":
                        m_dwCommand = JavaInterface.USB_SET_IMAGE_MANUAL_CORRECT;
                        break;
                    case "获取图像增强参数":
                        m_dwCommand = JavaInterface.USB_GET_IMAGE_ENHANCEMENT;
                        break;
                    case "设置图像增强参数":
                        m_dwCommand = JavaInterface.USB_SET_IMAGE_ENHANCEMENT;
                        break;
                    case "获取视频调整参数":
                        m_dwCommand = JavaInterface.USB_GET_IMAGE_VIDEO_ADJUST;
                        break;
                    case "设置视频调整参数":
                        m_dwCommand = JavaInterface.USB_SET_IMAGE_VIDEO_ADJUST;
                        break;
                    case "获取测温基本参数":
                        m_dwCommand = JavaInterface.USB_GET_THERMOMETRY_BASIC_PARAM;
                        break;
                    case "设置测温基本参数":
                        m_dwCommand = JavaInterface.USB_SET_THERMOMETRY_BASIC_PARAM;
                        break;
                    case "获取测温模式":
                        m_dwCommand = JavaInterface.USB_GET_THERMOMETRY_MODE;
                        break;
                    case "设置测温模式":
                        m_dwCommand = JavaInterface.USB_SET_THERMOMETRY_MODE;
                        break;
                    case "获取测温规则参数":
                        m_dwCommand = JavaInterface.USB_GET_THERMOMETRY_REGIONS;
                        break;
                    case "设置测温规则参数":
                        m_dwCommand = JavaInterface.USB_SET_THERMOMETRY_REGIONS;
                        break;
                    case "获取热成像相关算法版本信息":
                        m_dwCommand = JavaInterface.USB_GET_THERMAL_ALG_VERSION;
                        break;
                    case "获取热成像码流参数":
                        m_dwCommand = JavaInterface.USB_GET_THERMAL_STREAM_PARAM;
                        break;
                    case "设置热成像码流参数":
                        m_dwCommand = JavaInterface.USB_SET_THERMAL_STREAM_PARAM;
                        break;
                    case "获取测温修正参数":
                        m_dwCommand = JavaInterface.USB_GET_TEMPERATURE_CORRECT;
                        break;
                    case "设置测温修正参数":
                        m_dwCommand = JavaInterface.USB_SET_TEMPERATURE_CORRECT;
                        break;
                    case "获取黑体参数":
                        m_dwCommand = JavaInterface.USB_GET_BLACK_BODY;
                        break;
                    case "设置黑体参数":
                        m_dwCommand = JavaInterface.USB_SET_BLACK_BODY;
                        break;
                    case "获取体温补偿参数":
                        m_dwCommand = JavaInterface.USB_GET_BODYTEMP_COMPENSATION;
                        break;
                    case "设置体温补偿参数":
                        m_dwCommand = JavaInterface.USB_SET_BODYTEMP_COMPENSATION;
                        break;
                    case "获取热图":
                        m_dwCommand = JavaInterface.USB_GET_JPEGPIC_WITH_APPENDDATA;
                        break;
                    case "区域最高温信息查询":
                        m_dwCommand = JavaInterface.USB_GET_ROI_MAX_TEMPERATURE_SEARCH;
                        break;
                    case "获取全屏测温参数":
                        m_dwCommand = JavaInterface.USB_GET_P2P_PARAM;
                        break;
                    case "设置全屏测温参数":
                        m_dwCommand = JavaInterface.USB_SET_P2P_PARAM;
                        break;
                    case "测温标定文件导出":
                        m_dwCommand = JavaInterface.USB_GET_THERMOMETRY_CALIBRATION_FILE;
                        break;
                    case "测温标定文件导入":
                        m_dwCommand = JavaInterface.USB_SET_THERMOMETRY_CALIBRATION_FILE;
                        break;
                    case "获取专家测温规则":
                        m_dwCommand = JavaInterface.USB_GET_THERMOMETRY_EXPERT_REGIONS;
                        break;
                    case "设置专家测温规则":
                        m_dwCommand = JavaInterface.USB_SET_THERMOMETRY_EXPERT_REGIONS;
                        break;
                    case "获取专家测温校正参数":
                        m_dwCommand = JavaInterface.USB_GET_EXPERT_CORRECTION_PARAM;
                        break;
                    case "设置专家测温校正参数":
                        m_dwCommand = JavaInterface.USB_SET_EXPERT_CORRECTION_PARAM;
                        break;
                    case "专家测温校正开始":
                        m_dwCommand = JavaInterface.USB_START_EXPERT_CORRECTION;
                        break;
                    case "设置图像WDR":
                        m_dwCommand = JavaInterface.USB_SET_IMAGE_WDR;
                        break;
                    case "获取音频输入状态":
                        m_dwCommand = JavaInterface.USB_GET_AUDIO_IN_STATUS;
                        break;
                    case "获取温升参数":
                        m_dwCommand = JavaInterface.USB_GET_THERMOMETRY_RISE_SETTINGS;
                        break;
                    case "设置温升参数":
                        m_dwCommand = JavaInterface.USB_SET_THERMOMETRY_RISE_SETTINGS;
                        break;
                    case "获取环境温度校正参数":
                        m_dwCommand = JavaInterface.USB_GET_ENVIROTEMPERATURE_CORRECT;
                        break;
                    case "设置环境温度校正参数":
                        m_dwCommand = JavaInterface.USB_SET_ENVIROTEMPERATURE_CORRECT;
                        break;
                    case "带条件诊断信息导出(内部使用)":
                        m_dwCommand = JavaInterface.USB_GET_SYSTEM_DIAGNOSED_DATA_EX;
                        break;
                    default:
                        break;
                }
                Log.i("[USBDemo]", "选择的配置是   m_dwCommand:" + m_dwCommand);
            } else if (parent.getId() == R.id.spinner_transfer) {
                Spinner spin = (Spinner) findViewById(R.id.spinner_transfer);
                String szTransferName = spin.getSelectedItem().toString();
                switch (szTransferName) {
                    //"日志文件导出", "音频数据导出", "设备加密"
                    case "日志文件导出":
                        m_dwCommand = JavaInterface.USB_GET_SYSTEM_LOG_DATA;
                        break;
                    case "音频数据导出":
                        m_dwCommand = JavaInterface.USB_GET_AUDIO_DUMP_DATA;
                        break;
                    case "设备加密":
                        m_dwCommand = JavaInterface.USB_SET_SYSTEM_ENCRYPT_DATA;
                        break;
                    default:
                        break;
                }
                Log.i("[USBDemo]", "选择的文件类型是   m_dwCommand:" + m_dwCommand);
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    }

    private void ExecShell(String cmd) {
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", cmd});
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String readLine = br.readLine();
            while (readLine != null) {
                System.out.println(readLine);
                readLine = br.readLine();
            }
            if (br != null) {
                br.close();
            }
            p.destroy();
            p = null;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    //读写文件权限动态申请
    public void CheckPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 判断是否有这个权限，是返回PackageManager.PERMISSION_GRANTED，否则是PERMISSION_DENIED
            // 这里我们要给应用授权所以是!= PackageManager.PERMISSION_GRANTED
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.i("[USBDemo]", "未获得读写权限");
                // 如果应用之前请求过此权限但用户拒绝了请求,且没有选择"不再提醒"选项 (后显示对话框解释为啥要这个权限)，此方法将返回 true。
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    Log.i("[USBDemo]", "用户永久拒绝权限申请");
                } else {
                    Log.i("[USBDemo]", "申请权限");
                    // requestPermissions以标准对话框形式请求权限。123是识别码（任意设置的整型），用来识别权限。应用无法配置或更改此对话框。
                    //当应用请求权限时，系统将向用户显示一个对话框。当用户响应时，系统将调用应用的 onRequestPermissionsResult() 方法，向其传递用户响应。您的应用必须替换该方法，以了解是否已获得相应权限。回调会将您传递的相同请求代码传递给 requestPermissions()。
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
                }
            }
            Log.i("[USBDemo]", "已获得读写权限");
        } else {
            Log.i("[USBDemo]", "无需动态申请");
        }
    }

    //指定权限动态申请
    public void CheckPermission(String sPermission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 判断是否有这个权限，是返回PackageManager.PERMISSION_GRANTED，否则是PERMISSION_DENIED
            // 这里我们要给应用授权所以是!= PackageManager.PERMISSION_GRANTED
            if (ContextCompat.checkSelfPermission(this, sPermission) != PackageManager.PERMISSION_GRANTED) {
                Log.i("[USBDemo]", "未获得权限");
                // 如果应用之前请求过此权限但用户拒绝了请求,且没有选择"不再提醒"选项 (后显示对话框解释为啥要这个权限)，此方法将返回 true。
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, sPermission)) {
                    Log.i("[USBDemo]", "用户永久拒绝权限申请");
                } else {
                    Log.i("[USBDemo]", "申请权限");
                    // requestPermissions以标准对话框形式请求权限。123是识别码（任意设置的整型），用来识别权限。应用无法配置或更改此对话框。
                    //当应用请求权限时，系统将向用户显示一个对话框。当用户响应时，系统将调用应用的 onRequestPermissionsResult() 方法，向其传递用户响应。您的应用必须替换该方法，以了解是否已获得相应权限。回调会将您传递的相同请求代码传递给 requestPermissions()。
                    ActivityCompat.requestPermissions(this, new String[]{sPermission}, 100);
                }
            }
            Log.i("[USBDemo]", "已获得权限");
        } else {
            Log.i("[USBDemo]", "无需动态申请");
        }
    }

    //网络权限动态申请
    public void CheckNetworkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // 判断是否有这个权限，是返回PackageManager.PERMISSION_GRANTED，否则是PERMISSION_DENIED
            // 这里我们要给应用授权所以是!= PackageManager.PERMISSION_GRANTED
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                Log.i("[USBDemo]", "未获得网络权限");
                // 如果应用之前请求过此权限但用户拒绝了请求,且没有选择"不再提醒"选项 (后显示对话框解释为啥要这个权限)，此方法将返回 true。
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.INTERNET)) {
                    Log.i("[USBDemo]", "用户永久拒绝权限申请");
                } else {
                    Log.i("[USBDemo]", "申请权限");
                    // requestPermissions以标准对话框形式请求权限。123是识别码（任意设置的整型），用来识别权限。应用无法配置或更改此对话框。
                    //当应用请求权限时，系统将向用户显示一个对话框。当用户响应时，系统将调用应用的 onRequestPermissionsResult() 方法，向其传递用户响应。您的应用必须替换该方法，以了解是否已获得相应权限。回调会将您传递的相同请求代码传递给 requestPermissions()。
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET}, 100);
                }
            }
            Log.i("[USBDemo]", "已获得网络权限");
        } else {
            Log.i("[USBDemo]", "无需动态申请");
        }
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

    public void updateTmpValue() {

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //更改UI；
                maxTmpTv.setText("最高温：" + m_objPreview.getMaxTmp());
                streamType.setText("码流类型：" + m_objPreview.getStreamType());
                minTmpTv.setText("最低温：" + m_objPreview.getMinTmp());
            }
        });

    }

    private static boolean writeFileNode(String nodeFile, String value) {
        File file = new File(nodeFile);
        java.io.FileWriter fr = null;
        boolean flag = false;
        try {
            fr = new java.io.FileWriter(file);
            fr.write(value);
            fr.close();
            fr = null;
            flag = true;
        } catch (IOException e) {
            Log.e("[USBDemo]", "writeFileNode=>error: ", e);
            flag = false;
        } finally {
            try {
                if (fr != null) {
                    fr.close();
                }
            } catch (IOException e) {
            }
        }
        return flag;
    }

    public static boolean setIIRCameraMode() {
        writeFileNode(MTK_VDD5V_FILE, "1");
        writeFileNode(MTK_USB_DEVICE_FILE, "0");

        boolean result = writeFileNode(MTK_IIRCAMERA_FILE, "1");

        return result;
    }

    public static boolean setUsbMode() {
        writeFileNode(MTK_VDD5V_FILE, "0");
        writeFileNode(MTK_USB_DEVICE_FILE, "1");

        boolean result = writeFileNode(MTK_IIRCAMERA_FILE, "0");

        return result;
    }

}
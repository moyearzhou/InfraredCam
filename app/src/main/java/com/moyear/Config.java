package com.moyear;

import android.util.Log;

import com.hcusbsdk.Interface.JavaInterface;
import com.hcusbsdk.Interface.USB_AUDIO_STATUS;
import com.hcusbsdk.Interface.USB_BLACK_BODY;
import com.hcusbsdk.Interface.USB_BODYTEMP_COMPENSATION;
import com.hcusbsdk.Interface.USB_IMAGE_BRIGHTNESS;
import com.hcusbsdk.Interface.USB_IMAGE_CONTRAST;
import com.hcusbsdk.Interface.USB_IMAGE_ENHANCEMENT;
import com.hcusbsdk.Interface.USB_IMAGE_VIDEO_ADJUST;
import com.hcusbsdk.Interface.USB_IMAGE_WDR;
import com.hcusbsdk.Interface.USB_JPEGPIC_WITH_APPENDDATA;
import com.hcusbsdk.Interface.USB_P2P_PARAM;
import com.hcusbsdk.Interface.USB_ROI_MAX_TEMPERATURE_SEARCH;
import com.hcusbsdk.Interface.USB_ROI_MAX_TEMPERATURE_SEARCH_RESULT;
import com.hcusbsdk.Interface.USB_SYSTEM_DEVICE_INFO;
import com.hcusbsdk.Interface.USB_SYSTEM_DIAGNOSED_DATA;
import com.hcusbsdk.Interface.USB_SYSTEM_DIAGNOSED_DATA_COND;
import com.hcusbsdk.Interface.USB_SYSTEM_HARDWARE_SERVER;
import com.hcusbsdk.Interface.USB_SYSTEM_LOCALTIME;
import com.hcusbsdk.Interface.USB_TEMPERATURE_CORRECT;
import com.hcusbsdk.Interface.USB_THERMAL_ALG_VERSION;
import com.hcusbsdk.Interface.USB_THERMAL_STREAM_PARAM;
import com.hcusbsdk.Interface.USB_THERMOMETRY_BASIC_PARAM;
import com.hcusbsdk.Interface.USB_THERMOMETRY_CALIBRATION_FILE;
import com.hcusbsdk.Interface.USB_THERMOMETRY_EXPERT_CORRECTION_PARAM;
import com.hcusbsdk.Interface.USB_THERMOMETRY_EXPERT_REGIONS;
import com.hcusbsdk.Interface.USB_THERMOMETRY_MODE;
import com.hcusbsdk.Interface.USB_THERMOMETRY_REGIONS;
import com.hcusbsdk.Interface.USB_THERMOMETRY_RISE_SETTINGS;
import com.hcusbsdk.Interface.USB_ENVIROTEMPERATURE_CORRECT;
import com.hcusbsdk.Interface.USB_VIDEO_PARAM;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;

public class Config {
    //选择具体的配置功能
    public boolean Config(int lUserID, long m_dwCommand) {
        boolean bRet = false;
        switch ((int)m_dwCommand)
        {
            case JavaInterface.USB_SET_VIDEO_PARAM: //设置视频参数
                bRet = SetVideoParam(lUserID);
                break;
            case JavaInterface.USB_GET_SYSTEM_DEVICE_INFO: //获取设备信息
                bRet = USB_GetSysTemDeviceInfo(lUserID);
                break;
            case JavaInterface.USB_SET_SYSTEM_REBOOT: //设备重启
                bRet = USB_SetSystemReboot(lUserID);
                break;
            case JavaInterface.USB_SET_SYSTEM_RESET: //恢复默认
                bRet = USB_SetSystemReset(lUserID);
                break;
            case JavaInterface.USB_GET_SYSTEM_HARDWARE_SERVER: //获取硬件服务参数
                bRet = USB_GetSystemHardwareServer(lUserID);
                break;
            case JavaInterface.USB_SET_SYSTEM_HARDWARE_SERVER: //设置硬件服务参数
                bRet = USB_SetSystemHardwareServer(lUserID);
                break;
            case JavaInterface.USB_GET_SYSTEM_LOCALTIME: //获取系统本地时间
                bRet = USB_GetSystemLocalTime(lUserID);
                break;
            case JavaInterface.USB_SET_SYSTEM_LOCALTIME: //设置系统本地时间
                bRet = USB_SetSystemLocalTime(lUserID);
                break;
            case JavaInterface.USB_GET_IMAGE_BRIGHTNESS: //获取图像亮度参数
                bRet = USB_GetImageBrightNess(lUserID);
                break;
            case JavaInterface.USB_SET_IMAGE_BRIGHTNESS: //设置图像亮度参数
                bRet = USB_SetImageBrightNess(lUserID);
                break;
            case JavaInterface.USB_GET_IMAGE_CONTRAST: //获取图像对比度参数
                bRet = USB_GetImageContrast(lUserID);
                break;
            case JavaInterface.USB_SET_IMAGE_CONTRAST: //设置图像对比度参数
                bRet = USB_SetImageContrast(lUserID);
                break;
            case JavaInterface.USB_SET_IMAGE_BACKGROUND_CORRECT: //一键背景校正
                bRet = USB_SetImageBackGroundCorrect(lUserID);
                break;
            case JavaInterface.USB_GET_SYSTEM_DIAGNOSED_DATA: //诊断信息导出
                bRet = USB_GetSystemDiagnosedData(lUserID);
                break;
            case JavaInterface.USB_GET_SYSTEM_DIAGNOSED_DATA_EX: //带参数条件诊断信息导出
                bRet = USB_GetSystemDiagnosedDataEx(lUserID);
                break;
            case JavaInterface.USB_SET_IMAGE_MANUAL_CORRECT: //一键手动校正
                bRet = USB_SetImageManualCorrect(lUserID);
                break;
            case JavaInterface.USB_GET_IMAGE_ENHANCEMENT: //获取图像增强参数
                bRet = USB_GetImageEnhancement(lUserID);
                break;
            case JavaInterface.USB_SET_IMAGE_ENHANCEMENT: //设置图像增强参数
                bRet = USB_SetImageEnhancement(lUserID);
                break;
            case JavaInterface.USB_GET_IMAGE_VIDEO_ADJUST: //获取视频调整参数
                bRet = USB_GetImageVideoAdjust(lUserID);
                break;
            case JavaInterface.USB_SET_IMAGE_VIDEO_ADJUST: //设置视频调整参数
                bRet = USB_SetImageVideoAdjust(lUserID);
                break;
            case JavaInterface.USB_GET_THERMOMETRY_BASIC_PARAM: //获取测温基本参数
                bRet = USB_GetThermometryBasicParam(lUserID);
                break;
            case JavaInterface.USB_SET_THERMOMETRY_BASIC_PARAM: //设置测温进本参数
                bRet = USB_SetThermometryBasicParam(lUserID);
                break;
            case JavaInterface.USB_GET_THERMOMETRY_MODE: //获取测温模式
                bRet = USB_GetThermometryMode(lUserID);
                break;
            case JavaInterface.USB_SET_THERMOMETRY_MODE: //设置测温模式
                bRet = USB_SetThermometryMode(lUserID);
                break;
            case JavaInterface.USB_GET_THERMOMETRY_REGIONS: //获取测温规则参数
                bRet = USB_GetThermometryRegions(lUserID);
                break;
            case JavaInterface.USB_SET_THERMOMETRY_REGIONS: //设置测温规则参数
                bRet = USB_SetThermometryRegions(lUserID);
                break;
            case JavaInterface.USB_GET_THERMAL_ALG_VERSION: //获取热成像相关算法版本信息
                bRet = USB_GetThermalAlgVersion(lUserID);
                break;
            case JavaInterface.USB_GET_THERMAL_STREAM_PARAM: //获取热成像码流参数
                bRet = USB_GetThermalStreamParam(lUserID);
                break;
            case JavaInterface.USB_SET_THERMAL_STREAM_PARAM: //设置热成像码流参数
                bRet = USB_SetThermalStreamParam(lUserID);
                break;
            case JavaInterface.USB_GET_TEMPERATURE_CORRECT: //获取测温修正参数
                bRet = USB_GetTemperatureCorrect(lUserID);
                break;
            case JavaInterface.USB_SET_TEMPERATURE_CORRECT: //设置测温修正参数
                bRet = USB_SetTemperatureCorrect(lUserID);
                break;
            case JavaInterface.USB_GET_BLACK_BODY: //获取黑体参数
                bRet = USB_GetBlackBody(lUserID);
                break;
            case JavaInterface.USB_SET_BLACK_BODY: //设置黑体参数
                bRet = USB_SetBlackBody(lUserID);
                break;
            case JavaInterface.USB_GET_BODYTEMP_COMPENSATION: //获取体温补偿参数
                bRet = USB_GetBodyTemperatureCompensation(lUserID);
                break;
            case JavaInterface.USB_SET_BODYTEMP_COMPENSATION: //设置体温补偿参数
                bRet = USB_SetBodyTemperatureCompensation(lUserID);
                break;
            case JavaInterface.USB_GET_JPEGPIC_WITH_APPENDDATA: //获取热图
                bRet = USB_GetJpegpicWithAppendData(lUserID);
                break;
            case JavaInterface.USB_GET_ROI_MAX_TEMPERATURE_SEARCH: //ROI最高温信息查询
                bRet = USB_GetROITemperatureSearch(lUserID);
                break;
            case JavaInterface.USB_GET_P2P_PARAM: //获取全屏测温参数
                bRet = USB_GetP2pParam(lUserID);
                break;
            case JavaInterface.USB_SET_P2P_PARAM: //设置全屏测温参数
                bRet = USB_SetP2pParam(lUserID);
                break;
            case JavaInterface.USB_GET_THERMOMETRY_CALIBRATION_FILE: //测温标定文件导出
                bRet = USB_GetThermometryCalibrationFile(lUserID);
                break;
            case JavaInterface.USB_SET_THERMOMETRY_CALIBRATION_FILE: //测温标定文件导入
                bRet = USB_SetThermometryCalibrationFile(lUserID);
                break;
            case JavaInterface.USB_GET_THERMOMETRY_EXPERT_REGIONS: //获取专家测温规则
                bRet = USB_GetThermometryExpertRegions(lUserID);
                break;
            case JavaInterface.USB_SET_THERMOMETRY_EXPERT_REGIONS: //设置专家测温规则
                bRet = USB_SetThermometryExpertRegions(lUserID);
                break;
            case JavaInterface.USB_GET_EXPERT_CORRECTION_PARAM: //获取专家测温校正参数
                bRet = USB_GetExpertCorrectionParam(lUserID);
                break;
            case JavaInterface.USB_SET_EXPERT_CORRECTION_PARAM: //设置专家测温校正参数
                bRet = USB_SetExpertCorrectionParam(lUserID);
                break;
            case JavaInterface.USB_START_EXPERT_CORRECTION: //开始专家测温校正
                bRet = USB_StartExpertCorrection(lUserID);
                break;
            case JavaInterface.USB_GET_AUDIO_IN_STATUS:
                bRet = USB_GetAudioInStatus(lUserID);
                break;
            case JavaInterface.USB_SET_IMAGE_WDR:
                bRet = USB_SetImageWDR(lUserID);
                break;
            case JavaInterface.USB_GET_THERMOMETRY_RISE_SETTINGS: //获取温升配置参数
                bRet = USB_GetRiseSettings(lUserID);
                break;
            case JavaInterface.USB_SET_THERMOMETRY_RISE_SETTINGS: //设置温升配置参数
                bRet = USB_SetRiseSettings(lUserID);
                break;
            case JavaInterface.USB_GET_ENVIROTEMPERATURE_CORRECT:  // 获取环境温度校正参数
                bRet = USB_GetEnvirotemperatureCorrect(lUserID);
                break;
            case JavaInterface.USB_SET_ENVIROTEMPERATURE_CORRECT:  // 设置环境温度校正参数
                bRet = USB_SetEnvirotemperatureCorrect(lUserID);
                break;
            default:
                Log.i("[USBDemo]", "Config No support! ");
                break;
        }
        return bRet;
    }



    //设置视频参数
    private boolean SetVideoParam(int lUserID)
    {
        USB_VIDEO_PARAM struVideoParam = new USB_VIDEO_PARAM();
        struVideoParam.dwVideoFormat = JavaInterface.USB_STREAM_MJPEG; //Mjpeg码流
        struVideoParam.dwWidth = 240; //宽
        struVideoParam.dwHeight = 320; //高
        struVideoParam.dwFramerate = 30; //帧率
        struVideoParam.dwBitrate = 0; //用不到
        struVideoParam.dwParamType = 0; //用不到
        struVideoParam.dwValue = 0; //用不到

        if (JavaInterface.getInstance().USB_SetVideoParam(lUserID, struVideoParam))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_SetVideoParam Success! " +
                    " dwVideoFormat:" + struVideoParam.dwVideoFormat +
                    " dwWidth:" + struVideoParam.dwWidth +
                    " dwHeight:" + struVideoParam.dwHeight +
                    " dwFramerate:" + struVideoParam.dwFramerate +
                    " dwBitrate:" + struVideoParam.dwBitrate +
                    " dwParamType:" + struVideoParam.dwParamType +
                    " dwValue:" + struVideoParam.dwValue);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_SetVideoParam failed! error:" + JavaInterface.getInstance().USB_GetLastError()+
                    " dwVideoFormat:" + struVideoParam.dwVideoFormat +
                    " dwWidth:" + struVideoParam.dwWidth +
                    " dwHeight:" + struVideoParam.dwHeight +
                    " dwFramerate:" + struVideoParam.dwFramerate +
                    " dwBitrate:" + struVideoParam.dwBitrate +
                    " dwParamType:" + struVideoParam.dwParamType +
                    " dwValue:" + struVideoParam.dwValue);
            return false;
        }
    }

    //获取设备信息
    private boolean USB_GetSysTemDeviceInfo(int lUserID)
    {
        USB_SYSTEM_DEVICE_INFO struSysDevInfo = new USB_SYSTEM_DEVICE_INFO();
        if (JavaInterface.getInstance().USB_GetSysTemDeviceInfo(lUserID, struSysDevInfo))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_GetSysTemDeviceInfo Success! " +
                    " byFirmwareVersion:" + struSysDevInfo.byFirmwareVersion +
                    " byEncoderVersion:" + struSysDevInfo.byEncoderVersion +
                    " byHardwareVersion:" + struSysDevInfo.byHardwareVersion +
                    " byDeviceType:" + struSysDevInfo.byDeviceType +
                    " byProtocolVersion:" + struSysDevInfo.byProtocolVersion +
                    " bySerialNumber:" + struSysDevInfo.bySerialNumber);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_GetSysTemDeviceInfo failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byFirmwareVersion:" + struSysDevInfo.byFirmwareVersion +
                    " byEncoderVersion:" + struSysDevInfo.byEncoderVersion +
                    " byHardwareVersion:" + struSysDevInfo.byHardwareVersion +
                    " byDeviceType:" + struSysDevInfo.byDeviceType +
                    " byProtocolVersion:" + struSysDevInfo.byProtocolVersion +
                    " bySerialNumber:" + struSysDevInfo.bySerialNumber);
            return false;
        }
    }

    //设备重启
    public boolean USB_SetSystemReboot(int lUserID)
    {
        if (JavaInterface.getInstance().USB_SetSystemReboot(lUserID))
        {
            Log.i("[USBDemo]","USB_SetSystemReboot Success!");
            return true;
        }
        else
        {
            Log.e("[USBDemo]","USB_SetSystemReboot failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            return false;
        }
    }

    //恢复默认
    public boolean USB_SetSystemReset(int lUserID)
    {
        if (JavaInterface.getInstance().USB_SetSystemReset(lUserID))
        {
            Log.i("[USBDemo]","USB_SetSystemReset Success!");
            return true;
        }
        else
        {
            Log.e("[USBDemo]","USB_SetSystemReset failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            return false;
        }
    }

    //获取硬件服务参数
    private boolean USB_GetSystemHardwareServer(int lUserID)
    {
        USB_SYSTEM_HARDWARE_SERVER struHardwareServer = new USB_SYSTEM_HARDWARE_SERVER();
        if (JavaInterface.getInstance().USB_GetSystemHardwareServer(lUserID, struHardwareServer))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_GetSystemHardwareServer Success! " +
                    " byUsbMode:" + struHardwareServer.byUsbMode);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_GetSystemHardwareServer failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byUsbMode:" + struHardwareServer.byUsbMode);
            return false;
        }
    }

    //设置硬件服务参数
    private boolean USB_SetSystemHardwareServer(int lUserID)
    {
        USB_SYSTEM_HARDWARE_SERVER struHardwareServer = new USB_SYSTEM_HARDWARE_SERVER();
        struHardwareServer.byUsbMode = 1; //USB模式切换	1-USB的UVC模式,	2-USB的NCM模式
        if (JavaInterface.getInstance().USB_SetSystemHardwareServer(lUserID, struHardwareServer))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_SetSystemHardwareServer Success! " +
                    " byUsbMode:" + struHardwareServer.byUsbMode);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_SetSystemHardwareServer failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byUsbMode:" + struHardwareServer.byUsbMode);
            return false;
        }
    }

    //获取系统本地时间
    private boolean USB_GetSystemLocalTime(int lUserID)
    {
        USB_SYSTEM_LOCALTIME struLocalTime = new USB_SYSTEM_LOCALTIME();
        if (JavaInterface.getInstance().USB_GetSystemLocalTime(lUserID, struLocalTime))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_GetSystemLocalTime Success! " +
                    " wYear:" + struLocalTime.wYear +
                    " byMonth:" + struLocalTime.byMonth +
                    " byDay:" + struLocalTime.byDay +
                    " byHour:" + struLocalTime.byHour +
                    " byMinute:" + struLocalTime.byMinute +
                    " bySecond:" + struLocalTime.bySecond +
                    " wMillisecond:" + struLocalTime.wMillisecond +
                    " byExternalTimeSourceEnabled:" + struLocalTime.byExternalTimeSourceEnabled);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_GetSystemLocalTime failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " wYear:" + struLocalTime.wYear +
                    " byMonth:" + struLocalTime.byMonth +
                    " byDay:" + struLocalTime.byDay +
                    " byHour:" + struLocalTime.byHour +
                    " byMinute:" + struLocalTime.byMinute +
                    " bySecond:" + struLocalTime.bySecond +
                    " wMillisecond:" + struLocalTime.wMillisecond +
                    " byExternalTimeSourceEnabled:" + struLocalTime.byExternalTimeSourceEnabled);
            return false;
        }
    }

    //设置系统本地时间
    private boolean USB_SetSystemLocalTime(int lUserID)
    {
        USB_SYSTEM_LOCALTIME struLocalTime = new USB_SYSTEM_LOCALTIME();
        struLocalTime.wMillisecond = 100;
        struLocalTime.bySecond = 30;
        struLocalTime.byMinute = 40;
        struLocalTime.byHour = 20;
        struLocalTime.byDay = 22;
        struLocalTime.byMonth = 12;
        struLocalTime.wYear = 2020;
        struLocalTime.byExternalTimeSourceEnabled = 0;

        if (JavaInterface.getInstance().USB_SetSystemLocalTime(lUserID, struLocalTime))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_SetSystemLocalTime Success! " +
                    " wYear:" + struLocalTime.wYear +
                    " byMonth:" + struLocalTime.byMonth +
                    " byDay:" + struLocalTime.byDay +
                    " byHour:" + struLocalTime.byHour +
                    " byMinute:" + struLocalTime.byMinute +
                    " bySecond:" + struLocalTime.bySecond +
                    " wMillisecond:" + struLocalTime.wMillisecond +
                    " byExternalTimeSourceEnabled:" + struLocalTime.byExternalTimeSourceEnabled);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_SetSystemLocalTime failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " wYear:" + struLocalTime.wYear +
                    " byMonth:" + struLocalTime.byMonth +
                    " byDay:" + struLocalTime.byDay +
                    " byHour:" + struLocalTime.byHour +
                    " byMinute:" + struLocalTime.byMinute +
                    " bySecond:" + struLocalTime.bySecond +
                    " wMillisecond:" + struLocalTime.wMillisecond +
                    " byExternalTimeSourceEnabled:" + struLocalTime.byExternalTimeSourceEnabled);
            return false;
        }
    }

    //获取图像亮度参数
    private boolean USB_GetImageBrightNess(int lUserID)
    {
        USB_IMAGE_BRIGHTNESS struImageBrightNess = new USB_IMAGE_BRIGHTNESS();
        if (JavaInterface.getInstance().USB_GetImageBrightNess(lUserID, struImageBrightNess))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_GetImageBrightNess Success! " +
                    " dwBrightness:" + struImageBrightNess.dwBrightness);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_GetImageBrightNess failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " dwBrightness:" + struImageBrightNess.dwBrightness);
            return false;
        }
    }

    //设置图像亮度参数
    private boolean USB_SetImageBrightNess(int lUserID)
    {
        USB_IMAGE_BRIGHTNESS struImageBrightNess = new USB_IMAGE_BRIGHTNESS();
        struImageBrightNess.dwBrightness = 49;

        if (JavaInterface.getInstance().USB_SetImageBrightNess(lUserID, struImageBrightNess))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_SetImageBrightNess Success! " +
                    " dwBrightness:" + struImageBrightNess.dwBrightness);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_SetImageBrightNess failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " dwBrightness:" + struImageBrightNess.dwBrightness);
            return false;
        }
    }

    //获取图像对比度参数
    private boolean USB_GetImageContrast(int lUserID)
    {
        USB_IMAGE_CONTRAST struImageContrast = new USB_IMAGE_CONTRAST();
        if (JavaInterface.getInstance().USB_GetImageContrast(lUserID, struImageContrast))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_GetImageContrast Success! " +
                    " dwContrast:" + struImageContrast.dwContrast);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_GetImageContrast failed! error:" + JavaInterface.getInstance().USB_GetLastError()+
                    " dwContrast:" + struImageContrast.dwContrast);
            return false;
        }
    }

    //设置图像对比度参数
    private boolean USB_SetImageContrast(int lUserID)
    {
        USB_IMAGE_CONTRAST struImageContrast = new USB_IMAGE_CONTRAST();
        struImageContrast.dwContrast = 49;

        if (JavaInterface.getInstance().USB_SetImageContrast(lUserID, struImageContrast))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_SetImageContrast Success! " +
                    " dwContrast:" + struImageContrast.dwContrast);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_SetImageContrast failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " dwContrast:" + struImageContrast.dwContrast);
            return false;
        }
    }

    //一键背景校正
    private boolean USB_SetImageBackGroundCorrect(int lUserID)
    {
        if (JavaInterface.getInstance().USB_SetImageBackGroundCorrect(lUserID))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_SetImageBackGroundCorrect Success! ");
            return true;
        } else {
            Log.e("[USBDemo]", "USB_SetImageBackGroundCorrect failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            return false;
        }
    }

    //诊断信息导出
    private boolean USB_GetSystemDiagnosedData(int lUserID)
    {
        USB_SYSTEM_DIAGNOSED_DATA struSysDiagnosedData = new USB_SYSTEM_DIAGNOSED_DATA();
        if (JavaInterface.getInstance().USB_GetSystemDiagnosedData(lUserID, struSysDiagnosedData))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_GetSystemDiagnosedData Success! " +
                    " dwDataLenth:" + struSysDiagnosedData.dwDataLenth);

            //诊断信息写文件
            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss");
            String date = sDateFormat.format(new java.util.Date());
            FileOutputStream file = null;
            try {
                file = new FileOutputStream("/mnt/sdcard/sdklog/" + date + "_diagnosed_data");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                file.write(struSysDiagnosedData.pDiagnosedData, 0, struSysDiagnosedData.dwDataLenth);
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        } else {
            Log.e("[USBDemo]", "USB_GetSystemDiagnosedData failed! error:" + JavaInterface.getInstance().USB_GetLastError()+
                    " dwDataLenth:" + struSysDiagnosedData.dwDataLenth);
            return false;
        }
    }

    //带参数条件诊断信息导出
    private boolean USB_GetSystemDiagnosedDataEx(int lUserID)
    {
        USB_SYSTEM_DIAGNOSED_DATA_COND struSysDiagnosedDataCond = new USB_SYSTEM_DIAGNOSED_DATA_COND();
        USB_SYSTEM_DIAGNOSED_DATA struSysDiagnosedData = new USB_SYSTEM_DIAGNOSED_DATA();
        if (JavaInterface.getInstance().USB_GetSystemDiagnosedDataEx(lUserID, struSysDiagnosedDataCond, struSysDiagnosedData))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_GetSystemDiagnosedDataEx Success! " +
                    " dwDataLenth:" + struSysDiagnosedData.dwDataLenth);

            //诊断信息写文件
            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss");
            String date = sDateFormat.format(new java.util.Date());
            FileOutputStream file = null;
            try {
                file = new FileOutputStream("/mnt/sdcard/sdklog/" + date + "_diagnosed_data");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                file.write(struSysDiagnosedData.pDiagnosedData, 0, struSysDiagnosedData.dwDataLenth);
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        }
        else
        {
            Log.e("[USBDemo]", "USB_GetSystemDiagnosedDataEx failed! error:" + JavaInterface.getInstance().USB_GetLastError()+
                    " dwDataLenth:" + struSysDiagnosedData.dwDataLenth);
            return false;
        }
    }

    //一键手动校正
    private boolean USB_SetImageManualCorrect(int lUserID)
    {
        if (JavaInterface.getInstance().USB_SetImageManualCorrect(lUserID))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_SetImageManualCorrect Success! ");
            return true;
        } else {
            Log.e("[USBDemo]", "USB_SetImageManualCorrect failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            return false;
        }
    }

    //获取图像增强参数
    private boolean USB_GetImageEnhancement(int lUserID)
    {
        USB_IMAGE_ENHANCEMENT struImageEnhancement = new USB_IMAGE_ENHANCEMENT();
        if (JavaInterface.getInstance().USB_GetImageEnhancement(lUserID, struImageEnhancement))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_GetImageEnhancement Success! " +
                    " byNoiseReduceMode:" + struImageEnhancement.byNoiseReduceMode +
                    " dwGeneralLevel:" + struImageEnhancement.dwGeneralLevel +
                    " dwFrameNoiseReduceLevel:" + struImageEnhancement.dwFrameNoiseReduceLevel +
                    " dwInterFrameNoiseReduceLevel:" + struImageEnhancement.dwInterFrameNoiseReduceLevel +
                    " byPaletteMode:" + struImageEnhancement.byPaletteMode +
                    " byLSEDetailEnabled:" + struImageEnhancement.byLSEDetailEnabled +
                    " dwLSEDetailLevel:" + struImageEnhancement.dwLSEDetailLevel);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_GetImageEnhancement failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byNoiseReduceMode:" + struImageEnhancement.byNoiseReduceMode +
                    " dwGeneralLevel:" + struImageEnhancement.dwGeneralLevel +
                    " dwFrameNoiseReduceLevel:" + struImageEnhancement.dwFrameNoiseReduceLevel +
                    " dwInterFrameNoiseReduceLevel:" + struImageEnhancement.dwInterFrameNoiseReduceLevel +
                    " byPaletteMode:" + struImageEnhancement.byPaletteMode +
                    " byLSEDetailEnabled:" + struImageEnhancement.byLSEDetailEnabled +
                    " dwLSEDetailLevel:" + struImageEnhancement.dwLSEDetailLevel);
            return false;
        }
    }

    //设置图像增强参数
    private boolean USB_SetImageEnhancement(int lUserID)
    {
        USB_IMAGE_ENHANCEMENT struImageEnhancement = new USB_IMAGE_ENHANCEMENT();
        struImageEnhancement.byNoiseReduceMode = 2; //数字降噪模式：0-关闭; 1-普通模式;  2 - 专家模式
        struImageEnhancement.dwGeneralLevel = 51; //普通模式降噪级别 0-100
        struImageEnhancement.dwFrameNoiseReduceLevel = 51; //专家模式空域降噪级别 0-100
        struImageEnhancement.dwInterFrameNoiseReduceLevel = 51; //专家模式时域降噪级别 0-100
        struImageEnhancement.byPaletteMode = 11; //伪彩色颜色模式：1-白热;  2-黑热;  10-融合1; 11-彩虹; 12-融合2; 13-铁红1; 14-铁红2; 15-深褐色; 16-色彩1; 17-色彩2; 18-冰火; 19-雨; 20-红热; 21-绿热; 22-深蓝
        struImageEnhancement.byLSEDetailEnabled = 1; //图像细节增强使能: 0-关闭 1-开启
        struImageEnhancement.dwLSEDetailLevel = 51; //图像细节增强等级: 0-100
        struImageEnhancement.dwWideTemperatureUpThreshold = 1500;
        struImageEnhancement.dwWideTemperatureDownThreshold = 1200;

        if (JavaInterface.getInstance().USB_SetImageEnhancement(lUserID, struImageEnhancement))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_SetImageEnhancement Success! " +
                    " byNoiseReduceMode:" + struImageEnhancement.byNoiseReduceMode +
                    " dwGeneralLevel:" + struImageEnhancement.dwGeneralLevel +
                    " dwFrameNoiseReduceLevel:" + struImageEnhancement.dwFrameNoiseReduceLevel +
                    " dwInterFrameNoiseReduceLevel:" + struImageEnhancement.dwInterFrameNoiseReduceLevel +
                    " byPaletteMode:" + struImageEnhancement.byPaletteMode +
                    " byLSEDetailEnabled:" + struImageEnhancement.byLSEDetailEnabled +
                    " dwLSEDetailLevel:" + struImageEnhancement.dwLSEDetailLevel);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_SetImageEnhancement failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byNoiseReduceMode:" + struImageEnhancement.byNoiseReduceMode +
                    " dwGeneralLevel:" + struImageEnhancement.dwGeneralLevel +
                    " dwFrameNoiseReduceLevel:" + struImageEnhancement.dwFrameNoiseReduceLevel +
                    " dwInterFrameNoiseReduceLevel:" + struImageEnhancement.dwInterFrameNoiseReduceLevel +
                    " byPaletteMode:" + struImageEnhancement.byPaletteMode +
                    " byLSEDetailEnabled:" + struImageEnhancement.byLSEDetailEnabled +
                    " dwLSEDetailLevel:" + struImageEnhancement.dwLSEDetailLevel);
            return false;
        }
    }

    //获取视频调整参数
    private boolean USB_GetImageVideoAdjust(int lUserID)
    {
        USB_IMAGE_VIDEO_ADJUST struImageVideoAdjust = new USB_IMAGE_VIDEO_ADJUST();
        if (JavaInterface.getInstance().USB_GetImageVideoAdjust(lUserID, struImageVideoAdjust))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_GetImageVideoAdjust Success! " +
                    " byImageFlipStyle:" + struImageVideoAdjust.byImageFlipStyle +
                    " byPowerLineFrequencyMode:" + struImageVideoAdjust.byPowerLineFrequencyMode +
                    " byCorridor:" + struImageVideoAdjust.byCorridor);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_GetImageVideoAdjust failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byImageFlipStyle:" + struImageVideoAdjust.byImageFlipStyle +
                    " byPowerLineFrequencyMode:" + struImageVideoAdjust.byPowerLineFrequencyMode +
                    " byCorridor:" + struImageVideoAdjust.byCorridor);
            return false;
        }
    }

    //设置视频调整参数
    private boolean USB_SetImageVideoAdjust(int lUserID)
    {
        USB_IMAGE_VIDEO_ADJUST struImageVideoAdjust1 = new USB_IMAGE_VIDEO_ADJUST();
        JavaInterface.getInstance().USB_GetImageVideoAdjust(lUserID, struImageVideoAdjust1);

        USB_IMAGE_VIDEO_ADJUST struImageVideoAdjust = new USB_IMAGE_VIDEO_ADJUST();
        struImageVideoAdjust.byImageFlipStyle = 0; //镜像模式:	0-关闭	1-中心	2-左右	3-上下
        struImageVideoAdjust.byPowerLineFrequencyMode = 1;	//视频制式：1-PAL(50HZ)
        //镜头走廊模式(旋转):	0-关闭	1-开启

        if(struImageVideoAdjust1.byCorridor==1) {
            struImageVideoAdjust.byCorridor = 0;
            BasicConfig.yuvImgHeight=192;
            BasicConfig.yuvImgWidth=256;
        }else{
            struImageVideoAdjust.byCorridor = 1;
            BasicConfig.yuvImgHeight=256;
            BasicConfig.yuvImgWidth=192;
        }

        if (JavaInterface.getInstance().USB_SetImageVideoAdjust(lUserID, struImageVideoAdjust))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_SetImageVideoAdjust Success! " +
                    " byImageFlipStyle:" + struImageVideoAdjust.byImageFlipStyle +
                    " byPowerLineFrequencyMode:" + struImageVideoAdjust.byPowerLineFrequencyMode +
                    " byCorridor:" + struImageVideoAdjust.byCorridor);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_SetImageVideoAdjust failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byImageFlipStyle:" + struImageVideoAdjust.byImageFlipStyle +
                    " byPowerLineFrequencyMode:" + struImageVideoAdjust.byPowerLineFrequencyMode +
                    " byCorridor:" + struImageVideoAdjust.byCorridor);
            return false;
        }
    }

    //获取测温基本参数
    private boolean USB_GetThermometryBasicParam(int lUserID)
    {
        USB_THERMOMETRY_BASIC_PARAM struThermometryBasic = new USB_THERMOMETRY_BASIC_PARAM();
        if (JavaInterface.getInstance().USB_GetThermometryBasicParam(lUserID, struThermometryBasic))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_GetThermometryBasicParam Success! " +
                    " byEnabled:" + struThermometryBasic.byEnabled +
                    " byDisplayMaxTemperatureEnabled:" + struThermometryBasic.byDisplayMaxTemperatureEnabled +
                    " byDisplayMinTemperatureEnabled:" + struThermometryBasic.byDisplayMinTemperatureEnabled +
                    " byDisplayAverageTemperatureEnabled:" + struThermometryBasic.byDisplayAverageTemperatureEnabled +
                    " byTemperatureUnit:" + struThermometryBasic.byTemperatureUnit +
                    " byTemperatureRange:" + struThermometryBasic.byTemperatureRange +
                    " byCalibrationCoefficientEnabled:" + struThermometryBasic.byCalibrationCoefficientEnabled +
                    " dwCalibrationCoefficient:" + struThermometryBasic.dwCalibrationCoefficient +
                    " dwExternalOpticsWindowCorrection:" + struThermometryBasic.dwExternalOpticsWindowCorrection +
                    " dwEmissivity:" + struThermometryBasic.dwEmissivity +
                    " byDistanceUnit:" + struThermometryBasic.byDistanceUnit +
                    " dwDistance:" + struThermometryBasic.dwDistance +
                    " byReflectiveEnable:" + struThermometryBasic.byReflectiveEnable +
                    " dwReflectiveTemperature:" + struThermometryBasic.dwReflectiveTemperature +
                    " byThermomrtryInfoDisplayPosition:" + struThermometryBasic.byThermomrtryInfoDisplayPosition +
                    " byThermometryStreamOverlay:" + struThermometryBasic.byThermometryStreamOverlay +
                    " dwAlert:" + struThermometryBasic.dwAlert +
                    " dwAlarm:" + struThermometryBasic.dwAlarm +
                    " dwExternalOpticsTransmit:" + struThermometryBasic.dwExternalOpticsTransmit);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_GetThermometryBasicParam failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byEnabled:" + struThermometryBasic.byEnabled +
                    " byDisplayMaxTemperatureEnabled:" + struThermometryBasic.byDisplayMaxTemperatureEnabled +
                    " byDisplayMinTemperatureEnabled:" + struThermometryBasic.byDisplayMinTemperatureEnabled +
                    " byDisplayAverageTemperatureEnabled:" + struThermometryBasic.byDisplayAverageTemperatureEnabled +
                    " byTemperatureUnit:" + struThermometryBasic.byTemperatureUnit +
                    " byTemperatureRange:" + struThermometryBasic.byTemperatureRange +
                    " byCalibrationCoefficientEnabled:" + struThermometryBasic.byCalibrationCoefficientEnabled +
                    " dwCalibrationCoefficient:" + struThermometryBasic.dwCalibrationCoefficient +
                    " dwExternalOpticsWindowCorrection:" + struThermometryBasic.dwExternalOpticsWindowCorrection +
                    " dwEmissivity:" + struThermometryBasic.dwEmissivity +
                    " byDistanceUnit:" + struThermometryBasic.byDistanceUnit +
                    " dwDistance:" + struThermometryBasic.dwDistance +
                    " byReflectiveEnable:" + struThermometryBasic.byReflectiveEnable +
                    " dwReflectiveTemperature:" + struThermometryBasic.dwReflectiveTemperature +
                    " byThermomrtryInfoDisplayPosition:" + struThermometryBasic.byThermomrtryInfoDisplayPosition +
                    " byThermometryStreamOverlay:" + struThermometryBasic.byThermometryStreamOverlay +
                    " dwAlert:" + struThermometryBasic.dwAlert +
                    " dwAlarm:" + struThermometryBasic.dwAlarm +
                    " dwExternalOpticsTransmit:" + struThermometryBasic.dwExternalOpticsTransmit);
            return false;
        }
    }

    //设置测温基本参数
    private boolean USB_SetThermometryBasicParam(int lUserID)
    {
        USB_THERMOMETRY_BASIC_PARAM struThermometryBasic = new USB_THERMOMETRY_BASIC_PARAM();
        struThermometryBasic.byEnabled = 1;//开启测温功能使能
        struThermometryBasic.byDisplayMaxTemperatureEnabled = 0;//显示最高温: 0-关闭; 1-开启
        struThermometryBasic.byDisplayMinTemperatureEnabled = 0;//显示最低温: 0-关闭; 1-开启
        struThermometryBasic.byDisplayAverageTemperatureEnabled = 0;//显示平均温: 0-关闭; 1-开启
        struThermometryBasic.byTemperatureUnit = 1;//温度单位: 1 - 摄氏温度;2 - 华氏温度;3 - 开尔文温度(协议传输中约定以摄氏温度作为单位传输)
        struThermometryBasic.byTemperatureRange = 2;//!!!测温范围: 1-30~45  2- -20~150     3- 0~400
        struThermometryBasic.byCalibrationCoefficientEnabled = 0;//启用标定系数:0 - 关闭;1 - 开启
        struThermometryBasic.dwCalibrationCoefficient = 2000;//标定系数: 0.00~30.00 ,传输时实际值 * 100换算成整数
        struThermometryBasic.dwExternalOpticsWindowCorrection = 1200;//外部光学温度: -40.0~80.0℃ ,传输时(实际值 + 100) * 10换算成正整数
        struThermometryBasic.dwEmissivity = 98;//发射率: 0.01~1(精确到小数点后两位), 传输时实际值 * 100换算成整数
        struThermometryBasic.byDistanceUnit = 1;//距离单位: 1 - 米; 2 - 厘米; 3 - 英尺
        struThermometryBasic.dwDistance = 30;//距离: 0.3-2m（协议传输中约定以cm作为单位传输, 精确到小数点后1位）
        struThermometryBasic.byReflectiveEnable = 0;//反射温度使能: 0 - 关闭; 1 - 开启
        struThermometryBasic.dwReflectiveTemperature = 1100;//反射温度: -100.0~1000.0℃（精确到小数点后1位）,传输时(实际值 + 100) * 10换算成正整数
        struThermometryBasic.byThermomrtryInfoDisplayPosition = 2;//测温信息显示位置: 1-跟随规则 2-屏幕左上角
        struThermometryBasic.byThermometryStreamOverlay = 1; //码流叠加温度信息: 1-不叠加  2-叠加
        struThermometryBasic.dwAlert = 1500; //预警温度: -20℃~400℃, 传输时(实际值+100)*10换算成正整数
        struThermometryBasic.dwAlarm = 1700; //报警温度: -20℃~400℃, 传输时(实际值+100)*10换算成正整数
        struThermometryBasic.dwExternalOpticsTransmit = 50; //外部光学透过率: 0.01~1(精确到小数点后两位), 传输时实际值*100换算成整数

        if (JavaInterface.getInstance().USB_SetThermometryBasicParam(lUserID, struThermometryBasic))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_SetThermometryBasicParam Success! " +
                    " byEnabled:" + struThermometryBasic.byEnabled +
                    " byDisplayMaxTemperatureEnabled:" + struThermometryBasic.byDisplayMaxTemperatureEnabled +
                    " byDisplayMinTemperatureEnabled:" + struThermometryBasic.byDisplayMinTemperatureEnabled +
                    " byDisplayAverageTemperatureEnabled:" + struThermometryBasic.byDisplayAverageTemperatureEnabled +
                    " byTemperatureUnit:" + struThermometryBasic.byTemperatureUnit +
                    " byTemperatureRange:" + struThermometryBasic.byTemperatureRange +
                    " byCalibrationCoefficientEnabled:" + struThermometryBasic.byCalibrationCoefficientEnabled +
                    " dwCalibrationCoefficient:" + struThermometryBasic.dwCalibrationCoefficient +
                    " dwExternalOpticsWindowCorrection:" + struThermometryBasic.dwExternalOpticsWindowCorrection +
                    " dwEmissivity:" + struThermometryBasic.dwEmissivity +
                    " byDistanceUnit:" + struThermometryBasic.byDistanceUnit +
                    " dwDistance:" + struThermometryBasic.dwDistance +
                    " byReflectiveEnable:" + struThermometryBasic.byReflectiveEnable +
                    " dwReflectiveTemperature:" + struThermometryBasic.dwReflectiveTemperature +
                    " byThermomrtryInfoDisplayPosition:" + struThermometryBasic.byThermomrtryInfoDisplayPosition +
                    " byThermometryStreamOverlay:" + struThermometryBasic.byThermometryStreamOverlay +
                    " dwAlert:" + struThermometryBasic.dwAlert +
                    " dwAlarm:" + struThermometryBasic.dwAlarm +
                    " dwExternalOpticsTransmit:" + struThermometryBasic.dwExternalOpticsTransmit);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_SetThermometryBasicParam failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byEnabled:" + struThermometryBasic.byEnabled +
                    " byDisplayMaxTemperatureEnabled:" + struThermometryBasic.byDisplayMaxTemperatureEnabled +
                    " byDisplayMinTemperatureEnabled:" + struThermometryBasic.byDisplayMinTemperatureEnabled +
                    " byDisplayAverageTemperatureEnabled:" + struThermometryBasic.byDisplayAverageTemperatureEnabled +
                    " byTemperatureUnit:" + struThermometryBasic.byTemperatureUnit +
                    " byTemperatureRange:" + struThermometryBasic.byTemperatureRange +
                    " byCalibrationCoefficientEnabled:" + struThermometryBasic.byCalibrationCoefficientEnabled +
                    " dwCalibrationCoefficient:" + struThermometryBasic.dwCalibrationCoefficient +
                    " dwExternalOpticsWindowCorrection:" + struThermometryBasic.dwExternalOpticsWindowCorrection +
                    " dwEmissivity:" + struThermometryBasic.dwEmissivity +
                    " byDistanceUnit:" + struThermometryBasic.byDistanceUnit +
                    " dwDistance:" + struThermometryBasic.dwDistance +
                    " byReflectiveEnable:" + struThermometryBasic.byReflectiveEnable +
                    " dwReflectiveTemperature:" + struThermometryBasic.dwReflectiveTemperature +
                    " byThermomrtryInfoDisplayPosition:" + struThermometryBasic.byThermomrtryInfoDisplayPosition +
                    " byThermometryStreamOverlay:" + struThermometryBasic.byThermometryStreamOverlay +
                    " dwAlert:" + struThermometryBasic.dwAlert +
                    " dwAlarm:" + struThermometryBasic.dwAlarm +
                    " dwExternalOpticsTransmit:" + struThermometryBasic.dwExternalOpticsTransmit);
            return false;
        }
    }

    //获取测温模式
    private boolean USB_GetThermometryMode(int lUserID)
    {
        USB_THERMOMETRY_MODE struThermometryMode = new USB_THERMOMETRY_MODE();
        if (JavaInterface.getInstance().USB_GetThermometryMode(lUserID, struThermometryMode))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_GetThermometryBasicParam Success! " +
                    " byThermometryMode:" + struThermometryMode.byThermometryMode +
                    " byThermometryROIEnabled:" + struThermometryMode.byThermometryROIEnabled);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_GetThermometryBasicParam failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byThermometryMode:" + struThermometryMode.byThermometryMode +
                    " byThermometryROIEnabled:" + struThermometryMode.byThermometryROIEnabled);
            return false;
        }
    }

    //设置测温模式
    private boolean USB_SetThermometryMode(int lUserID)
    {
        USB_THERMOMETRY_MODE struThermometryMode = new USB_THERMOMETRY_MODE();
        struThermometryMode.byThermometryMode = 2;//测温模式: 1-普通; 2-专家
        struThermometryMode.byThermometryROIEnabled = 0;//测温ROI使能: 0 - 关闭; 1 - 开启

        if (JavaInterface.getInstance().USB_SetThermometryMode(lUserID, struThermometryMode))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_SetThermometryBasicParam Success! " +
                    " byThermometryMode:" + struThermometryMode.byThermometryMode +
                    " byThermometryROIEnabled:" + struThermometryMode.byThermometryROIEnabled);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_SetThermometryBasicParam failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byThermometryMode:" + struThermometryMode.byThermometryMode +
                    " byThermometryROIEnabled:" + struThermometryMode.byThermometryROIEnabled);
            return false;
        }
    }

    //获取测温规则参数
    private boolean USB_GetThermometryRegions(int lUserID)
    {
        USB_THERMOMETRY_REGIONS struThermometryRegions = new USB_THERMOMETRY_REGIONS();
        if (JavaInterface.getInstance().USB_GetThermometryRegions(lUserID, struThermometryRegions))
        {
            //配置成功
            Log.i("[USBDemo]","USB_GetThermometryRegions Success!" +
                    " byRegionNum:" + struThermometryRegions.byRegionNum);
            for (int i = 0; i < struThermometryRegions.byRegionNum; i++)
            {
                Log.i("[USBDemo]","" + i +
                        " byRegionID:" + struThermometryRegions.struRegion[i].byRegionID +
                        " byRegionEnabled:" + struThermometryRegions.struRegion[i].byRegionEnabled +
                        " dwRegionX:" + struThermometryRegions.struRegion[i].dwRegionX +
                        " dwRegionY:" + struThermometryRegions.struRegion[i].dwRegionY +
                        " dwRegionWidth:" + struThermometryRegions.struRegion[i].dwRegionWidth +
                        " dwRegionHeight:" + struThermometryRegions.struRegion[i].dwRegionHeight);
            }
            return true;
        } else {
            Log.e("[USBDemo]", "USB_GetThermometryBasicParam failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byRegionNum:" + struThermometryRegions.byRegionNum);
            for (int i = 0; i < struThermometryRegions.byRegionNum; i++)
            {
                Log.e("[USBDemo]","" + i +
                        " byRegionID:" + struThermometryRegions.struRegion[i].byRegionID +
                        " byRegionEnabled:" + struThermometryRegions.struRegion[i].byRegionEnabled +
                        " dwRegionX:" + struThermometryRegions.struRegion[i].dwRegionX +
                        " dwRegionY:" + struThermometryRegions.struRegion[i].dwRegionY +
                        " dwRegionWidth:" + struThermometryRegions.struRegion[i].dwRegionWidth +
                        " dwRegionHeight:" + struThermometryRegions.struRegion[i].dwRegionHeight);
            }
            return false;
        }
    }

    //设置测温规则参数
    private boolean USB_SetThermometryRegions(int lUserID)
    {
        USB_THERMOMETRY_REGIONS struThermometryRegions = new USB_THERMOMETRY_REGIONS();
        struThermometryRegions.byRegionNum = 1;//规则区域总个数
        for (int i = 0; i < struThermometryRegions.byRegionNum; i++)
        {
            struThermometryRegions.struRegion[i].byRegionID = 1;  //区域ID，从1开始递增
            struThermometryRegions.struRegion[i].byRegionEnabled = 1;  //区域使能 0-关闭 1-开启
            struThermometryRegions.struRegion[i].dwRegionX = 10;  //区域左上顶点X坐标，归一化值，范围0-1000
            struThermometryRegions.struRegion[i].dwRegionY = 10;  //区域左上顶点Y坐标，归一化值，范围0-1000
            struThermometryRegions.struRegion[i].dwRegionWidth = 100;  //区域宽度，归一化值，范围0-1000
            struThermometryRegions.struRegion[i].dwRegionHeight = 100;  //区域高度，归一化值，范围0-1000
        }

        if (JavaInterface.getInstance().USB_SetThermometryRegions(lUserID, struThermometryRegions))
        {
            //配置成功
            Log.i("[USBDemo]","USB_SetThermometryRegions Success!" +
                    " byRegionNum:" + struThermometryRegions.byRegionNum);
            for (int i = 0; i < struThermometryRegions.byRegionNum; i++)
            {
                Log.i("[USBDemo]","" + i +
                        " byRegionID:" + struThermometryRegions.struRegion[i].byRegionID +
                        " byRegionEnabled:" + struThermometryRegions.struRegion[i].byRegionEnabled +
                        " dwRegionX:" + struThermometryRegions.struRegion[i].dwRegionX +
                        " dwRegionY:" + struThermometryRegions.struRegion[i].dwRegionY +
                        " dwRegionWidth:" + struThermometryRegions.struRegion[i].dwRegionWidth +
                        " dwRegionHeight:" + struThermometryRegions.struRegion[i].dwRegionHeight);
            }
            return true;
        } else {
            Log.e("[USBDemo]", "USB_SetThermometryRegions failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byRegionNum:" + struThermometryRegions.byRegionNum);
            for (int i = 0; i < struThermometryRegions.byRegionNum; i++)
            {
                Log.e("[USBDemo]","" + i +
                        " byRegionID:" + struThermometryRegions.struRegion[i].byRegionID +
                        " byRegionEnabled:" + struThermometryRegions.struRegion[i].byRegionEnabled +
                        " dwRegionX:" + struThermometryRegions.struRegion[i].dwRegionX +
                        " dwRegionY:" + struThermometryRegions.struRegion[i].dwRegionY +
                        " dwRegionWidth:" + struThermometryRegions.struRegion[i].dwRegionWidth +
                        " dwRegionHeight:" + struThermometryRegions.struRegion[i].dwRegionHeight);
            }
            return false;
        }
    }

    //获取热成像相关算法版本信息
    private boolean USB_GetThermalAlgVersion(int lUserID)
    {
        USB_THERMAL_ALG_VERSION struvThermalAlgVersion = new USB_THERMAL_ALG_VERSION();
        if (JavaInterface.getInstance().USB_GetThermalAlgVersion(lUserID, struvThermalAlgVersion))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_GetThermalAlgVersion Success! " +
                    " szThermometryAlgName:" + struvThermalAlgVersion.szThermometryAlgName);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_GetThermalAlgVersion failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " szThermometryAlgName:" + struvThermalAlgVersion.szThermometryAlgName);
            return false;
        }
    }

    //获取热成像码流参数
    private boolean USB_GetThermalStreamParam(int lUserID)
    {
        USB_THERMAL_STREAM_PARAM struThermalStreamParam = new USB_THERMAL_STREAM_PARAM();
        if (JavaInterface.getInstance().USB_GetThermalStreamParam(lUserID, struThermalStreamParam))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_GetThermalStreamParam Success! " +
                    " byVideoCodingType:" + struThermalStreamParam.byVideoCodingType);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_GetThermalStreamParam failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byVideoCodingType:" + struThermalStreamParam.byVideoCodingType);
            return false;
        }
    }

    //设置热成像码流参数
    private boolean USB_SetThermalStreamParam(int lUserID)
    {
        USB_THERMAL_STREAM_PARAM struThermalStreamParam = new USB_THERMAL_STREAM_PARAM();
        if(BasicConfig.videoCodingType==8) {
            BasicConfig.videoCodingType=2;
        } else if(BasicConfig.videoCodingType==2) {
            BasicConfig.videoCodingType=6;
        } else if(BasicConfig.videoCodingType==6) {
            BasicConfig.videoCodingType=8;
        }
        struThermalStreamParam.byVideoCodingType = (byte)BasicConfig.videoCodingType;


        if (JavaInterface.getInstance().USB_SetThermalStreamParam(lUserID, struThermalStreamParam))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_SetThermalStreamParam Success! " +
                    " byVideoCodingType:" + struThermalStreamParam.byVideoCodingType);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_SetThermalStreamParam failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byVideoCodingType:" + struThermalStreamParam.byVideoCodingType);
            return false;
        }
    }

    //获取测温修正参数
    private boolean USB_GetTemperatureCorrect(int lUserID)
    {
        USB_TEMPERATURE_CORRECT struTemperatureCorrect = new USB_TEMPERATURE_CORRECT();
        if (JavaInterface.getInstance().USB_GetTemperatureCorrect(lUserID, struTemperatureCorrect))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_GetTemperatureCorrect Success! " +
                    " byEnabled:" + struTemperatureCorrect.byEnabled +
                    " byStreamOverlay:" + struTemperatureCorrect.byStreamOverlay +
                    " byCorrectEnabled:" + struTemperatureCorrect.byCorrectEnabled +
                    " dwEmissivity:" + struTemperatureCorrect.dwEmissivity +
                    " dwDistance:" + struTemperatureCorrect.dwDistance +
                    " dwTemperature:" + struTemperatureCorrect.dwTemperature +
                    " dwCentrePointX:" + struTemperatureCorrect.dwCentrePointX +
                    " dwCentrePointY:" + struTemperatureCorrect.dwCentrePointY +
                    " dwCorrectTemperature:" + struTemperatureCorrect.dwCorrectTemperature);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_GetTemperatureCorrect failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byEnabled:" + struTemperatureCorrect.byEnabled +
                    " byStreamOverlay:" + struTemperatureCorrect.byStreamOverlay +
                    " byCorrectEnabled:" + struTemperatureCorrect.byCorrectEnabled +
                    " dwEmissivity:" + struTemperatureCorrect.dwEmissivity +
                    " dwDistance:" + struTemperatureCorrect.dwDistance +
                    " dwTemperature:" + struTemperatureCorrect.dwTemperature +
                    " dwCentrePointX:" + struTemperatureCorrect.dwCentrePointX +
                    " dwCentrePointY:" + struTemperatureCorrect.dwCentrePointY +
                    " dwCorrectTemperature:" + struTemperatureCorrect.dwCorrectTemperature);
            return false;
        }
    }

    //设置测温修正参数
    private boolean USB_SetTemperatureCorrect(int lUserID)
    {
        USB_TEMPERATURE_CORRECT struTemperatureCorrect = new USB_TEMPERATURE_CORRECT();
        struTemperatureCorrect.byEnabled = 1;  //使能 0-关闭 1-开启
        struTemperatureCorrect.byStreamOverlay = 1;  //修正温度码流叠加使能 0-关闭 1-开启
        struTemperatureCorrect.byCorrectEnabled = 1; //测温修正使能 0-关闭 1-开启
        struTemperatureCorrect.dwEmissivity = 95; //黑体发射率:0.01-1.00, 传输时实际值*100换算成整数
        struTemperatureCorrect.dwDistance = 150;   //距离 0.3-2m, 协议传输时以厘米为单位
        struTemperatureCorrect.dwTemperature = 355;  //黑体温度 30.0~50.0℃, 传输时实际值*10换算成整数
        struTemperatureCorrect.dwCentrePointX = 555;  //黑体中心点X坐标，归一化值，范围0-1000
        struTemperatureCorrect.dwCentrePointY = 555;  //黑体中心点Y坐标，归一化值，范围0-1000
        struTemperatureCorrect.dwCorrectTemperature = 1100;  //修正温度 -99.0-99.0℃ 传输时(实际值 + 100) * 10换算成正整数

        if (JavaInterface.getInstance().USB_SetTemperatureCorrect(lUserID, struTemperatureCorrect))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_SetTemperatureCorrect Success! " +
                    " byEnabled:" + struTemperatureCorrect.byEnabled +
                    " byStreamOverlay:" + struTemperatureCorrect.byStreamOverlay +
                    " byCorrectEnabled:" + struTemperatureCorrect.byCorrectEnabled +
                    " dwEmissivity:" + struTemperatureCorrect.dwEmissivity +
                    " dwDistance:" + struTemperatureCorrect.dwDistance +
                    " dwTemperature:" + struTemperatureCorrect.dwTemperature +
                    " dwCentrePointX:" + struTemperatureCorrect.dwCentrePointX +
                    " dwCentrePointY:" + struTemperatureCorrect.dwCentrePointY +
                    " dwCorrectTemperature:" + struTemperatureCorrect.dwCorrectTemperature);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_SetTemperatureCorrect failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byEnabled:" + struTemperatureCorrect.byEnabled +
                    " byStreamOverlay:" + struTemperatureCorrect.byStreamOverlay +
                    " byCorrectEnabled:" + struTemperatureCorrect.byCorrectEnabled +
                    " dwEmissivity:" + struTemperatureCorrect.dwEmissivity +
                    " dwDistance:" + struTemperatureCorrect.dwDistance +
                    " dwTemperature:" + struTemperatureCorrect.dwTemperature +
                    " dwCentrePointX:" + struTemperatureCorrect.dwCentrePointX +
                    " dwCentrePointY:" + struTemperatureCorrect.dwCentrePointY +
                    " dwCorrectTemperature:" + struTemperatureCorrect.dwCorrectTemperature);
            return false;
        }
    }

    //获取黑体参数
    private boolean USB_GetBlackBody(int lUserID)
    {
        USB_BLACK_BODY struBlackBody = new USB_BLACK_BODY();
        if (JavaInterface.getInstance().USB_GetBlackBody(lUserID, struBlackBody))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_GetBlackBody Success! " +
                    " byEnabled:" + struBlackBody.byEnabled +
                    " dwEmissivity:" + struBlackBody.dwEmissivity +
                    " dwDistance:" + struBlackBody.dwDistance +
                    " dwTemperature:" + struBlackBody.dwTemperature +
                    " dwCentrePointX:" + struBlackBody.dwCentrePointX +
                    " dwCentrePointY:" + struBlackBody.dwCentrePointY);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_GetBlackBody failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byEnabled:" + struBlackBody.byEnabled +
                    " dwEmissivity:" + struBlackBody.dwEmissivity +
                    " dwDistance:" + struBlackBody.dwDistance +
                    " dwTemperature:" + struBlackBody.dwTemperature +
                    " dwCentrePointX:" + struBlackBody.dwCentrePointX +
                    " dwCentrePointY:" + struBlackBody.dwCentrePointY);
            return false;
        }
    }

    //设置黑体参数
    private boolean USB_SetBlackBody(int lUserID)
    {
        USB_BLACK_BODY struBlackBody = new USB_BLACK_BODY();
        struBlackBody.byEnabled = 1;  //使能 0-关闭 1-开启
        struBlackBody.dwEmissivity = 95;  //黑体发射率:0.01 - 1.00, 传输时实际值 * 100换算成整数
        struBlackBody.dwDistance = 150; //距离 0.3-2m, 协议传输时以厘米为单位
        struBlackBody.dwTemperature = 355; //黑体温度 30.0~50.0℃, 传输时实际值*10换算成整数
        struBlackBody.dwCentrePointX = 555;   //黑体中心点X坐标，归一化值，范围0-1000
        struBlackBody.dwCentrePointY = 556;  //黑体中心点Y坐标，归一化值，范围0-1000

        if (JavaInterface.getInstance().USB_SetBlackBody(lUserID, struBlackBody))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_SetBlackBody Success! " +
                    " byEnabled:" + struBlackBody.byEnabled +
                    " dwEmissivity:" + struBlackBody.dwEmissivity +
                    " dwDistance:" + struBlackBody.dwDistance +
                    " dwTemperature:" + struBlackBody.dwTemperature +
                    " dwCentrePointX:" + struBlackBody.dwCentrePointX +
                    " dwCentrePointY:" + struBlackBody.dwCentrePointY);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_SetBlackBody failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byEnabled:" + struBlackBody.byEnabled +
                    " dwEmissivity:" + struBlackBody.dwEmissivity +
                    " dwDistance:" + struBlackBody.dwDistance +
                    " dwTemperature:" + struBlackBody.dwTemperature +
                    " dwCentrePointX:" + struBlackBody.dwCentrePointX +
                    " dwCentrePointY:" + struBlackBody.dwCentrePointY);
            return false;
        }
    }

    //获取体温补偿参数
    private boolean USB_GetBodyTemperatureCompensation(int lUserID)
    {
        USB_BODYTEMP_COMPENSATION struBodyTemperatureCompensation = new USB_BODYTEMP_COMPENSATION();
        if (JavaInterface.getInstance().USB_GetBodyTemperatureCompensation(lUserID, struBodyTemperatureCompensation))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_GetBodyTemperatureCompensation Success! " +
                    " byEnabled:" + struBodyTemperatureCompensation.byEnabled +
                    " byType:" + struBodyTemperatureCompensation.byType +
                    " iCompensationValue:" + struBodyTemperatureCompensation.iCompensationValue +
                    " dwSmartCorrection:" + struBodyTemperatureCompensation.dwSmartCorrection +
                    " dwEnvironmentalTemperature:" + struBodyTemperatureCompensation.dwEnvironmentalTemperature +
                    " byEnvironmentalTemperatureMode:" + struBodyTemperatureCompensation.byEnvironmentalTemperatureMode +
                    " byTemperatureCurveSensitivityLevel:" + struBodyTemperatureCompensation.byTemperatureCurveSensitivityLevel +
                    " byEnvironmentCompensationenabled:" + struBodyTemperatureCompensation.byEnvironmentCompensationenabled);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_GetBodyTemperatureCompensation failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byEnabled:" + struBodyTemperatureCompensation.byEnabled +
                    " byType:" + struBodyTemperatureCompensation.byType +
                    " iCompensationValue:" + struBodyTemperatureCompensation.iCompensationValue +
                    " dwSmartCorrection:" + struBodyTemperatureCompensation.dwSmartCorrection +
                    " dwEnvironmentalTemperature:" + struBodyTemperatureCompensation.dwEnvironmentalTemperature +
                    " byEnvironmentalTemperatureMode:" + struBodyTemperatureCompensation.byEnvironmentalTemperatureMode +
                    " byTemperatureCurveSensitivityLevel:" + struBodyTemperatureCompensation.byTemperatureCurveSensitivityLevel +
                    " byEnvironmentCompensationenabled:" + struBodyTemperatureCompensation.byEnvironmentCompensationenabled);
            return false;
        }
    }

    //设置体温补偿参数
    private boolean USB_SetBodyTemperatureCompensation(int lUserID)
    {
        USB_BODYTEMP_COMPENSATION struBodyTemperatureCompensation = new USB_BODYTEMP_COMPENSATION();
        struBodyTemperatureCompensation.byEnabled = 1;  //使能 0-关闭 1-开启
        struBodyTemperatureCompensation.byType = 1;   //补偿方式:1-手动补偿 2-自动补偿
        struBodyTemperatureCompensation.iCompensationValue = -5; //补偿温度 [-10.0 10.0]摄氏度, 传输时实际值*10换算成整数
        struBodyTemperatureCompensation.dwSmartCorrection = 985; //手动校准 -99.0~990.℃, 传输时(实际值+100)*10换算成正整数
        struBodyTemperatureCompensation.dwEnvironmentalTemperature = 985;    //环境温度 -99.0~99.0℃, 传输时(实际值+100)*10换算成正整数
        struBodyTemperatureCompensation.byEnvironmentalTemperatureMode = 1;   //环境温度模式 1-自动模式 2-手动模式
        struBodyTemperatureCompensation.byTemperatureCurveSensitivityLevel = 2;//温度曲线灵敏度等级: 1-低 2-中 3-高
        struBodyTemperatureCompensation.byEnvironmentCompensationenabled = 1;//环境补偿使能: 1-关闭  2-开启

        if (JavaInterface.getInstance().USB_SetBodyTemperatureCompensation(lUserID, struBodyTemperatureCompensation))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_SetBodyTemperatureCompensation Success! " +
                    " byEnabled:" + struBodyTemperatureCompensation.byEnabled +
                    " byType:" + struBodyTemperatureCompensation.byType +
                    " iCompensationValue:" + struBodyTemperatureCompensation.iCompensationValue +
                    " dwSmartCorrection:" + struBodyTemperatureCompensation.dwSmartCorrection +
                    " dwEnvironmentalTemperature:" + struBodyTemperatureCompensation.dwEnvironmentalTemperature +
                    " byEnvironmentalTemperatureMode:" + struBodyTemperatureCompensation.byEnvironmentalTemperatureMode +
                    " byTemperatureCurveSensitivityLevel:" + struBodyTemperatureCompensation.byTemperatureCurveSensitivityLevel +
                    " byEnvironmentCompensationenabled:" + struBodyTemperatureCompensation.byEnvironmentCompensationenabled);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_SetBodyTemperatureCompensation failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byEnabled:" + struBodyTemperatureCompensation.byEnabled +
                    " byType:" + struBodyTemperatureCompensation.byType +
                    " iCompensationValue:" + struBodyTemperatureCompensation.iCompensationValue +
                    " dwSmartCorrection:" + struBodyTemperatureCompensation.dwSmartCorrection +
                    " dwEnvironmentalTemperature:" + struBodyTemperatureCompensation.dwEnvironmentalTemperature +
                    " byEnvironmentalTemperatureMode:" + struBodyTemperatureCompensation.byEnvironmentalTemperatureMode +
                    " byTemperatureCurveSensitivityLevel:" + struBodyTemperatureCompensation.byTemperatureCurveSensitivityLevel +
                    " byEnvironmentCompensationenabled:" + struBodyTemperatureCompensation.byEnvironmentCompensationenabled);
            return false;
        }
    }

    //获取热图
    private boolean USB_GetJpegpicWithAppendData(int lUserID)
    {
        USB_JPEGPIC_WITH_APPENDDATA struJpegpicWithAppendData = new USB_JPEGPIC_WITH_APPENDDATA();
        if (JavaInterface.getInstance().USB_GetJpegpicWithAppendData(lUserID, struJpegpicWithAppendData))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_GetJpegpicWithAppendData Success! " +
                    " dwJpegPicLen:" + struJpegpicWithAppendData.dwJpegPicLen +
                    " dwJpegPicWidth:" + struJpegpicWithAppendData.dwJpegPicWidth +
                    " dwJpegPicHeight:" + struJpegpicWithAppendData.dwJpegPicHeight +
                    " dwP2pDataLen:" + struJpegpicWithAppendData.dwP2pDataLen +
                    " byIsFreezedata:" + struJpegpicWithAppendData.byIsFreezedata +
                    " byTemperatureDataLength:" + struJpegpicWithAppendData.byTemperatureDataLength +
                    " dwScale:" + struJpegpicWithAppendData.dwScale +
                    " iOffset:" + struJpegpicWithAppendData.iOffset);

            //热图
            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss");
            String date = sDateFormat.format(new java.util.Date());
            FileOutputStream file = null;
            try {
                file = new FileOutputStream("/mnt/sdcard/sdklog/" + date + "_p2p.bin");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                file.write(struJpegpicWithAppendData.byP2pData, 0, struJpegpicWithAppendData.dwP2pDataLen);
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            //jpeg
            sDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss");
            date = sDateFormat.format(new java.util.Date());
            FileOutputStream file1 = null;
            try {
                file1 = new FileOutputStream("/mnt/sdcard/sdklog/" + date + "_jpeg.jpg");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                file1.write(struJpegpicWithAppendData.byJpegPic, 0, struJpegpicWithAppendData.dwJpegPicLen);
                file1.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        } else {
            Log.e("[USBDemo]", "USB_GetJpegpicWithAppendData failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " dwJpegPicLen:" + struJpegpicWithAppendData.dwJpegPicLen +
                    " dwJpegPicWidth:" + struJpegpicWithAppendData.dwJpegPicWidth +
                    " dwJpegPicHeight:" + struJpegpicWithAppendData.dwJpegPicHeight +
                    " dwP2pDataLen:" + struJpegpicWithAppendData.dwP2pDataLen +
                    " byIsFreezedata:" + struJpegpicWithAppendData.byIsFreezedata +
                    " byTemperatureDataLength:" + struJpegpicWithAppendData.byTemperatureDataLength +
                    " dwScale:" + struJpegpicWithAppendData.dwScale +
                    " iOffset:" + struJpegpicWithAppendData.iOffset);
            return false;
        }
    }

    //ROI最高温信息查询
    private boolean USB_GetROITemperatureSearch(int lUserID)
    {
        USB_ROI_MAX_TEMPERATURE_SEARCH struROITemperatureSearch = new USB_ROI_MAX_TEMPERATURE_SEARCH();
        struROITemperatureSearch.byJpegPicEnabled = 1;
        struROITemperatureSearch.byMaxTemperatureOverlay = 1;
        struROITemperatureSearch.byRegionsOverlay = 1;
        struROITemperatureSearch.byROIRegionNum = 1;
        struROITemperatureSearch.struThermalROIRegion[0].byROIRegionID = 1;
        struROITemperatureSearch.struThermalROIRegion[0].byROIRegionEnabled = 1;
        struROITemperatureSearch.struThermalROIRegion[0].dwROIRegionX = 250;
        struROITemperatureSearch.struThermalROIRegion[0].dwROIRegionY = 250;
        struROITemperatureSearch.struThermalROIRegion[0].dwROIRegionHeight =250;
        struROITemperatureSearch.struThermalROIRegion[0].dwROIRegionWidth = 250;
        struROITemperatureSearch.struThermalROIRegion[0].dwDistance = 50;

        USB_ROI_MAX_TEMPERATURE_SEARCH_RESULT struROITemperatureSearchResult = new USB_ROI_MAX_TEMPERATURE_SEARCH_RESULT();
        struROITemperatureSearchResult.dwJpegPicLen = JavaInterface.MAX_JEPG_DATA_SIZE;

        if (JavaInterface.getInstance().USB_GetROITemperatureSearch(lUserID, struROITemperatureSearch, struROITemperatureSearchResult))
        {
            //配置成功
            Log.i("[USBDemo]", "struROITemperatureSearchResult Success! " +
                    " dwMaxP2PTemperature:" + struROITemperatureSearchResult.dwMaxP2PTemperature +
                    " dwVisibleP2PMaxTemperaturePointX:" + struROITemperatureSearchResult.dwVisibleP2PMaxTemperaturePointX +
                    " dwVisibleP2PMaxTemperaturePointY:" + struROITemperatureSearchResult.dwVisibleP2PMaxTemperaturePointY +
                    " dwThermalP2PMaxTemperaturePointX:" + struROITemperatureSearchResult.dwThermalP2PMaxTemperaturePointX +
                    " dwThermalP2PMaxTemperaturePointY:" + struROITemperatureSearchResult.dwThermalP2PMaxTemperaturePointY +
                    " byROIRegionNum:" + struROITemperatureSearchResult.byROIRegionNum +
                    " dwJpegPicLen:" + struROITemperatureSearchResult.dwJpegPicLen);
            for (int i = 0; i < struROITemperatureSearchResult.byROIRegionNum; i++)
            {
                Log.i("[USBDemo]", ""+ i +
                        " byROIRegionID:" + struROITemperatureSearchResult.struThermalROIRegionInfo[i].byROIRegionID +
                        " dwMaxROIRegionTemperature:" + struROITemperatureSearchResult.struThermalROIRegionInfo[i].dwMaxROIRegionTemperature +
                        " dwVisibleROIRegionMaxTemperaturePointX:" + struROITemperatureSearchResult.struThermalROIRegionInfo[i].dwVisibleROIRegionMaxTemperaturePointX +
                        " dwVisibleROIRegionMaxTemperaturePointY:" + struROITemperatureSearchResult.struThermalROIRegionInfo[i].dwVisibleROIRegionMaxTemperaturePointY +
                        " dwThermalROIRegionMaxTemperaturePointX:" + struROITemperatureSearchResult.struThermalROIRegionInfo[i].dwThermalROIRegionMaxTemperaturePointX +
                        " dwThermalROIRegionMaxTemperaturePointY:" + struROITemperatureSearchResult.struThermalROIRegionInfo[i].dwThermalROIRegionMaxTemperaturePointY);
            }

            //jpeg
            SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss");
            String date = sDateFormat.format(new java.util.Date());
            FileOutputStream file = null;
            try {
                file = new FileOutputStream("/mnt/sdcard/sdklog/" + date + "_ROI.jpg");
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                file.write(struROITemperatureSearchResult.byJpegPic, 0, struROITemperatureSearchResult.dwJpegPicLen);
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            return true;
        } else {
            Log.e("[USBDemo]", "struROITemperatureSearchResult failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " dwMaxP2PTemperature:" + struROITemperatureSearchResult.dwMaxP2PTemperature +
                    " dwVisibleP2PMaxTemperaturePointX:" + struROITemperatureSearchResult.dwVisibleP2PMaxTemperaturePointX +
                    " dwVisibleP2PMaxTemperaturePointY:" + struROITemperatureSearchResult.dwVisibleP2PMaxTemperaturePointY +
                    " dwThermalP2PMaxTemperaturePointX:" + struROITemperatureSearchResult.dwThermalP2PMaxTemperaturePointX +
                    " dwThermalP2PMaxTemperaturePointY:" + struROITemperatureSearchResult.dwThermalP2PMaxTemperaturePointY +
                    " byROIRegionNum:" + struROITemperatureSearchResult.byROIRegionNum +
                    " dwJpegPicLen:" + struROITemperatureSearchResult.dwJpegPicLen);

            for (int i = 0; i < struROITemperatureSearchResult.byROIRegionNum; i++)
            {
                Log.e("[USBDemo]", ""+ i +
                        " byROIRegionID:" + struROITemperatureSearchResult.struThermalROIRegionInfo[i].byROIRegionID +
                        " dwMaxROIRegionTemperature:" + struROITemperatureSearchResult.struThermalROIRegionInfo[i].dwMaxROIRegionTemperature +
                        " dwVisibleROIRegionMaxTemperaturePointX:" + struROITemperatureSearchResult.struThermalROIRegionInfo[i].dwVisibleROIRegionMaxTemperaturePointX +
                        " dwVisibleROIRegionMaxTemperaturePointY:" + struROITemperatureSearchResult.struThermalROIRegionInfo[i].dwVisibleROIRegionMaxTemperaturePointY +
                        " dwThermalROIRegionMaxTemperaturePointX:" + struROITemperatureSearchResult.struThermalROIRegionInfo[i].dwThermalROIRegionMaxTemperaturePointX +
                        " dwThermalROIRegionMaxTemperaturePointY:" + struROITemperatureSearchResult.struThermalROIRegionInfo[i].dwThermalROIRegionMaxTemperaturePointY);
            }
            return false;
        }
    }

    //获取全屏测温参数
    private boolean USB_GetP2pParam(int lUserID)
    {
        USB_P2P_PARAM struP2pParam = new USB_P2P_PARAM();
        if (JavaInterface.getInstance().USB_GetP2pParam(lUserID, struP2pParam))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_GetP2pParam Success! " +
                    " byJpegPicEnabled:" + struP2pParam.byJpegPicEnabled);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_GetP2pParam failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byJpegPicEnabled:" + struP2pParam.byJpegPicEnabled);
            return false;
        }
    }

    //设置全屏测温参数
    private boolean USB_SetP2pParam(int lUserID)
    {
        USB_P2P_PARAM struP2pParam = new USB_P2P_PARAM();
        struP2pParam.byJpegPicEnabled = 1;  //使能 0-关闭 1-开启

        if (JavaInterface.getInstance().USB_SetP2pParam(lUserID, struP2pParam))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_SetP2pParam Success! " +
                    " byJpegPicEnabled:" + struP2pParam.byJpegPicEnabled);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_SetP2pParam failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byJpegPicEnabled:" + struP2pParam.byJpegPicEnabled);
            return false;
        }
    }

    //测温标定文件导出
    private boolean USB_GetThermometryCalibrationFile(int lUserID)
    {
        USB_THERMOMETRY_CALIBRATION_FILE struThermometryCalibrationFile = new USB_THERMOMETRY_CALIBRATION_FILE();
        if (JavaInterface.getInstance().USB_GetThermometryCalibrationFile(lUserID, struThermometryCalibrationFile))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_GetThermometryCalibrationFile Success! " +
                    " dwFileLenth:" + struThermometryCalibrationFile.dwFileLenth);

            try{
                SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss");
                String date = sDateFormat.format(new java.util.Date());
                FileOutputStream file = new FileOutputStream("/mnt/sdcard/sdklog/" + "MT_TB-4117-3S20200608AACHE49017105.dat");
                file.write(struThermometryCalibrationFile.pCalibrationFile, 0, struThermometryCalibrationFile.dwFileLenth);
                file.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        } else {
            Log.e("[USBDemo]", "USB_GetThermometryCalibrationFile failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " dwFileLenth:" + struThermometryCalibrationFile.dwFileLenth);
            return false;
        }
    }

    //测温标定文件导入
    private boolean USB_SetThermometryCalibrationFile(int lUserID)
    {
        USB_THERMOMETRY_CALIBRATION_FILE struThermometryCalibrationFile = new USB_THERMOMETRY_CALIBRATION_FILE();
        String filePath = "/mnt/sdcard/sdklog/MT_TB-4117-3S20200608AACHE49017105.dat";
        String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
        int iFileSize = 0;
        FileInputStream calibrationFile = null;
        try{
            calibrationFile = new FileInputStream(new File(filePath));
        } catch (IOException e) {
            e.printStackTrace();
        }

        try{
            iFileSize = calibrationFile.available();
        }
        catch(IOException e1){
            e1.printStackTrace();
        }

        if(iFileSize < 0)
        {
            Log.e("[USBDemo]","input file dataSize < 0");
            return false;
        }

        try{
            calibrationFile.read(struThermometryCalibrationFile.pCalibrationFile);

        } catch (IOException e) {
            e.printStackTrace();
        }

        struThermometryCalibrationFile.szFileName = fileName;
        struThermometryCalibrationFile.dwFileLenth = iFileSize;

        if (JavaInterface.getInstance().USB_SetThermometryCalibrationFile(lUserID, struThermometryCalibrationFile))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_SetThermometryCalibrationFile Success! " +
                    " dwFileLenth:" + struThermometryCalibrationFile.dwFileLenth +
                    " byFileName:" + struThermometryCalibrationFile.szFileName);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_SetThermometryCalibrationFile failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " dwFileLenth:" + struThermometryCalibrationFile.dwFileLenth +
                    " byFileName:" + struThermometryCalibrationFile.szFileName);
            return false;
        }
    }


    //获取专家测温规则
    private boolean USB_GetThermometryExpertRegions(int lUserID)
    {
        USB_THERMOMETRY_EXPERT_REGIONS struThermometryExpertRegions = new USB_THERMOMETRY_EXPERT_REGIONS();
        if (JavaInterface.getInstance().USB_GetThermometryExpertRegions(lUserID, struThermometryExpertRegions))
        {
            //配置成功

            Log.i("[USBDemo]", "USB_GetThermometryExpertRegions Success!" +
                    " byRegionNum:" + struThermometryExpertRegions.byRegionNum);
            for (int i = 0; i < struThermometryExpertRegions.byRegionNum && i < JavaInterface.MAX_EXPERT_REGIONS; i++)
            {
                Log.i("[USBDemo]", "" + i +
                        " byRegionID:" + struThermometryExpertRegions.struExpertRegions[i].byRegionID +
                        " byEnabled:" + struThermometryExpertRegions.struExpertRegions[i].byEnabled +
                        " byName:" + struThermometryExpertRegions.struExpertRegions[i].szName +
                        " dwEmissivity:" + struThermometryExpertRegions.struExpertRegions[i].dwEmissivity +
                        " dwDistance:" + struThermometryExpertRegions.struExpertRegions[i].dwDistance +
                        " byReflectiveEnable:" + struThermometryExpertRegions.struExpertRegions[i].byReflectiveEnable +
                        " dwReflectiveTemperature:" + struThermometryExpertRegions.struExpertRegions[i].dwReflectiveTemperature +
                        " byType:" + struThermometryExpertRegions.struExpertRegions[i].byType +
                        " byShowAlarmColorEnabled:" + struThermometryExpertRegions.struExpertRegions[i].byShowAlarmColorEnabled +
                        " byRule:" + struThermometryExpertRegions.struExpertRegions[i].byRule +
                        " dwAlert:" + struThermometryExpertRegions.struExpertRegions[i].dwAlert +
                        " dwAlarm:" + struThermometryExpertRegions.struExpertRegions[i].dwAlarm +
                        " byPointNum:" + struThermometryExpertRegions.struExpertRegions[i].byPointNum);
                for(int l =0;l<  struThermometryExpertRegions.struExpertRegions[i].struRegionCoordinate.length;l++) {
                    Log.e("[USBDemo]", " " + "dwPointX:" +struThermometryExpertRegions.struExpertRegions[i].struRegionCoordinate[l].dwPointX
                            +"dwPointY:" +struThermometryExpertRegions.struExpertRegions[i].struRegionCoordinate[l].dwPointY);
                };

            }
            return true;
        } else {
            Log.e("[USBDemo]", "USB_GetThermometryExpertRegions failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byRegionNum:" + struThermometryExpertRegions.byRegionNum);
            for (int i = 0; i < struThermometryExpertRegions.byRegionNum && i < JavaInterface.MAX_EXPERT_REGIONS; i++)
            {
                Log.e("[USBDemo]", "" + i +
                        " byRegionID:" + struThermometryExpertRegions.struExpertRegions[i].byRegionID +
                        " byEnabled:" + struThermometryExpertRegions.struExpertRegions[i].byEnabled +
                        " byName:" + struThermometryExpertRegions.struExpertRegions[i].szName +
                        " dwEmissivity:" + struThermometryExpertRegions.struExpertRegions[i].dwEmissivity +
                        " dwDistance:" + struThermometryExpertRegions.struExpertRegions[i].dwDistance +
                        " byReflectiveEnable:" + struThermometryExpertRegions.struExpertRegions[i].byReflectiveEnable +
                        " dwReflectiveTemperature:" + struThermometryExpertRegions.struExpertRegions[i].dwReflectiveTemperature +
                        " byType:" + struThermometryExpertRegions.struExpertRegions[i].byType +
                        " byShowAlarmColorEnabled:" + struThermometryExpertRegions.struExpertRegions[i].byShowAlarmColorEnabled +
                        " byRule:" + struThermometryExpertRegions.struExpertRegions[i].byRule +
                        " dwAlert:" + struThermometryExpertRegions.struExpertRegions[i].dwAlert +
                        " dwAlarm:" + struThermometryExpertRegions.struExpertRegions[i].dwAlarm +
                        " byPointNum:" + struThermometryExpertRegions.struExpertRegions[i].byPointNum




                        );

                for(int l =0;l<  struThermometryExpertRegions.struExpertRegions[i].struRegionCoordinate.length;l++) {
                    Log.e("[USBDemo]", " " + "dwPointX:" +struThermometryExpertRegions.struExpertRegions[i].struRegionCoordinate[l].dwPointX
                    +"dwPointY:" +struThermometryExpertRegions.struExpertRegions[i].struRegionCoordinate[l].dwPointY);
                };

            }
            return false;
        }
    }

    //设置专家测温规则
    private boolean USB_SetThermometryExpertRegions(int lUserID)
    {
        USB_THERMOMETRY_EXPERT_REGIONS struThermometryExpertRegions = new USB_THERMOMETRY_EXPERT_REGIONS();
        struThermometryExpertRegions.byRegionNum = 2;//规则区域总个数
        struThermometryExpertRegions.struExpertRegions[0].byRegionID = 1; //区域ID，从1开始递增
        struThermometryExpertRegions.struExpertRegions[0].byEnabled = 1;//区域使能 0-关闭 1-开启
        struThermometryExpertRegions.struExpertRegions[0].dwDistance = 30;
        struThermometryExpertRegions.struExpertRegions[0].szName = new String("0 ");//规则名称
        struThermometryExpertRegions.struExpertRegions[0].dwEmissivity = 98;//发射率: 0.01~1(精确到小数点后两位), 传输时实际值 * 100换算成整数
        struThermometryExpertRegions.struExpertRegions[0].byReflectiveEnable = 1;//反射温度使能：0-关闭 1-开启
        struThermometryExpertRegions.struExpertRegions[0].byType = 3;//规则标定类型: 1-点 2-线 3-框
        struThermometryExpertRegions.struExpertRegions[0].byShowAlarmColorEnabled = 0;//报警颜色显示使能: 1-开启 0-关闭
        struThermometryExpertRegions.struExpertRegions[0].byRule = 1;//报警颜色显示使能: 1-开启 0-关闭
        struThermometryExpertRegions.struExpertRegions[0].dwAlert = 1500;//报警颜色显示使能: 1-开启 0-关闭
        struThermometryExpertRegions.struExpertRegions[0].dwAlarm = 1700;//报警颜色显示使能: 1-开启 0-关闭
        struThermometryExpertRegions.struExpertRegions[0].byPointNum = 4;//区域顶点总个数：当type为1-点时个数为1;   当type为2-线时个数为2 ;   当type为3-框时个数为3-10
        struThermometryExpertRegions.struExpertRegions[0].struRegionCoordinate[0].dwPointX = 0;//X坐标, 归一化0-1000
        struThermometryExpertRegions.struExpertRegions[0].struRegionCoordinate[0].dwPointY = 0;//Y坐标, 归一化0-1000
        struThermometryExpertRegions.struExpertRegions[0].struRegionCoordinate[1].dwPointX = 1000;//X坐标, 归一化0-1000
        struThermometryExpertRegions.struExpertRegions[0].struRegionCoordinate[1].dwPointY = 0;//Y坐标, 归一化0-1000
        struThermometryExpertRegions.struExpertRegions[0].struRegionCoordinate[2].dwPointX = 1000;//X坐标, 归一化0-1000
        struThermometryExpertRegions.struExpertRegions[0].struRegionCoordinate[2].dwPointY = 1000;//Y坐标, 归一化0-1000
        struThermometryExpertRegions.struExpertRegions[0].struRegionCoordinate[3].dwPointX = 0;//X坐标, 归一化0-1000
        struThermometryExpertRegions.struExpertRegions[0].struRegionCoordinate[3].dwPointY = 1000;//Y坐标, 归一化0-1000

        struThermometryExpertRegions.struExpertRegions[1].byRegionID = 2; //区域ID，从1开始递增
        struThermometryExpertRegions.struExpertRegions[1].byEnabled = 1;//区域使能 0-关闭 1-开启
        struThermometryExpertRegions.struExpertRegions[1].dwDistance = 30;
        struThermometryExpertRegions.struExpertRegions[1].szName = new String("2 ");//规则名称
        struThermometryExpertRegions.struExpertRegions[1].dwEmissivity = 98;//发射率: 0.01~1(精确到小数点后两位), 传输时实际值 * 100换算成整数
        struThermometryExpertRegions.struExpertRegions[1].byReflectiveEnable = 1;//反射温度使能：0-关闭 1-开启
        struThermometryExpertRegions.struExpertRegions[1].byType = 3;//规则标定类型: 1-点 2-线 3-框
        struThermometryExpertRegions.struExpertRegions[1].byShowAlarmColorEnabled = 0;//报警颜色显示使能: 1-开启 0-关闭
        struThermometryExpertRegions.struExpertRegions[1].byRule = 1;//报警颜色显示使能: 1-开启 0-关闭
        struThermometryExpertRegions.struExpertRegions[1].dwAlert = 1500;//报警颜色显示使能: 1-开启 0-关闭
        struThermometryExpertRegions.struExpertRegions[1].dwAlarm = 1700;//报警颜色显示使能: 1-开启 0-关闭
        struThermometryExpertRegions.struExpertRegions[1].byPointNum = 4;//区域顶点总个数：当type为1-点时个数为1;   当type为2-线时个数为2 ;   当type为3-框时个数为3-10
        struThermometryExpertRegions.struExpertRegions[1].struRegionCoordinate[0].dwPointX = 480;//X坐标, 归一化0-1000
        struThermometryExpertRegions.struExpertRegions[1].struRegionCoordinate[0].dwPointY = 490;//Y坐标, 归一化0-1000
        struThermometryExpertRegions.struExpertRegions[1].struRegionCoordinate[1].dwPointX = 520;//X坐标, 归一化0-1000
        struThermometryExpertRegions.struExpertRegions[1].struRegionCoordinate[1].dwPointY = 490;//Y坐标, 归一化0-1000
        struThermometryExpertRegions.struExpertRegions[1].struRegionCoordinate[2].dwPointX = 520;//X坐标, 归一化0-1000
        struThermometryExpertRegions.struExpertRegions[1].struRegionCoordinate[2].dwPointY = 510;//Y坐标, 归一化0-1000
        struThermometryExpertRegions.struExpertRegions[1].struRegionCoordinate[3].dwPointX = 480;//X坐标, 归一化0-1000
        struThermometryExpertRegions.struExpertRegions[1].struRegionCoordinate[3].dwPointY = 510;//Y坐标, 归一化0-1000

        if (JavaInterface.getInstance().USB_SetThermometryExpertRegions(lUserID, struThermometryExpertRegions))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_SetThermometryExpertRegions Success! " +
                    " byRegionNum:" + struThermometryExpertRegions.byRegionNum);
            for (int i = 0; i < struThermometryExpertRegions.byRegionNum; i++)
            {
                Log.i("[USBDemo]", "" + i +
                        " byRegionID:" + struThermometryExpertRegions.struExpertRegions[i].byRegionID +
                        " byEnabled:" + struThermometryExpertRegions.struExpertRegions[i].byEnabled +
                        " byName:" + struThermometryExpertRegions.struExpertRegions[i].szName +
                        " dwEmissivity:" + struThermometryExpertRegions.struExpertRegions[i].dwEmissivity +
                        " dwDistance:" + struThermometryExpertRegions.struExpertRegions[i].dwDistance +
                        " byReflectiveEnable:" + struThermometryExpertRegions.struExpertRegions[i].byReflectiveEnable +
                        " dwReflectiveTemperature:" + struThermometryExpertRegions.struExpertRegions[i].dwReflectiveTemperature +
                        " byType:" + struThermometryExpertRegions.struExpertRegions[i].byType +
                        " byShowAlarmColorEnabled:" + struThermometryExpertRegions.struExpertRegions[i].byShowAlarmColorEnabled +
                        " byRule:" + struThermometryExpertRegions.struExpertRegions[i].byRule +
                        " dwAlert:" + struThermometryExpertRegions.struExpertRegions[i].dwAlert +
                        " dwAlarm:" + struThermometryExpertRegions.struExpertRegions[i].dwAlarm +
                        " byPointNum:" + struThermometryExpertRegions.struExpertRegions[i].byPointNum);


                for(int l =0;l<  struThermometryExpertRegions.struExpertRegions[i].struRegionCoordinate.length;l++) {
                    Log.e("[USBDemo]", " " + "dwPointX:" +struThermometryExpertRegions.struExpertRegions[i].struRegionCoordinate[l].dwPointX
                            +"dwPointY:" +struThermometryExpertRegions.struExpertRegions[i].struRegionCoordinate[l].dwPointY);
                };
            }


            return true;
        } else {
            Log.e("[USBDemo]", "USB_SetThermometryExpertRegions failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byRegionNum:" + struThermometryExpertRegions.byRegionNum);
            for (int i = 0; i < struThermometryExpertRegions.byRegionNum; i++)
            {
                Log.e("[USBDemo]", "" + i +
                        " byRegionID:" + struThermometryExpertRegions.struExpertRegions[i].byRegionID +
                        " byEnabled:" + struThermometryExpertRegions.struExpertRegions[i].byEnabled +
                        " byName:" + struThermometryExpertRegions.struExpertRegions[i].szName +
                        " dwEmissivity:" + struThermometryExpertRegions.struExpertRegions[i].dwEmissivity +
                        " dwDistance:" + struThermometryExpertRegions.struExpertRegions[i].dwDistance +
                        " byReflectiveEnable:" + struThermometryExpertRegions.struExpertRegions[i].byReflectiveEnable +
                        " dwReflectiveTemperature:" + struThermometryExpertRegions.struExpertRegions[i].dwReflectiveTemperature +
                        " byType:" + struThermometryExpertRegions.struExpertRegions[i].byType +
                        " byShowAlarmColorEnabled:" + struThermometryExpertRegions.struExpertRegions[i].byShowAlarmColorEnabled +
                        " byRule:" + struThermometryExpertRegions.struExpertRegions[i].byRule +
                        " dwAlert:" + struThermometryExpertRegions.struExpertRegions[i].dwAlert +
                        " dwAlarm:" + struThermometryExpertRegions.struExpertRegions[i].dwAlarm +
                        " byPointNum:" + struThermometryExpertRegions.struExpertRegions[i].byPointNum);
            }
            return false;
        }
    }

    //获取专家测温校正参数
    private boolean USB_GetExpertCorrectionParam(int lUserID)
    {
        USB_THERMOMETRY_EXPERT_CORRECTION_PARAM struExpertCorrectionParam = new USB_THERMOMETRY_EXPERT_CORRECTION_PARAM();
        if (JavaInterface.getInstance().USB_GetExpertCorrectionParam(lUserID, struExpertCorrectionParam))
        {
            //配置成功

            Log.i("[USBDemo]", "USB_GetExpertCorrectionParam Success!" +
                    " dwDistance:" + struExpertCorrectionParam.dwDistance +
                    " dwEnviroTemperature:" + struExpertCorrectionParam.dwEnviroTemperature +
                    " dwEmissivity:" + struExpertCorrectionParam.dwEmissivity +
                    " byPointNum:" + struExpertCorrectionParam.byPointNum);
            for (int i = 0; i < JavaInterface.MAX_TEMPERATURE_NUM; i++)
            {
                Log.i("[USBDemo]", "" + i +
                        " byID:" + struExpertCorrectionParam.struExpertTemperature[i].byID +
                        " dwPresetTemperature:" + struExpertCorrectionParam.struExpertTemperature[i].dwPresetTemperature +
                        " dwPointX:" + struExpertCorrectionParam.struExpertTemperature[i].dwPointX +
                        " dwPointY:" + struExpertCorrectionParam.struExpertTemperature[i].dwPointY);
            }
            return true;
        } else {
            Log.e("[USBDemo]", "USB_GetExpertCorrectionParam failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " dwDistance:" + struExpertCorrectionParam.dwDistance +
                    " dwEnviroTemperature:" + struExpertCorrectionParam.dwEnviroTemperature +
                    " dwEmissivity:" + struExpertCorrectionParam.dwEmissivity +
                    " byPointNum:" + struExpertCorrectionParam.byPointNum);
            for (int i = 0; i < JavaInterface.MAX_TEMPERATURE_NUM; i++)
            {
                Log.e("[USBDemo]", "" + i +
                        " byID:" + struExpertCorrectionParam.struExpertTemperature[i].byID +
                        " dwPresetTemperature:" + struExpertCorrectionParam.struExpertTemperature[i].dwPresetTemperature +
                        " dwPointX:" + struExpertCorrectionParam.struExpertTemperature[i].dwPointX +
                        " dwPointY:" + struExpertCorrectionParam.struExpertTemperature[i].dwPointY);
            }
            return false;
        }
    }

    //设置专家测温校正参数
    private boolean USB_SetExpertCorrectionParam(int lUserID)
    {
        USB_THERMOMETRY_EXPERT_CORRECTION_PARAM struExpertCorrectionParam = new USB_THERMOMETRY_EXPERT_CORRECTION_PARAM();

        struExpertCorrectionParam.byPointNum = 3;
        struExpertCorrectionParam.dwDistance = 100;
        struExpertCorrectionParam.dwEnviroTemperature = 3200;
        struExpertCorrectionParam.dwEmissivity = 98;
        struExpertCorrectionParam.struExpertTemperature[0].byID = 1;
        struExpertCorrectionParam.struExpertTemperature[0].dwPresetTemperature = 1350;
        struExpertCorrectionParam.struExpertTemperature[0].dwPointX = 100;
        struExpertCorrectionParam.struExpertTemperature[0].dwPointY = 500;

        struExpertCorrectionParam.struExpertTemperature[1].byID = 2;
        struExpertCorrectionParam.struExpertTemperature[1].dwPresetTemperature = 1600;
        struExpertCorrectionParam.struExpertTemperature[1].dwPointX = 500;
        struExpertCorrectionParam.struExpertTemperature[1].dwPointY = 500;

        struExpertCorrectionParam.struExpertTemperature[2].byID = 3;
        struExpertCorrectionParam.struExpertTemperature[2].dwPresetTemperature = 2500;
        struExpertCorrectionParam.struExpertTemperature[2].dwPointX = 800;
        struExpertCorrectionParam.struExpertTemperature[2].dwPointY = 500;

        if (JavaInterface.getInstance().USB_SetExpertCorrectionParam(lUserID, struExpertCorrectionParam))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_SetExpertCorrectionParam Success! " +
                    " dwDistance:" + struExpertCorrectionParam.dwDistance +
                    " dwEnviroTemperature:" + struExpertCorrectionParam.dwEnviroTemperature +
                    " dwEmissivity:" + struExpertCorrectionParam.dwEmissivity +
                    " byPointNum:" + struExpertCorrectionParam.byPointNum);
            for (int i = 0; i < JavaInterface.MAX_TEMPERATURE_NUM; i++)
            {
                Log.i("[USBDemo]", "" + i +
                        " byID:" + struExpertCorrectionParam.struExpertTemperature[i].byID +
                        " dwPresetTemperature:" + struExpertCorrectionParam.struExpertTemperature[i].dwPresetTemperature +
                        " dwPointX:" + struExpertCorrectionParam.struExpertTemperature[i].dwPointX +
                        " dwPointY:" + struExpertCorrectionParam.struExpertTemperature[i].dwPointY);
            }
            return true;
        } else {
            Log.e("[USBDemo]", "USB_SetExpertCorrectionParam failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " dwDistance:" + struExpertCorrectionParam.dwDistance +
                    " dwEnviroTemperature:" + struExpertCorrectionParam.dwEnviroTemperature +
                    " dwEmissivity:" + struExpertCorrectionParam.dwEmissivity +
                    " byPointNum:" + struExpertCorrectionParam.byPointNum);
            for (int i = 0; i < JavaInterface.MAX_TEMPERATURE_NUM; i++)
            {
                Log.e("[USBDemo]", "" + i +
                        " byID:" + struExpertCorrectionParam.struExpertTemperature[i].byID +
                        " dwPresetTemperature:" + struExpertCorrectionParam.struExpertTemperature[i].dwPresetTemperature +
                        " dwPointX:" + struExpertCorrectionParam.struExpertTemperature[i].dwPointX +
                        " dwPointY:" + struExpertCorrectionParam.struExpertTemperature[i].dwPointY);
            }
            return false;
        }
    }

    //开始专家测温校正
    private boolean USB_StartExpertCorrection(int lUserID)
    {
        if (JavaInterface.getInstance().USB_StartExpertCorrection(lUserID))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_StartExpertCorrection Success! ");
            return true;
        } else {
            Log.e("[USBDemo]", "USB_StartExpertCorrection failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            return false;
        }
    }

    //设置图像WDR
    private boolean USB_SetImageWDR(int lUserID)
    {
        USB_IMAGE_WDR struImageWDR = new USB_IMAGE_WDR();
        struImageWDR.byEnabled = 1;
        struImageWDR.byMode = 1;
        struImageWDR.byLevel = 1;
        if (JavaInterface.getInstance().USB_SetImageWDR(lUserID, struImageWDR))
        {
            //设置成功
            Log.i("[USBDemo]", "USB_SetImageWDR Success!" +
                    " byEnabled:" + struImageWDR.byEnabled + " byMode:" + struImageWDR.byMode + " byLevel:" + struImageWDR.byLevel);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_SetImageWDR failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            return false;
        }
    }

    //获取音频输入状态
    private boolean USB_GetAudioInStatus(int lUserID)
    {
        USB_AUDIO_STATUS struAudioStatus = new USB_AUDIO_STATUS();
        struAudioStatus.byChannelID = 0;
        if (JavaInterface.getInstance().USB_GetAudioInStatus(lUserID, struAudioStatus))
        {
            //获取成功
            Log.i("[USBDemo]", "USB_GetAudioInStatus Success!" +
                    " byChannelID:" + struAudioStatus.byChannelID + " byConnectStatus:" + struAudioStatus.byConnectStatus);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_GetAudioInStatus failed! error:" + JavaInterface.getInstance().USB_GetLastError());
            return false;
        }
    }

    //获取温升配置参数
    private boolean USB_GetRiseSettings(int lUserID)
    {
        USB_THERMOMETRY_RISE_SETTINGS struRiseSettings = new USB_THERMOMETRY_RISE_SETTINGS();
        if (JavaInterface.getInstance().USB_GetRiseSettings(lUserID, struRiseSettings))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_GetRiseSettings Success!" +
                    " byEnabled:" + struRiseSettings.byEnabled +
                    " byType:" + struRiseSettings.byType +
                    " byResult:" + struRiseSettings.byResult +
                    " dwEnvTemperature:" + struRiseSettings.dwEnvTemperature +
                    " dwMaxTemperatureRise:" + struRiseSettings.dwMaxTemperatureRise);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_GetRiseSettings failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byEnabled:" + struRiseSettings.byEnabled +
                    " byType:" + struRiseSettings.byType +
                    " byResult:" + struRiseSettings.byResult +
                    " dwEnvTemperature:" + struRiseSettings.dwEnvTemperature +
                    " dwMaxTemperatureRise:" + struRiseSettings.dwMaxTemperatureRise);
            return false;
        }
    }

    //设置温升配置参数
    private boolean USB_SetRiseSettings(int lUserID)
    {
        USB_THERMOMETRY_RISE_SETTINGS struRiseSettings = new USB_THERMOMETRY_RISE_SETTINGS();
        struRiseSettings.byEnabled = 1;//启用温升设置 0-关闭  1-开启
        struRiseSettings.byType = 0;//温升参数获取方式 0-自动获取 1-手动输入
        struRiseSettings.dwEnvTemperature = 40;//环境温度: -99.0~99.0℃ (精确到小数点后两位), 传输时(实际值+100)*10换算成正整数
        struRiseSettings.dwCoefficient = 1;//温升系数: -10~10
        struRiseSettings.dwMaxTemperatureRise= 14;//最大温升: 2~20
        struRiseSettings.dwColdStartRate = 10;//冷开机温升速率: 0.01~0.5 (精确到小数点后两位), 传输时实际值*100换算成整数
        struRiseSettings.dwColdStartRise = 1000;//冷开机温升: -3.0~3.0 (精确到小数点后1位), 传输时(实际值+100)*10换算成正整数


        if (JavaInterface.getInstance().USB_SetRiseSettings(lUserID, struRiseSettings))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_SetRiseSettings Success! " +
                    " byEnabled:" + struRiseSettings.byEnabled +
                    " byType:" + struRiseSettings.byType +
                    " byResult:" + struRiseSettings.byResult +
                    " dwEnvTemperature:" + struRiseSettings.dwEnvTemperature +
                    " dwCoefficient:" + struRiseSettings.dwCoefficient);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_SetRiseSettings failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byEnabled:" + struRiseSettings.byEnabled +
                    " byType:" + struRiseSettings.byType +
                    " byResult:" + struRiseSettings.byResult +
                    " dwEnvTemperature:" + struRiseSettings.dwEnvTemperature +
                    " dwCoefficient:" + struRiseSettings.dwCoefficient);
            return false;
        }
    }

    //获取环境温度校正参数
    private boolean USB_GetEnvirotemperatureCorrect(int lUserID)
    {
        USB_ENVIROTEMPERATURE_CORRECT struEnvirotemperatureCorrect = new USB_ENVIROTEMPERATURE_CORRECT();
        if (JavaInterface.getInstance().USB_GetEnvirotemperatureCorrect(lUserID, struEnvirotemperatureCorrect))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_GetEnvirotemperatureCorrect Success!" +
                    " byEnabled:" + struEnvirotemperatureCorrect.byEnabled +
                    " byCorrectEnabled:" + struEnvirotemperatureCorrect.byCorrectEnabled +
                    " dwEnviroTemperature:" + struEnvirotemperatureCorrect.dwEnviroTemperature +
                    " dwCalibrationTemperature:" + struEnvirotemperatureCorrect.dwCalibrationTemperature);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_GetEnvirotemperatureCorrect failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byEnabled:" + struEnvirotemperatureCorrect.byEnabled +
                    " byCorrectEnabled:" + struEnvirotemperatureCorrect.byCorrectEnabled +
                    " dwEnviroTemperature:" + struEnvirotemperatureCorrect.dwEnviroTemperature +
                    " dwCalibrationTemperature:" + struEnvirotemperatureCorrect.dwCalibrationTemperature);
            return false;
        }
    }

    //设置环境温度校正参数
    private boolean USB_SetEnvirotemperatureCorrect(int lUserID)
    {
        USB_ENVIROTEMPERATURE_CORRECT struEnvirotemperatureCorrect = new USB_ENVIROTEMPERATURE_CORRECT();
        struEnvirotemperatureCorrect.byEnabled = 1; //总使能, 用于开关校准参数是否生效  0-关闭  1-开启
        struEnvirotemperatureCorrect.byCorrectEnabled = 1 ;//环境温度校准使能, 用于生成校准参数  0-关闭  1-开启
        struEnvirotemperatureCorrect.dwEnviroTemperature = 1000;//环境温度校准值, -20.0~50.0℃(精确到小数点后1位), 传输时(实际值+100)*10换算成正整数
        struEnvirotemperatureCorrect.dwCalibrationTemperature = 1;//(只读)温变校准值结果, -20.0~50.0℃(精确到小数点后1位), 传输时(实际值+100)*10换算成正整数


        if (JavaInterface.getInstance().USB_SetEnvirotemperatureCorrect(lUserID, struEnvirotemperatureCorrect))
        {
            //配置成功
            Log.i("[USBDemo]", "USB_SetEnvirotemperatureCorrect Success! " +
                    " byEnabled:" + struEnvirotemperatureCorrect.byEnabled +
                    " byCorrectEnabled:" + struEnvirotemperatureCorrect.byCorrectEnabled +
                    " dwEnviroTemperature:" + struEnvirotemperatureCorrect.dwEnviroTemperature +
                    " dwCalibrationTemperature:" + struEnvirotemperatureCorrect.dwCalibrationTemperature);
            return true;
        } else {
            Log.e("[USBDemo]", "USB_SetEnvirotemperatureCorrect failed! error:" + JavaInterface.getInstance().USB_GetLastError() +
                    " byEnabled:" + struEnvirotemperatureCorrect.byEnabled +
                    " byCorrectEnabled:" + struEnvirotemperatureCorrect.byCorrectEnabled +
                    " dwEnviroTemperature:" + struEnvirotemperatureCorrect.dwEnviroTemperature +
                    " dwCalibrationTemperature:" + struEnvirotemperatureCorrect.dwCalibrationTemperature);
            return false;
        }
    }

}

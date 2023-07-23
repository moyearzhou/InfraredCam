package com.moyear;

import android.content.Context;
import android.util.Log;

import com.hcusbsdk.Interface.JavaInterface;
import com.hcusbsdk.Interface.USB_UPGRADE_INFO;
import com.hcusbsdk.Interface.USB_UPGRADE_STATE_INFO;
import com.hcusbsdk.jna.HCUSBSDK;

public class Upgrade {
    //用于显示提示框
    private Context m_mainActivity = null;
    private int m_dwUserID = JavaInterface.USB_INVALID_USER_ID; //设备句柄
    private int m_lUpgradeHandle = JavaInterface.USB_INVALID_CHANNEL; //升级句柄
    private Thread m_getStateThread = null; //获取状态线程
    private boolean m_bExit = true; //true-退出获取状态线程 false-获取状态线程运行中

    public Upgrade(Context context) {
        m_mainActivity = context;
    }

    //设置设备句柄
    public void SetUserID(int iUserID)
    {
        m_dwUserID = iUserID;
        Log.i("[USBDemo]", "SetUserID Success! m_dwUserID:" + m_dwUserID);
    }

    //开始升级
    public boolean StartUpgrade() {
        USB_UPGRADE_INFO struUpgradeInfo = new USB_UPGRADE_INFO();
        struUpgradeInfo.szFileName = new String( "/sdcard/HM-TM32-3RGT_0953560100_UVC_FPGA.bin");

        m_lUpgradeHandle = JavaInterface.getInstance().USB_Upgrade(m_dwUserID, struUpgradeInfo);
        if (m_lUpgradeHandle > 0)
        {
            Log.i("[USBDemo]","USB_Upgrade Success! m_lUpgradeHandle:" + m_lUpgradeHandle +
                    " szFileName:" + struUpgradeInfo.szFileName);
            return true;
        }
        else
        {
            Log.e("[USBDemo]","USB_GetUpgradeState failed! error:" + HCUSBSDK.getInstance().USB_GetLastError() +
                    " m_lUpgradeHandle:" + m_lUpgradeHandle +
                    " szFileName:" + struUpgradeInfo.szFileName);
            return false;
        }
    }

    //停止升级
    public boolean StopUpgrade() {
        m_bExit = true; //退出获取状态线程
        m_getStateThread = null;

        if (JavaInterface.getInstance().USB_CloseUpgradeHandle(m_lUpgradeHandle))
        {
            Log.i("[USBDemo]","USB_CloseUpgradeHandle Success! m_lUpgradeHandle:" + m_lUpgradeHandle);
            return true;
        }
        else
        {
            Log.e("[USBDemo]","USB_CloseUpgradeHandle failed! error:" + HCUSBSDK.getInstance().USB_GetLastError() +
                    " m_lUpgradeHandle:" + m_lUpgradeHandle);
            return false;
        }
    }

    //定时获取升级状态
    public boolean GetUpgradeState() {
        if (m_getStateThread != null)
        {
            return false;
        }

        m_bExit = false;
        m_getStateThread = new Thread(new UpgradeStateThread());
        m_getStateThread.start();
        return true;
    }

    class UpgradeStateThread implements Runnable {
        @Override
        public void run() {
            GetState();
        }
    }

    private void GetState()
    {
        while (!m_bExit)
        {
            USB_UPGRADE_STATE_INFO struUpgradeState = new USB_UPGRADE_STATE_INFO();
            if (JavaInterface.getInstance().USB_GetUpgradeState(m_lUpgradeHandle, struUpgradeState))
            {
                Log.i("[USBDemo]","USB_GetUpgradeState Success! m_lUpgradeHandle:" + m_lUpgradeHandle +
                        " byState:" + struUpgradeState.byState +
                        " byProgress:" + struUpgradeState.byProgress);
//                Toast.makeText(m_mainActivity, " State:" + struUpgradeState.byState +
//                        " Progress:" + struUpgradeState.byProgress, Toast.LENGTH_SHORT).show();
            }
            else
            {
                Log.e("[USBDemo]","USB_GetUpgradeState failed! error:" + HCUSBSDK.getInstance().USB_GetLastError() +
                        " m_lUpgradeHandle:" + m_lUpgradeHandle +
                        " byState:" + struUpgradeState.byState +
                        " byProgress:" + struUpgradeState.byProgress);
//                Toast.makeText(m_mainActivity, "USB_GetUpgradeState failed! error:" + HCUSBSDK.getInstance().USB_GetLastError(), Toast.LENGTH_SHORT).show();
            }

            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}

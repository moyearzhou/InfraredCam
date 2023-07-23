package com.moyear

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager

class UsbReceiver: BroadcastReceiver() {

    var onUsbOperateCallback: OnUsbOperateCallback ?= null

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED == action) {
            // USB设备插入
            val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
            device?.let {
                // 处理设备连接逻辑
                // 比如检查设备的Vendor ID和Product ID是否符合预期
                onUsbOperateCallback?.onAttach(it)
            }
        } else if (UsbManager.ACTION_USB_DEVICE_DETACHED == action) {
            // USB设备拔出
            val device = intent.getParcelableExtra<UsbDevice>(UsbManager.EXTRA_DEVICE)
            device?.let {
                // 处理设备拔出逻辑
                onUsbOperateCallback?.onDetach(it)
            }
        }
    }

}

interface OnUsbOperateCallback {
    fun onAttach(usbDevice: UsbDevice)
    fun onDetach(usbDevice: UsbDevice)
}
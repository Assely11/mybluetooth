package com.example.mybluetooth.Interface;

import android.bluetooth.BluetoothDevice;

/**
 * 找到设备监听者
 * */
public interface DeviceFindListener {
    void onDeviceFound(BluetoothDevice device);
}

package ar.com.pinard.bletester;

import android.bluetooth.BluetoothGattCharacteristic;

public interface NoticeCharacteristicDialogListener {
    void onWriteCharacteristic(BluetoothGattCharacteristic characteristic, String value);
    void onReadCharacteristic(BluetoothGattCharacteristic characteristic);
    void onEnableNotification(BluetoothGattCharacteristic characteristic);
    void onDisableNotification(BluetoothGattCharacteristic characteristic);
}

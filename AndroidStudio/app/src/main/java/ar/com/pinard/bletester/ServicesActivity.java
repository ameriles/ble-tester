package ar.com.pinard.bletester;

import android.app.DialogFragment;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Parcelable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import ar.com.pinard.bletester.adapters.BluetoothServiceAdapter;
import ar.com.pinard.bletester.fragments.CharacteristicDialogFragment;

public class ServicesActivity extends AppCompatActivity implements CharacteristicDialogFragment.NoticeCharacteristicDialogListener{

    public static final String BLUETOOTH_DEVICE = "BLUETOOTH_DEVICE";
    private static final String TAG = ServicesActivity.class.getSimpleName();
    private BluetoothDevice mDevice;
    private BluetoothGatt mBluetoothGatt;
    private ListView mServicesListView;
    private BluetoothServiceAdapter mServicesAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_services);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null) {
            mDevice = bundle.getParcelable(BLUETOOTH_DEVICE);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(mDevice.getName() != null ? mDevice.getName() : mDevice.getAddress() + "/Services");
        }

        // initialize members
        mServicesAdapter = new BluetoothServiceAdapter(this, new ArrayList<BluetoothGattService>());
        mServicesListView = findViewById(R.id.services_lv_services);
        mServicesListView.setAdapter(mServicesAdapter);
        mServicesListView.setOnItemClickListener(servicesOnItemClickListener);

        tryConnect();
    }

    private void tryConnect() {
        mServicesAdapter.clear();
        mBluetoothGatt = mDevice.connectGatt(this, true, gattCallback);
    }

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);

            String statusStr = "";
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    mBluetoothGatt.discoverServices();
                    Log.i(TAG, "Connected to GATT server.");
                    Log.i(TAG, "Attempting to start service discovery: " + mBluetoothGatt.discoverServices());
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.i(TAG, "Disconnected from GATT server.");
                    break;
                case BluetoothProfile.STATE_CONNECTING:
                    Log.i(TAG, "Connecting to GATT server...");
                    break;
                case BluetoothProfile.STATE_DISCONNECTING:
                    Log.i(TAG, "Disconnecting from GATT server...");
                    break;
            }
        }

        @Override
        // New services discovered
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                for (BluetoothGattService service : gatt.getServices()) {
                    tryAddServiceToAdapter(service);
                }

                Log.d(TAG, "GATT_SUCCESS");
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "READ: " + characteristic.getUuid().toString() + " = " + characteristic.getStringValue(0));
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            Log.d(TAG, "WRITE: " + characteristic.getUuid().toString() + " = " + characteristic.getStringValue(0));
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            Log.d(TAG, "CHANGED: " + characteristic.getUuid().toString() + " = " + characteristic.getStringValue(0));
        }
    };

    private void tryAddServiceToAdapter(BluetoothGattService service) {
        boolean exists = false;
        for (int i = 0; i < mServicesAdapter.getCount(); ++i) {
            BluetoothGattService s = mServicesAdapter.getItem(i);
            if (s.getUuid().equals(service.getUuid())) {
                exists = true;
                break;
            }
        }

        if (!exists) {
            mServicesAdapter.add(service);
        }
    }

    private AdapterView.OnItemClickListener servicesOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

            BluetoothGattService service = mServicesAdapter.getItem(i);

            Bundle bundle = new Bundle();
            bundle.putParcelable(CharacteristicDialogFragment.BLUETOOTH_SERVICE, service);

            DialogFragment dialog = new CharacteristicDialogFragment();
            dialog.setArguments(bundle);
            dialog.show(getFragmentManager(), "characteristics");

//            StringBuilder sb = new StringBuilder();
//            for (BluetoothGattCharacteristic c : service.getCharacteristics()) {
//                sb.append(c.getUuid().toString());
//                sb.append(":");
//                sb.append(c.getValue() == null ? "null" : c.getValue().toString());
//                sb.append('\n');
//
//                byte[] value = new byte[1];
//                value[0] = (byte) (21 & 0xFF);
//                c.setValue(value);
//                boolean status = mBluetoothGatt.writeCharacteristic(c);
//                Log.d(TAG, "writeCharacteristic " + status);
//            }
//
//            Toast.makeText(ServicesActivity.this, sb.toString(), Toast.LENGTH_SHORT).show();


        }
    };

    @Override
    public void onWriteCharacteristic(BluetoothGattCharacteristic characteristic, String value) {
        characteristic.setValue(value);
        mBluetoothGatt.writeCharacteristic(characteristic);
    }

    @Override
    public void onReadCharacteristic(BluetoothGattCharacteristic characteristic) {
        Log.d(TAG, "READ: " + characteristic.getUuid());
    }

    @Override
    public void onEnableNotification(BluetoothGattCharacteristic characteristic) {
        mBluetoothGatt.setCharacteristicNotification(characteristic, true);

        UUID uuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(uuid);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE); // could be ENABLE_INDICATION_VALUE depending on the device
        mBluetoothGatt.writeDescriptor(descriptor);
    }

    @Override
    public void onDisableNotification(BluetoothGattCharacteristic characteristic) {
        mBluetoothGatt.setCharacteristicNotification(characteristic, false);

        UUID uuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(uuid);
        descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE); // could be DISABLE_INDICATION_VALUE depending on the device
        mBluetoothGatt.writeDescriptor(descriptor);
    }
}

package ar.com.pinard.bletester;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

import ar.com.pinard.bletester.adapters.BluetoothDeviceAdapter;

public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private final int REQUEST_ENABLE_BT = 1;
    private final int REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS = 2;
    private final int SCANNING_TIME_MS = 5000; // 5 seconds
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private ListView mDevicesListView;
    private BluetoothDeviceAdapter mDevicesAdapter;
    private boolean mIsScanning;
    private SwipeRefreshLayout mMainSwipeRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initialize members
        mMainSwipeRefresh = findViewById(R.id.main_swipe_refresh);
        mMainSwipeRefresh.setOnRefreshListener(this);

        mDevicesAdapter = new BluetoothDeviceAdapter(this, new ArrayList<BluetoothDevice>());
        mDevicesListView = findViewById(R.id.main_lv_devices);
        mDevicesListView.setAdapter(mDevicesAdapter);
        mDevicesListView.setOnItemClickListener(devicesOnItemClickListener);

        testBluetoothLeFeature();

        testBluetoothPermissions();
    }

    private void testBluetoothPermissions() {
        // If has the required permissions granted
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                // TODO
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS);
            }
        } else {
            tryStartBlueetoothScan();
        }
    }

    private void testBluetoothLeFeature() {
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void tryStartBlueetoothScan() {
        mBluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = mBluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            scanForDevices();
        }
    }

    private void scanForDevices() {
        if (mIsScanning) return;

        mIsScanning = true;
        mMainSwipeRefresh.setRefreshing(true);

        // clear devices
        mDevicesAdapter.clear();

        // starts the bluetooth devices scan
        mBluetoothAdapter.startLeScan(scanCallback);

        new android.os.Handler().postDelayed(
                new Runnable() {
                    public void run() {
                        stopScan();
                    }
                },
                SCANNING_TIME_MS);
    }

    private void stopScan() {
        mBluetoothAdapter.stopLeScan(scanCallback);
        mIsScanning = false;
        mMainSwipeRefresh.setRefreshing(false);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopScan();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // permission was granted, yay! Do the
                // contacts-related task you need to do.
                tryStartBlueetoothScan();
            } else {
                // permission denied, boo! Disable the
                // functionality that depends on this permission.
            }
            return;
        }

        // other 'case' lines to check for other
        // permissions this app might request
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater findMenuItems = getMenuInflater();
        findMenuItems.inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.main_menu_scan:
                scanForDevices();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    BluetoothAdapter.LeScanCallback scanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            boolean exists = false;
            for (int i = 0; i < mDevicesAdapter.getCount(); i++) {
                BluetoothDevice d = mDevicesAdapter.getItem(i);
                if (d.getAddress().equals(device.getAddress())) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                mDevicesAdapter.add(device);
            }
        }
    };

    @Override
    public void onRefresh() {
        scanForDevices();
    }

    private AdapterView.OnItemClickListener devicesOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            // start new activity with the selected bluetooth device to discover services
            BluetoothDevice device = mDevicesAdapter.getItem(i);

            Bundle bundle = new Bundle();
            bundle.putParcelable(ServicesActivity.BLUETOOTH_DEVICE, device);

            Intent intent = new Intent(MainActivity.this, ServicesActivity.class);
            intent.putExtras(bundle);

            startActivity(intent);
        }
    };
}
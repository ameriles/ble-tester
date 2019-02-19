package ar.com.pinard.bletester.adapters;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import ar.com.pinard.bletester.R;

public class BluetoothDeviceAdapter extends ArrayAdapter<BluetoothDevice> {
    public BluetoothDeviceAdapter(Context context, List<BluetoothDevice> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        BluetoothDevice device = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.bluetooth_device_item, parent, false);
        }
        // Lookup view for data population
        TextView tvName = convertView.findViewById(R.id.device_tv_name);
        TextView tvAddress = convertView.findViewById(R.id.device_tv_address);
        // Populate the data into the template view using the data object
        String name = device.getName();
        tvName.setText(name == null ? "<Unknown device>" : name);
        tvAddress.setText(device.getAddress());
        // Return the completed view to render on screen
        return convertView;
    }

}
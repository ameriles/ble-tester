package ar.com.pinard.bletester.adapters;

import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import ar.com.pinard.bletester.R;

public class BluetoothServiceAdapter extends ArrayAdapter<BluetoothGattService> {

    public BluetoothServiceAdapter(@NonNull Context context, @NonNull List<BluetoothGattService> objects) {
        super(context, 0, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        BluetoothGattService service = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.bluetooth_service_item, parent, false);
        }

        // Lookup view for data population
        TextView tvType = convertView.findViewById(R.id.service_tv_type);
        TextView tvUuid = convertView.findViewById(R.id.service_tv_uuid);

        // Populate the data into the template view using the data object
        tvType.setText(service.getType() == BluetoothGattService.SERVICE_TYPE_PRIMARY ? "PRIMARY" : "SECONDARY");
        tvUuid.setText(service.getUuid().toString());

        // Return the completed view to render on screen
        return convertView;
    }
}

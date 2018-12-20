package ar.com.pinard.bletester.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.util.ArrayList;
import java.util.UUID;

import ar.com.pinard.bletester.R;

public class CharacteristicDialogFragment extends DialogFragment {

    public interface NoticeCharacteristicDialogListener {
        void onWriteCharacteristic(BluetoothGattCharacteristic characteristic, String value);
        void onReadCharacteristic(BluetoothGattCharacteristic characteristic);
        void onEnableNotification(BluetoothGattCharacteristic characteristic);
        void onDisableNotification(BluetoothGattCharacteristic characteristic);
    }

    public static String BLUETOOTH_SERVICE = "BLUETOOH_SERVICE";
    private BluetoothGattService mService;
    private ArrayAdapter<String> mCharacteristicsAdapter;
    private TextView mTvReadValue;
    private EditText mEtWriteValue;

    private NoticeCharacteristicDialogListener mListener;
    private BluetoothGattCharacteristic mSelectedCharacteristic;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            mListener = (NoticeCharacteristicDialogListener)activity;
        } catch (ClassCastException e){
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement NoticeDialogListener");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle bundle = getArguments();
        if(bundle != null) {
            mService = bundle.getParcelable(BLUETOOTH_SERVICE);
        }

        ArrayList<String> characteristics = new ArrayList<>();
        for (BluetoothGattCharacteristic c : mService.getCharacteristics()) {
            characteristics.add(c.getUuid().toString());
        }

        mCharacteristicsAdapter  = new ArrayAdapter<String>(getContext(), android.R.layout.simple_list_item_1, characteristics);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout
        View view = inflater.inflate(R.layout.dialog_characteristic, null);

        TextView tvService = view.findViewById(R.id.characteristic_tv_service);
        tvService.setText(mService.getUuid().toString());

        Spinner spCharacteristics = view.findViewById(R.id.characteristic_sp_characteristics);
        spCharacteristics.setAdapter(mCharacteristicsAdapter);
        spCharacteristics.setOnItemSelectedListener(characteristicsOnItemSelectedListener);

        mTvReadValue = view.findViewById(R.id.characteristic_tv_read_value);
        mEtWriteValue = view.findViewById(R.id.characteristic_et_write_value);

        Button btRead = view.findViewById(R.id.characteristic_bt_read);
        btRead.setOnClickListener(btReadOnClickListener);

        Button btWrite = view.findViewById(R.id.characteristic_bt_write);
        btWrite.setOnClickListener(btWriteOnClickListener);

        ToggleButton tbNotification = view.findViewById(R.id.characteristic_tb_notification);
        tbNotification.setOnCheckedChangeListener(tbNotificationOnCheckedChangeListener);

        builder.setView(view)
                // Add action buttons
                .setPositiveButton("CERRAR", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        CharacteristicDialogFragment.this.getDialog().cancel();
                    }
                });

        return builder.create();
    }

    private AdapterView.OnItemSelectedListener characteristicsOnItemSelectedListener = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
            String uuid = mCharacteristicsAdapter.getItem(i);
            mSelectedCharacteristic = mService.getCharacteristic(UUID.fromString(uuid));

            if (mSelectedCharacteristic.getValue() != null) {
                mTvReadValue.setText(String.valueOf(mSelectedCharacteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 0).intValue()));
            } else {
                mTvReadValue.setText("null");
            }
        }

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {

        }
    };

    private View.OnClickListener btReadOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mListener.onReadCharacteristic(mSelectedCharacteristic);
        }
    };

    private View.OnClickListener btWriteOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            mListener.onWriteCharacteristic(mSelectedCharacteristic, mEtWriteValue.getText().toString());
        }
    };

    private CompoundButton.OnCheckedChangeListener tbNotificationOnCheckedChangeListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
            if (b) {
                mListener.onEnableNotification(mSelectedCharacteristic);
            } else {
                mListener.onDisableNotification(mSelectedCharacteristic);
            }
        }
    };
}

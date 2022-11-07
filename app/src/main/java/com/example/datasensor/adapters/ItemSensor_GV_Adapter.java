package com.example.datasensor.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.datasensor.R;

import java.util.ArrayList;

public class ItemSensor_GV_Adapter extends BaseAdapter {

    Context context;
    ArrayList<String> nombreSensor;
    ArrayList<Integer> imageSensor;

    LayoutInflater inflater;

    public ItemSensor_GV_Adapter(Context context, ArrayList<String> nombreSensor, ArrayList<Integer> imageSensor) {
        this.context = context;
        this.nombreSensor = nombreSensor;
        this.imageSensor = imageSensor;
    }

    @Override
    public int getCount() {
        return nombreSensor.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("ViewHolder")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        /**
        if (inflater == null) {
            inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.gv_item_sensorsmartphone, null);
        }
         **/

        convertView = LayoutInflater.from(parent.getContext()).inflate(
                R.layout.gv_item_sensorsmartphone, parent, false);

        ImageView imagen_sensor = convertView.findViewById(R.id.gv_item_sensor_iv);
        TextView nombre_sensor = convertView.findViewById(R.id.gv_item_sensor_tv);

        imagen_sensor.setImageResource(imageSensor.get(position));
        nombre_sensor.setText(nombreSensor.get(position));

        return convertView;
    }
}

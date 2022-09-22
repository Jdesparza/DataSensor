package com.example.datasensor.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.provider.Settings;
import android.renderscript.ScriptGroup;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.datasensor.MainActivity;
import com.example.datasensor.R;
import com.example.datasensor.adapters.ItemSensor_GV_Adapter;

import java.util.ArrayList;

public class SensoresFragment extends Fragment {

    private SharedPreferences sharedPreferences;
    private Context context;

    // Fragment Sensores
    //private SensorTermometroFragment sensorTermometroFragment = new SensorTermometroFragment();
    //private SensorProximidadFragment sensorProximidadFragment = new SensorProximidadFragment();
    //private SensorLuzFragment sensorLuzFragment = new SensorLuzFragment();
    //private SensorAcelerometroFragment sensorAcelerometroFragment = new SensorAcelerometroFragment();
    //private SensorGiroscopioFragment sensorGiroscopioFragment = new SensorGiroscopioFragment();

    // Sensores Smartphone Grid View
    private GridView gv_sensoresSmartphone;

    private ArrayList<String> sensor_nombre = new ArrayList<String>();
    private ArrayList<Integer> sensor_imagen = new ArrayList<Integer>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sensores, container, false);

        // Toolbar
        ((MainActivity) getActivity()).LogoToolbarMostrar();
        ((MainActivity) getActivity()).BtnRegresarOcultar();
        ((MainActivity) getActivity()).TitleToolbar("Sensores");

        sharedPreferences = getActivity().getSharedPreferences("ArchivoInfoApp_v1", getContext().MODE_PRIVATE);

        gv_sensoresSmartphone = view.findViewById(R.id.gv_sensoresSmartphone);

        SensoresGrid();

        return  view;
    }

    private void SensoresGrid() {
        sensor_nombre.clear();
        sensor_imagen.clear();

        if (sharedPreferences.getBoolean("band_sensorTermometro", false)) {
            sensor_nombre.add("Termómetro");
            sensor_imagen.add(R.drawable.ic_termometro);
        }
        if (sharedPreferences.getBoolean("band_sensorDeProximidad", false)) {
            sensor_nombre.add("Proximidad");
            sensor_imagen.add(R.drawable.ic_proximidad);
        }
        if (sharedPreferences.getBoolean("band_sensorDeLuz", false)) {
            sensor_nombre.add("Luz");
            sensor_imagen.add(R.drawable.ic_luz);
        }
        if (sharedPreferences.getBoolean("band_sensorDeAcelerometro", false)) {
            sensor_nombre.add("Acelerómetro");
            sensor_imagen.add(R.drawable.ic_acelerometro);
        }
        if (sharedPreferences.getBoolean("band_sensorDeGiroscopio", false)) {
            sensor_nombre.add("Giroscopio");
            sensor_imagen.add(R.drawable.ic_giroscopio);
        }
        if (sharedPreferences.getBoolean("band_sensorMagnetometro", false)) {
            sensor_nombre.add("Magnetómetro");
            sensor_imagen.add(R.drawable.ic_magnetometro);
        }
        if (sharedPreferences.getBoolean("band_sensorPodometro", false)) {
            sensor_nombre.add("Podómetro");
            sensor_imagen.add(R.drawable.ic_podometro);
        }
        if (sharedPreferences.getBoolean("band_sensorCamara", false)) {
            sensor_nombre.add("Cámara");
            sensor_imagen.add(R.drawable.ic_camara);
        }
        if (sharedPreferences.getBoolean("band_sensorBarometro", false)) {
            sensor_nombre.add("Barómetro");
            sensor_imagen.add(R.drawable.ic_barometro);
        }
        if (sharedPreferences.getBoolean("band_sensorGPS", false)) {
            sensor_nombre.add("GPS");
            sensor_imagen.add(R.drawable.ic_gps);
        }
        if (sharedPreferences.getBoolean("band_sensorMicrofono", false)) {
            sensor_nombre.add("Micrófono");
            sensor_imagen.add(R.drawable.ic_microfono);
        }
        if (sharedPreferences.getBoolean("band_sensorInfrarrojo", false)) {
            sensor_nombre.add("Infrarrojo");
            sensor_imagen.add(R.drawable.ic_infrarrojo);
        }

        ItemSensor_GV_Adapter itemSensor_gv_adapter = new ItemSensor_GV_Adapter(context, sensor_nombre, sensor_imagen);
        gv_sensoresSmartphone.setAdapter(itemSensor_gv_adapter);

        gv_sensoresSmartphone.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getContext(), sensor_nombre.get(position), Toast.LENGTH_SHORT).show();
                if (sensor_nombre.get(position) == "Termómetro") {
                    ((MainActivity) getActivity()).replaceFragment(new SensorTermometroFragment());
                } else if (sensor_nombre.get(position) == "Proximidad") {
                    ((MainActivity) getActivity()).replaceFragment(new SensorProximidadFragment());
                } else if (sensor_nombre.get(position) == "Luz") {
                    ((MainActivity) getActivity()).replaceFragment(new SensorLuzFragment());
                } else if (sensor_nombre.get(position) == "Acelerómetro") {
                    ((MainActivity) getActivity()).replaceFragment(new SensorAcelerometroFragment());
                } else if (sensor_nombre.get(position) == "Giroscopio") {
                    ((MainActivity) getActivity()).replaceFragment(new SensorGiroscopioFragment());
                } else if (sensor_nombre.get(position) == "Magnetómetro") {
                    ((MainActivity) getActivity()).replaceFragment(new SensorMagnetometroFragment());
                } else if (sensor_nombre.get(position) == "Podómetro") {
                    if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) &&
                            (ContextCompat.checkSelfPermission(context, Manifest.permission.ACTIVITY_RECOGNITION)
                                    != PackageManager.PERMISSION_GRANTED)) {
                        Log.e("No", "No hay permiso");
                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                                1);
                    } else {
                        ((MainActivity) getActivity()).replaceFragment(new SensorPodometroFragment());
                    }
                } else if (sensor_nombre.get(position) == "Cámara") {
                    if ((ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                            != PackageManager.PERMISSION_GRANTED)) {
                        Log.e("No", "No hay permiso");

                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.CAMERA},
                                1);
                    } else {
                        ((MainActivity) getActivity()).replaceFragment(new SensorCamaraFragment());
                    }
                } else if (sensor_nombre.get(position) == "Barómetro") {
                    ((MainActivity) getActivity()).replaceFragment(new SensorBarometroFragment());
                } else if (sensor_nombre.get(position).equals("GPS")) {
                    if ((ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                                    != PackageManager.PERMISSION_GRANTED)) {
                        Log.e("No", "No hay permiso");

                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                1);
                    } else {
                        ((MainActivity) getActivity()).replaceFragment(new SensorGPSFragment());
                    }
                } else if (sensor_nombre.get(position).equals("Micrófono")) {
                    if ((ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                            != PackageManager.PERMISSION_GRANTED)) {
                        Log.e("No", "No hay permiso");

                        ActivityCompat.requestPermissions(getActivity(),
                                new String[]{Manifest.permission.RECORD_AUDIO},
                                1);
                    } else {
                        ((MainActivity) getActivity()).replaceFragment(new SensorMicrofonoFragment());
                    }
                } else if (sensor_nombre.get(position) == "Infrarrojo") {
                    ((MainActivity) getActivity()).replaceFragment(new SensorInfrarrojoFragment());
                } else {
                    Toast.makeText(getContext(), sensor_nombre.get(position), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
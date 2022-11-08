package com.example.datasensor.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.example.datasensor.MainActivity;
import com.example.datasensor.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SensorGiroscopioFragment extends Fragment implements SensorEventListener {

    private Button btn_resultados_giroscopio;
    private ResultadosFragment resultadosFragment = new ResultadosFragment();

    private CheckBox cb_giroscopio_version, cb_giroscopio_max,
            cb_giroscopio_fabricante, cb_giroscopio_potencia, cb_giroscopio_resolucion,
            cb_giroscopio_actual;

    private CheckedTextView ctv_giroscopio_calculo_1, ctv_giroscopio_calculo_2;

    // Sensor
    private SensorManager sensorManager;
    private Sensor sensorGiroscopio;
    private Float[] rotacionEncontrada = new Float[3];
    private int contEvent = 0;

    private Context context;
    private String dispositivoConInternet;

    // Archivo App
    private SharedPreferences sharedPreferences;
    private String idSmartphone;

    // Dialog
    AlertDialog dialogGD;

    // Firebase
    private FirebaseFirestore db;
    private DocumentReference documentReference;
    private Map<String, Serializable> doc = new HashMap<String, Serializable>();
    private Map<String, Serializable> docIsRegister = new HashMap<String, Serializable>();
    private String sensorDB;
    private boolean isModificado = false;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sensor_giroscopio, container, false);

        // Toolbar
        ((MainActivity) getActivity()).BtnRegresarMostrar();
        ((MainActivity) getActivity()).LogoToolbarOcultar();
        ((MainActivity) getActivity()).TitleToolbar("Sensor Giroscopio");

        //Sensor Title DB
        sensorDB = "sensorGiroscopio";

        // check Box acelerometro
        cb_giroscopio_version = view.findViewById(R.id.cb_giroscopio_version);
        cb_giroscopio_max = view.findViewById(R.id.cb_giroscopio_max);
        cb_giroscopio_fabricante = view.findViewById(R.id.cb_giroscopio_fabricante);
        cb_giroscopio_potencia = view.findViewById(R.id.cb_giroscopio_potencia);
        cb_giroscopio_resolucion = view.findViewById(R.id.cb_giroscopio_resolucion);
        cb_giroscopio_actual = view.findViewById(R.id.cb_giroscopio_actual);
        ctv_giroscopio_calculo_1 = view.findViewById(R.id.ctv_giroscopio_calculo_1);
        ctv_giroscopio_calculo_2 = view.findViewById(R.id.ctv_giroscopio_calculo_2);

        // Sensor
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sensorGiroscopio = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        sensorManager.registerListener(SensorGiroscopioFragment.this, sensorGiroscopio, SensorManager.SENSOR_DELAY_NORMAL);

        // boton Registrar
        btn_resultados_giroscopio = view.findViewById(R.id.btn_resultados_giroscopio);

        // Habilitar Btn Resultados
        final Handler handler= new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                HabilitarDesabilitarBotonResult();
                handler.postDelayed(this,500);
            }
        },500);

        btn_resultados_giroscopio.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (cb_giroscopio_actual.isChecked() && (ctv_giroscopio_calculo_1.isChecked() || ctv_giroscopio_calculo_2.isChecked())) {
                    contEvent = 0;
                    sensorManager.registerListener(SensorGiroscopioFragment.this, sensorGiroscopio, SensorManager.SENSOR_DELAY_NORMAL);

                    GuardarDatos();
                } else {
                    GuardarDatos();
                }
            }
        });

        return view;
    }

    private void GuardarDatos() {
        //Conexión
        dispositivoConInternet = "EnEspera";

        //Traer IDSmartphone
        sharedPreferences = context.getSharedPreferences("ArchivoInfoApp_v1", context.MODE_PRIVATE);
        idSmartphone = sharedPreferences.getString("IdSmartphone", "No hay modelo");

        // Firebase
        db = FirebaseFirestore.getInstance();
        documentReference = db.collection("SensoresSmartphones").document(idSmartphone);

        DialogGuardarDatos();
    }

    private void HabilitarDesabilitarBotonResult() {

        if (cb_giroscopio_actual.isChecked() && !ctv_giroscopio_calculo_1.isEnabled() && !ctv_giroscopio_calculo_2.isEnabled()) {
            ctv_giroscopio_calculo_1.setEnabled(true);
            ctv_giroscopio_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            ctv_giroscopio_calculo_1.setTextColor(ContextCompat.getColor(context, R.color.black));
            ctv_giroscopio_calculo_2.setEnabled(true);
            ctv_giroscopio_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            ctv_giroscopio_calculo_2.setTextColor(ContextCompat.getColor(context, R.color.black));
        }
        else if (!cb_giroscopio_actual.isChecked() && ctv_giroscopio_calculo_1.isEnabled() && ctv_giroscopio_calculo_2.isEnabled()) {
            ctv_giroscopio_calculo_1.setEnabled(false);
            ctv_giroscopio_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.gris_claro));
            ctv_giroscopio_calculo_1.setTextColor(ContextCompat.getColor(context, R.color.gris_oscuro));
            if (ctv_giroscopio_calculo_1.isChecked()) ctv_giroscopio_calculo_1.setChecked(false);
            ctv_giroscopio_calculo_2.setEnabled(false);
            ctv_giroscopio_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.gris_claro));
            ctv_giroscopio_calculo_2.setTextColor(ContextCompat.getColor(context, R.color.gris_oscuro));
            if (ctv_giroscopio_calculo_2.isChecked()) ctv_giroscopio_calculo_2.setChecked(false);
        }

        if (
                (cb_giroscopio_fabricante.isChecked() || cb_giroscopio_max.isChecked() ||
                        cb_giroscopio_potencia.isChecked() || cb_giroscopio_resolucion.isChecked() ||
                        cb_giroscopio_version.isChecked()) &&
                        (!cb_giroscopio_actual.isChecked() && !btn_resultados_giroscopio.isEnabled())
        ) {
            BotonHabilitado();
        }
        else if (
                !cb_giroscopio_fabricante.isChecked() && !cb_giroscopio_max.isChecked() &&
                        !cb_giroscopio_potencia.isChecked() && !cb_giroscopio_resolucion.isChecked() &&
                        !cb_giroscopio_version.isChecked() && !cb_giroscopio_actual.isChecked() && btn_resultados_giroscopio.isEnabled()
        ) {
            BotonDeshabilitado();
        }

        ctv_giroscopio_calculo_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ctv_giroscopio_calculo_1.isChecked() ) {
                    ctv_giroscopio_calculo_1.setChecked(false);
                    ctv_giroscopio_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                } else {
                    ctv_giroscopio_calculo_1.setChecked(true);
                    ctv_giroscopio_calculo_1.setBackgroundColor(Color.parseColor("#A8EA8C"));
                    if (ctv_giroscopio_calculo_2.isChecked()) {
                        ctv_giroscopio_calculo_2.setChecked(false);
                        ctv_giroscopio_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    }
                }
            }
        });

        ctv_giroscopio_calculo_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ctv_giroscopio_calculo_2.isChecked() ) {
                    ctv_giroscopio_calculo_2.setChecked(false);
                    ctv_giroscopio_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                } else {
                    ctv_giroscopio_calculo_2.setChecked(true);
                    ctv_giroscopio_calculo_2.setBackgroundColor(Color.parseColor("#A8EA8C"));
                    if (ctv_giroscopio_calculo_1.isChecked()) {
                        ctv_giroscopio_calculo_1.setChecked(false);
                        ctv_giroscopio_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    }
                }
            }
        });

        if (cb_giroscopio_actual.isChecked() && (ctv_giroscopio_calculo_1.isChecked() || ctv_giroscopio_calculo_2.isChecked()) &&
                !btn_resultados_giroscopio.isEnabled()) {
            BotonHabilitado();
        } else if (cb_giroscopio_actual.isChecked() && !ctv_giroscopio_calculo_1.isChecked() && !ctv_giroscopio_calculo_2.isChecked() &&
                btn_resultados_giroscopio.isEnabled()) {
            BotonDeshabilitado();
        }

    }
    private void BotonHabilitado() {
        btn_resultados_giroscopio.setTextColor(ContextCompat.getColor(context, R.color.black));
        btn_resultados_giroscopio.setBackgroundColor(ContextCompat.getColor(context, R.color.celeste));
        btn_resultados_giroscopio.setEnabled(true);
    }
    private void BotonDeshabilitado() {
        btn_resultados_giroscopio.setTextColor(ContextCompat.getColor(context, R.color.gris_oscuro));
        btn_resultados_giroscopio.setBackgroundColor(ContextCompat.getColor(context, R.color.gris_claro));
        btn_resultados_giroscopio.setEnabled(false);
    }

    private void DialogGuardarDatos() {
        AlertDialog.Builder builderGD = new AlertDialog.Builder(context);

        LayoutInflater inflaterGD= getActivity().getLayoutInflater();

        View viewGD = inflaterGD.inflate(R.layout.dialog_guardar_datos_sensores, null);

        builderGD.setView(viewGD);
        builderGD.setCancelable(false);

        dialogGD = builderGD.create();
        if (dialogGD.getWindow() != null) {
            dialogGD.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        dialogGD.show();

        TextView tv_dialog_subtitle_GD = viewGD.findViewById(R.id.tv_dialog_subtitle_GD);

        IsOnlineTask isOnlineTask = new IsOnlineTask();
        isOnlineTask.execute();

        final Handler handler= new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!dispositivoConInternet.equals("EnEspera")) {
                    if (dispositivoConInternet.equals("ConInternet")) {
                        tv_dialog_subtitle_GD.setVisibility(View.INVISIBLE);

                        ValidarCheckBoxDatos();
                        IsRegisterDB();

                    } else if (dispositivoConInternet.equals("SinInternet")) {
                        tv_dialog_subtitle_GD.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                sensorManager.unregisterListener(SensorGiroscopioFragment.this);
                                dialogGD.dismiss();
                                ((MainActivity) getActivity()).mostrarDialogConexionRed();
                            }
                        }, 600);
                    }
                } else {
                    handler.postDelayed(this,1000);
                }
            }
        },1500);
    }

    private void ValidarCheckBoxDatos() {
        Log.i("Sensor", sensorGiroscopio.getName());
        doc.put("nombre", sensorGiroscopio.getName());
        if (cb_giroscopio_fabricante.isChecked()) {
            doc.put("fabricante", sensorGiroscopio.getVendor());
            Log.e("Fabricante", String.valueOf(sensorGiroscopio.getVendor()));
        }
        if (cb_giroscopio_version.isChecked()) {
            doc.put("version", sensorGiroscopio.getVersion());
            Log.e("Versión", String.valueOf(sensorGiroscopio.getVersion()));
        }
        if (cb_giroscopio_potencia.isChecked()) {
            doc.put("potencia", sensorGiroscopio.getPower());
            Log.e("Potencia", sensorGiroscopio.getPower() + " mA");
        }
        if (cb_giroscopio_resolucion.isChecked()) {
            doc.put("resolucion", sensorGiroscopio.getResolution());
            Log.e("Resolución", sensorGiroscopio.getResolution() + " rad/s");
        }
        if (cb_giroscopio_max.isChecked()) {
            doc.put("rangoMax", sensorGiroscopio.getMaximumRange());
            Log.e("Máx", sensorGiroscopio.getMaximumRange() + " rad/s");
        }
        if (ctv_giroscopio_calculo_1.isChecked()) {
            HashMap<String, Serializable> rotacion_1 = new HashMap<String, Serializable>();
            rotacion_1.put("x", rotacionEncontrada[0]);
            rotacion_1.put("y", rotacionEncontrada[1]);
            rotacion_1.put("z", rotacionEncontrada[2]);
            doc.put("rotacion_1", rotacion_1);
        }
        if (ctv_giroscopio_calculo_2.isChecked()) {
            HashMap<String, Serializable> rotacion_2 = new HashMap<String, Serializable>();
            rotacion_2.put("x", rotacionEncontrada[0]);
            rotacion_2.put("y", rotacionEncontrada[1]);
            rotacion_2.put("z", rotacionEncontrada[2]);
            doc.put("rotacion_2", rotacion_2);
        }
    }

    private void IsRegisterDB() {
        documentReference.get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            docIsRegister = (HashMap<String, Serializable>) document.getData().get(sensorDB);
                            UpdateDB();
                        } else {
                            Log.e("ProblemaBD", "Error al buscar documento", task.getException());
                        }
                    }
                });
    }

    private void UpdateDB() {
        sensorManager.unregisterListener(SensorGiroscopioFragment.this);

        Log.e("DOCResultsAEnviar1", String.valueOf(docIsRegister));
        for (Map.Entry entry : docIsRegister.entrySet()) {
            if ((doc.containsKey(entry.getKey().toString())) &&
                    (!entry.getKey().toString().equals("rotacion_1") && !entry.getKey().toString().equals("rotacion_2")) &&
                    (!Objects.equals(docIsRegister.get(entry.getKey().toString()).toString(), doc.get(entry.getKey().toString()).toString()))) {
                isModificado = true;
                docIsRegister.put(entry.getKey().toString(), doc.get(entry.getKey().toString()));
                doc.remove(entry.getKey().toString());
            }
            else if ((doc.containsKey(entry.getKey().toString())) &&
                    (!entry.getKey().toString().equals("rotacion_1") && !entry.getKey().toString().equals("rotacion_2")) &&
                    (Objects.equals(docIsRegister.get(entry.getKey().toString()).toString(), doc.get(entry.getKey().toString()).toString()))) {

                doc.remove(entry.getKey().toString());
            } else if ((doc.containsKey(entry.getKey().toString()) &&
                    (entry.getKey().toString().equals("rotacion_1") || entry.getKey().toString().equals("rotacion_2")))
            ) {
                isModificado = true;
                docIsRegister.put(entry.getKey().toString(), doc.get(entry.getKey().toString()));
                doc.remove(entry.getKey().toString());
            }
        }
        if (doc.size() > 0) {
            for (Map.Entry entry : doc.entrySet()) {
                isModificado = true;
                docIsRegister.put(entry.getKey().toString(), doc.get(entry.getKey().toString()));
            }
        }

        Log.e("DOCResultsAEnviar2", String.valueOf(docIsRegister));

        if (isModificado) {
            documentReference.update(sensorDB, docIsRegister)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void unused) {
                            dialogGD.dismiss();
                            ((MainActivity) getActivity()).replaceFragmentResultados(sensorDB, docIsRegister);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Datos", "Error", e);
                        }
                    });
        } else {
            dialogGD.dismiss();
            ((MainActivity) getActivity()).replaceFragmentResultados(sensorDB, docIsRegister);
        }
    }

    private boolean IsOnline(Context context) {
        Boolean bandIsOnline;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if ((networkInfo != null) && (networkInfo.isAvailable()) && (networkInfo.isConnected())) {
            dispositivoConInternet = "ConInternet";
            bandIsOnline = true;
        } else {
            dispositivoConInternet = "SinInternet";
            bandIsOnline = false;
        }

        return bandIsOnline;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (contEvent == 0) {
            contEvent++;
            rotacionEncontrada[0] = event.values[0];
            rotacionEncontrada[1] = event.values[1];
            rotacionEncontrada[2] = event.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    private class IsOnlineTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            IsOnline(context);

            return null;
        }
    }
}
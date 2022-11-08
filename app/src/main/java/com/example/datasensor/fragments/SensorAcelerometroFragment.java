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

public class SensorAcelerometroFragment extends Fragment implements SensorEventListener {

    private Button btn_resultados_acelerometro;
    private ResultadosFragment resultadosFragment = new ResultadosFragment();

    private CheckBox cb_acelerometro_version, cb_acelerometro_max,
            cb_acelerometro_fabricante, cb_acelerometro_potencia, cb_acelerometro_resolucion,
            cb_acelerometro_actual;

    private CheckedTextView ctv_acelerometro_calculo_1, ctv_acelerometro_calculo_2;

    // Sensor
    private SensorManager sensorManager;
    private Sensor sensorAcelerometro = null;
    private Float[] aceleracionEncontrada = new Float[3];
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
        View view = inflater.inflate(R.layout.fragment_sensor_acelerometro, container, false);

        // Toolbar
        ((MainActivity) getActivity()).BtnRegresarMostrar();
        ((MainActivity) getActivity()).LogoToolbarOcultar();
        ((MainActivity) getActivity()).TitleToolbar("Sensor Acelerómetro");

        //Sensor Title DB
        sensorDB = "sensorAcelerometro";

        // check Box acelerometro
        cb_acelerometro_version = view.findViewById(R.id.cb_acelerometro_version);
        cb_acelerometro_max = view.findViewById(R.id.cb_acelerometro_max);
        cb_acelerometro_fabricante = view.findViewById(R.id.cb_acelerometro_fabricante);
        cb_acelerometro_potencia = view.findViewById(R.id.cb_acelerometro_potencia);
        cb_acelerometro_resolucion = view.findViewById(R.id.cb_acelerometro_resolucion);
        cb_acelerometro_actual = view.findViewById(R.id.cb_acelerometro_actual);
        ctv_acelerometro_calculo_1 = view.findViewById(R.id.ctv_acelerometro_calculo_1);
        ctv_acelerometro_calculo_2 = view.findViewById(R.id.ctv_acelerometro_calculo_2);

        // Sensor
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sensorAcelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // boton Registrar
        btn_resultados_acelerometro = view.findViewById(R.id.btn_resultados_acelerometro);

        // Habilitar Btn Resultados
        final Handler handler= new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                HabilitarDesabilitarBotonResult();
                handler.postDelayed(this,500);
            }
        },500);

        btn_resultados_acelerometro.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (cb_acelerometro_actual.isChecked() && (ctv_acelerometro_calculo_1.isChecked() || ctv_acelerometro_calculo_2.isChecked())) {
                    contEvent = 0;
                    sensorManager.registerListener(SensorAcelerometroFragment.this, sensorAcelerometro, SensorManager.SENSOR_DELAY_NORMAL);

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

        if (cb_acelerometro_actual.isChecked() && !ctv_acelerometro_calculo_1.isEnabled() && !ctv_acelerometro_calculo_2.isEnabled()) {
            ctv_acelerometro_calculo_1.setEnabled(true);
            ctv_acelerometro_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            ctv_acelerometro_calculo_1.setTextColor(ContextCompat.getColor(context, R.color.black));
            ctv_acelerometro_calculo_2.setEnabled(true);
            ctv_acelerometro_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            ctv_acelerometro_calculo_2.setTextColor(ContextCompat.getColor(context, R.color.black));
        }
        else if (!cb_acelerometro_actual.isChecked() && ctv_acelerometro_calculo_1.isEnabled() && ctv_acelerometro_calculo_2.isEnabled()) {
            ctv_acelerometro_calculo_1.setEnabled(false);
            ctv_acelerometro_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.gris_claro));
            ctv_acelerometro_calculo_1.setTextColor(ContextCompat.getColor(context, R.color.gris_oscuro));
            if (ctv_acelerometro_calculo_1.isChecked()) ctv_acelerometro_calculo_1.setChecked(false);
            ctv_acelerometro_calculo_2.setEnabled(false);
            ctv_acelerometro_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.gris_claro));
            ctv_acelerometro_calculo_2.setTextColor(ContextCompat.getColor(context, R.color.gris_oscuro));
            if (ctv_acelerometro_calculo_2.isChecked()) ctv_acelerometro_calculo_2.setChecked(false);
        }

        if (
                (cb_acelerometro_fabricante.isChecked() || cb_acelerometro_max.isChecked() ||
                        cb_acelerometro_potencia.isChecked() || cb_acelerometro_resolucion.isChecked() ||
                        cb_acelerometro_version.isChecked()) &&
                        (!cb_acelerometro_actual.isChecked() && !btn_resultados_acelerometro.isEnabled())
        ) {
            BotonHabilitado();
        }
        else if (
                !cb_acelerometro_fabricante.isChecked() && !cb_acelerometro_max.isChecked() &&
                        !cb_acelerometro_potencia.isChecked() && !cb_acelerometro_resolucion.isChecked() &&
                        !cb_acelerometro_version.isChecked() && !cb_acelerometro_actual.isChecked() && btn_resultados_acelerometro.isEnabled()
        ) {
            BotonDeshabilitado();
        }

        ctv_acelerometro_calculo_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ctv_acelerometro_calculo_1.isChecked() ) {
                    ctv_acelerometro_calculo_1.setChecked(false);
                    ctv_acelerometro_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                } else {
                    ctv_acelerometro_calculo_1.setChecked(true);
                    ctv_acelerometro_calculo_1.setBackgroundColor(Color.parseColor("#A8EA8C"));
                    if (ctv_acelerometro_calculo_2.isChecked()) {
                        ctv_acelerometro_calculo_2.setChecked(false);
                        ctv_acelerometro_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    }
                }
            }
        });

        ctv_acelerometro_calculo_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ctv_acelerometro_calculo_2.isChecked() ) {
                    ctv_acelerometro_calculo_2.setChecked(false);
                    ctv_acelerometro_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                } else {
                    ctv_acelerometro_calculo_2.setChecked(true);
                    ctv_acelerometro_calculo_2.setBackgroundColor(Color.parseColor("#A8EA8C"));
                    if (ctv_acelerometro_calculo_1.isChecked()) {
                        ctv_acelerometro_calculo_1.setChecked(false);
                        ctv_acelerometro_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    }
                }
            }
        });

        if (cb_acelerometro_actual.isChecked() && (ctv_acelerometro_calculo_1.isChecked() || ctv_acelerometro_calculo_2.isChecked()) &&
                !btn_resultados_acelerometro.isEnabled()) {
            BotonHabilitado();
        } else if (cb_acelerometro_actual.isChecked() && !ctv_acelerometro_calculo_1.isChecked() && !ctv_acelerometro_calculo_2.isChecked() &&
                btn_resultados_acelerometro.isEnabled()) {
            BotonDeshabilitado();
        }

    }
    private void BotonHabilitado() {
        btn_resultados_acelerometro.setTextColor(ContextCompat.getColor(context, R.color.black));
        btn_resultados_acelerometro.setBackgroundColor(ContextCompat.getColor(context, R.color.celeste));
        btn_resultados_acelerometro.setEnabled(true);
    }
    private void BotonDeshabilitado() {
        btn_resultados_acelerometro.setTextColor(ContextCompat.getColor(context, R.color.gris_oscuro));
        btn_resultados_acelerometro.setBackgroundColor(ContextCompat.getColor(context, R.color.gris_claro));
        btn_resultados_acelerometro.setEnabled(false);
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
                                sensorManager.unregisterListener(SensorAcelerometroFragment.this);
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
        Log.i("Sensor", sensorAcelerometro.getName());
        doc.put("nombre", sensorAcelerometro.getName());
        if (cb_acelerometro_fabricante.isChecked()) {
            doc.put("fabricante", sensorAcelerometro.getVendor());
            Log.e("Fabricante", String.valueOf(sensorAcelerometro.getVendor()));
        }
        if (cb_acelerometro_version.isChecked()) {
            doc.put("version", sensorAcelerometro.getVersion());
            Log.e("Versión", String.valueOf(sensorAcelerometro.getVersion()));
        }
        if (cb_acelerometro_potencia.isChecked()) {
            doc.put("potencia", sensorAcelerometro.getPower());
            Log.e("Potencia", sensorAcelerometro.getPower() + " mA");
        }
        if (cb_acelerometro_resolucion.isChecked()) {
            doc.put("resolucion", sensorAcelerometro.getResolution());
            Log.e("Resolución", sensorAcelerometro.getResolution() + " m/s2");
        }
        if (cb_acelerometro_max.isChecked()) {
            doc.put("rangoMax", sensorAcelerometro.getMaximumRange());
            Log.e("Máx", sensorAcelerometro.getMaximumRange() + " m/s2");
        }
        if (ctv_acelerometro_calculo_1.isChecked()) {
            HashMap<String, Serializable> aceleracion_1 = new HashMap<String, Serializable>();
            aceleracion_1.put("x", aceleracionEncontrada[0]);
            aceleracion_1.put("y", aceleracionEncontrada[1]);
            aceleracion_1.put("z", aceleracionEncontrada[2]);
            doc.put("aceleracion_1", aceleracion_1);
        }
        if (ctv_acelerometro_calculo_2.isChecked()) {
            HashMap<String, Serializable> aceleracion_2 = new HashMap<String, Serializable>();
            aceleracion_2.put("x", aceleracionEncontrada[0]);
            aceleracion_2.put("y", aceleracionEncontrada[1]);
            aceleracion_2.put("z", aceleracionEncontrada[2]);
            doc.put("aceleracion_2", aceleracion_2);
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
        sensorManager.unregisterListener(SensorAcelerometroFragment.this);

        Log.e("DOCResultsAEnviar1", String.valueOf(docIsRegister));
        for (Map.Entry entry : docIsRegister.entrySet()) {
            if ((doc.containsKey(entry.getKey().toString())) &&
                    (!entry.getKey().toString().equals("aceleracion_1") && !entry.getKey().toString().equals("aceleracion_2")) &&
                    (!Objects.equals(docIsRegister.get(entry.getKey().toString()).toString(), doc.get(entry.getKey().toString()).toString()))) {
                isModificado = true;
                docIsRegister.put(entry.getKey().toString(), doc.get(entry.getKey().toString()));
                doc.remove(entry.getKey().toString());
            }
            else if ((doc.containsKey(entry.getKey().toString())) &&
                    (!entry.getKey().toString().equals("aceleracion_1") && !entry.getKey().toString().equals("aceleracion_2")) &&
                    (Objects.equals(docIsRegister.get(entry.getKey().toString()).toString(), doc.get(entry.getKey().toString()).toString()))) {
                doc.remove(entry.getKey().toString());
            } else if ((doc.containsKey(entry.getKey().toString()) &&
                    (entry.getKey().toString().equals("aceleracion_1") || entry.getKey().toString().equals("aceleracion_2")))
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
            aceleracionEncontrada[0] = event.values[0];
            aceleracionEncontrada[1] = event.values[1];
            aceleracionEncontrada[2] = event.values[2];
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
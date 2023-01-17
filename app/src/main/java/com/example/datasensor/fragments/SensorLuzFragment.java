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
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SensorLuzFragment extends Fragment implements SensorEventListener {

    private Button btn_resultados_luz;
    private ResultadosFragment resultadosFragment = new ResultadosFragment();

    private CheckBox cb_luz_version, cb_luz_max,
            cb_luz_fabricante, cb_luz_potencia, cb_luz_resolucion,
            cb_luz_actual;

    private CheckedTextView ctv_luz_calculo_1, ctv_luz_calculo_2;

    // Sensor
    private SensorManager sensorManager;
    private Sensor sensorLuz;
    private Float iluminacionEncontrada;

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
        View view = inflater.inflate(R.layout.fragment_sensor_luz, container, false);

        // Toolbar
        ((MainActivity) getActivity()).BtnRegresarMostrar();
        ((MainActivity) getActivity()).LogoToolbarOcultar();
        ((MainActivity) getActivity()).TitleToolbar("Sensor Luz");

        //Sensor Title DB
        sensorDB = "sensorLuz";

        // check Box Luz
        cb_luz_version = view.findViewById(R.id.cb_luz_version);
        cb_luz_max = view.findViewById(R.id.cb_luz_max);
        cb_luz_fabricante = view.findViewById(R.id.cb_luz_fabricante);
        cb_luz_potencia = view.findViewById(R.id.cb_luz_potencia);
        cb_luz_resolucion = view.findViewById(R.id.cb_luz_resolucion);
        cb_luz_actual = view.findViewById(R.id.cb_luz_actual);
        ctv_luz_calculo_1 = view.findViewById(R.id.ctv_luz_calculo_1);
        ctv_luz_calculo_2 = view.findViewById(R.id.ctv_luz_calculo_2);

        // Sensor
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sensorLuz = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        // boton Registrar
        btn_resultados_luz = view.findViewById(R.id.btn_resultados_luz);

        // Habilitar Btn Resultados
        final Handler handler= new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                HabilitarDesabilitarBotonResult();
                handler.postDelayed(this,500);
            }
        },500);

        btn_resultados_luz.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (cb_luz_actual.isChecked() && (ctv_luz_calculo_1.isChecked() || ctv_luz_calculo_2.isChecked())) {
                    sensorManager.registerListener(SensorLuzFragment.this, sensorLuz, SensorManager.SENSOR_DELAY_NORMAL);

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

        if (cb_luz_actual.isChecked() && !ctv_luz_calculo_1.isEnabled() && !ctv_luz_calculo_2.isEnabled()) {
            ctv_luz_calculo_1.setEnabled(true);
            ctv_luz_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            ctv_luz_calculo_1.setTextColor(ContextCompat.getColor(context, R.color.black));
            ctv_luz_calculo_2.setEnabled(true);
            ctv_luz_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            ctv_luz_calculo_2.setTextColor(ContextCompat.getColor(context, R.color.black));
        }
        else if (!cb_luz_actual.isChecked() && ctv_luz_calculo_1.isEnabled() && ctv_luz_calculo_2.isEnabled()) {
            ctv_luz_calculo_1.setEnabled(false);
            ctv_luz_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.gris_claro));
            ctv_luz_calculo_1.setTextColor(ContextCompat.getColor(context, R.color.gris_oscuro));
            if (ctv_luz_calculo_1.isChecked()) ctv_luz_calculo_1.setChecked(false);
            ctv_luz_calculo_2.setEnabled(false);
            ctv_luz_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.gris_claro));
            ctv_luz_calculo_2.setTextColor(ContextCompat.getColor(context, R.color.gris_oscuro));
            if (ctv_luz_calculo_2.isChecked()) ctv_luz_calculo_2.setChecked(false);
        }

        if (
                (cb_luz_fabricante.isChecked() || cb_luz_max.isChecked() ||
                        cb_luz_potencia.isChecked() || cb_luz_resolucion.isChecked() ||
                        cb_luz_version.isChecked()) &&
                        (!cb_luz_actual.isChecked() && !btn_resultados_luz.isEnabled())
        ) {
            BotonHabilitado();
        }
        else if (
                !cb_luz_fabricante.isChecked() && !cb_luz_max.isChecked() &&
                        !cb_luz_potencia.isChecked() && !cb_luz_resolucion.isChecked() &&
                        !cb_luz_version.isChecked() && !cb_luz_actual.isChecked() && btn_resultados_luz.isEnabled()
        ) {
            BotonDeshabilitado();
        }

        ctv_luz_calculo_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ctv_luz_calculo_1.isChecked() ) {
                    ctv_luz_calculo_1.setChecked(false);
                    ctv_luz_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                } else {
                    ctv_luz_calculo_1.setChecked(true);
                    ctv_luz_calculo_1.setBackgroundColor(Color.parseColor("#A8EA8C"));
                    if (ctv_luz_calculo_2.isChecked()) {
                        ctv_luz_calculo_2.setChecked(false);
                        ctv_luz_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    }
                }
            }
        });

        ctv_luz_calculo_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ctv_luz_calculo_2.isChecked() ) {
                    ctv_luz_calculo_2.setChecked(false);
                    ctv_luz_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                } else {
                    ctv_luz_calculo_2.setChecked(true);
                    ctv_luz_calculo_2.setBackgroundColor(Color.parseColor("#A8EA8C"));
                    if (ctv_luz_calculo_1.isChecked()) {
                        ctv_luz_calculo_1.setChecked(false);
                        ctv_luz_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    }
                }
            }
        });

        if (cb_luz_actual.isChecked() && (ctv_luz_calculo_1.isChecked() || ctv_luz_calculo_2.isChecked()) &&
                !btn_resultados_luz.isEnabled()) {
            BotonHabilitado();
        } else if (cb_luz_actual.isChecked() && !ctv_luz_calculo_1.isChecked() && !ctv_luz_calculo_2.isChecked() &&
                btn_resultados_luz.isEnabled()) {
            BotonDeshabilitado();
        }

    }
    private void BotonHabilitado() {
        btn_resultados_luz.setTextColor(ContextCompat.getColor(context, R.color.black));
        btn_resultados_luz.setBackgroundColor(ContextCompat.getColor(context, R.color.celeste));
        btn_resultados_luz.setEnabled(true);
    }
    private void BotonDeshabilitado() {
        btn_resultados_luz.setTextColor(ContextCompat.getColor(context, R.color.gris_oscuro));
        btn_resultados_luz.setBackgroundColor(ContextCompat.getColor(context, R.color.gris_claro));
        btn_resultados_luz.setEnabled(false);
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
                                sensorManager.unregisterListener(SensorLuzFragment.this);
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
        doc.put("nombre", sensorLuz.getName());
        if (cb_luz_fabricante.isChecked()) {
            doc.put("fabricante", sensorLuz.getVendor());
        }
        if (cb_luz_version.isChecked()) {
            doc.put("version", sensorLuz.getVersion());
        }
        if (cb_luz_potencia.isChecked()) {
            doc.put("potencia", sensorLuz.getPower());
        }
        if (cb_luz_resolucion.isChecked()) {
            doc.put("resolucion", sensorLuz.getResolution());
        }
        if (cb_luz_max.isChecked()) {
            doc.put("rangoMax", sensorLuz.getMaximumRange());
        }
        if (ctv_luz_calculo_1.isChecked()) {
            doc.put("iluminacion_1", iluminacionEncontrada);
        }
        if (ctv_luz_calculo_2.isChecked()) {
            doc.put("iluminacion_2", iluminacionEncontrada);
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
        sensorManager.unregisterListener(SensorLuzFragment.this);

        for (Map.Entry entry : docIsRegister.entrySet()) {
            if ((doc.containsKey(entry.getKey().toString())) &&
                    (!Objects.equals(docIsRegister.get(entry.getKey().toString()).toString(), doc.get(entry.getKey().toString()).toString()))) {
                isModificado = true;
                docIsRegister.put(entry.getKey().toString(), doc.get(entry.getKey().toString()));
                doc.remove(entry.getKey().toString());
            }
            else if ((doc.containsKey(entry.getKey().toString())) &&
                    (Objects.equals(docIsRegister.get(entry.getKey().toString()).toString(), doc.get(entry.getKey().toString()).toString()))) {

                doc.remove(entry.getKey().toString());
            }
        }
        if (doc.size() > 0) {
            for (Map.Entry entry : doc.entrySet()) {
                isModificado = true;
                docIsRegister.put(entry.getKey().toString(), doc.get(entry.getKey().toString()));
            }
        }

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
        iluminacionEncontrada = event.values[0];
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
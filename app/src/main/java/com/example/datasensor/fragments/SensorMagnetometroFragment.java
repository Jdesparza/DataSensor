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

public class SensorMagnetometroFragment extends Fragment implements SensorEventListener {

    private Button btn_resultados_magnetometro;
    private ResultadosFragment resultadosFragment = new ResultadosFragment();

    private CheckBox cb_magnetometro_version, cb_magnetometro_max,
            cb_magnetometro_fabricante, cb_magnetometro_potencia, cb_magnetometro_resolucion,
            cb_magnetometro_actual;

    private CheckedTextView ctv_magnetometro_calculo_1, ctv_magnetometro_calculo_2;

    // Sensor
    private SensorManager sensorManager;
    private Sensor sensorMagnetometro;
    private Float[] magnetismoEncontrada = new Float[3];
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
    public void onPause() {
        super.onPause();
        //sensorManager.unregisterListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sensor_magnetometro, container, false);

        // Toolbar
        ((MainActivity) getActivity()).BtnRegresarMostrar();
        ((MainActivity) getActivity()).LogoToolbarOcultar();
        ((MainActivity) getActivity()).TitleToolbar("Sensor Magnetómetro");

        //Sensor Title DB
        sensorDB = "sensorMagnetometro";

        // check Box magnetometro
        cb_magnetometro_version = view.findViewById(R.id.cb_magnetometro_version);
        cb_magnetometro_max = view.findViewById(R.id.cb_magnetometro_max);
        cb_magnetometro_fabricante = view.findViewById(R.id.cb_magnetometro_fabricante);
        cb_magnetometro_potencia = view.findViewById(R.id.cb_magnetometro_potencia);
        cb_magnetometro_resolucion = view.findViewById(R.id.cb_magnetometro_resolucion);
        cb_magnetometro_actual = view.findViewById(R.id.cb_magnetometro_actual);
        ctv_magnetometro_calculo_1 = view.findViewById(R.id.ctv_magnetometro_calculo_1);
        ctv_magnetometro_calculo_2 = view.findViewById(R.id.ctv_magnetometro_calculo_2);

        // Sensor
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sensorMagnetometro = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // boton Registrar
        btn_resultados_magnetometro = view.findViewById(R.id.btn_resultados_magnetometro);

        // Habilitar Btn Resultados
        final Handler handler= new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                HabilitarDesabilitarBotonResult();
                handler.postDelayed(this,500);
            }
        },500);

        btn_resultados_magnetometro.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (cb_magnetometro_actual.isChecked() && (ctv_magnetometro_calculo_1.isChecked() || ctv_magnetometro_calculo_2.isChecked())) {
                    contEvent = 0;
                    sensorManager.registerListener(SensorMagnetometroFragment.this, sensorMagnetometro, SensorManager.SENSOR_DELAY_NORMAL);

                    GuardarDatos();
                } else {
                    Log.e("Click", "Sin Calculo");
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

        if (cb_magnetometro_actual.isChecked() && !ctv_magnetometro_calculo_1.isEnabled() && !ctv_magnetometro_calculo_2.isEnabled()) {
            Log.e("if", "1");
            ctv_magnetometro_calculo_1.setEnabled(true);
            ctv_magnetometro_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            ctv_magnetometro_calculo_1.setTextColor(ContextCompat.getColor(context, R.color.black));
            ctv_magnetometro_calculo_2.setEnabled(true);
            ctv_magnetometro_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            ctv_magnetometro_calculo_2.setTextColor(ContextCompat.getColor(context, R.color.black));
        }
        else if (!cb_magnetometro_actual.isChecked() && ctv_magnetometro_calculo_1.isEnabled() && ctv_magnetometro_calculo_2.isEnabled()) {
            Log.e("if", "1-1");
            ctv_magnetometro_calculo_1.setEnabled(false);
            ctv_magnetometro_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.gris_claro));
            ctv_magnetometro_calculo_1.setTextColor(ContextCompat.getColor(context, R.color.gris_oscuro));
            if (ctv_magnetometro_calculo_1.isChecked()) ctv_magnetometro_calculo_1.setChecked(false);
            ctv_magnetometro_calculo_2.setEnabled(false);
            ctv_magnetometro_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.gris_claro));
            ctv_magnetometro_calculo_2.setTextColor(ContextCompat.getColor(context, R.color.gris_oscuro));
            if (ctv_magnetometro_calculo_2.isChecked()) ctv_magnetometro_calculo_2.setChecked(false);
        }

        if (
                (cb_magnetometro_fabricante.isChecked() || cb_magnetometro_max.isChecked() ||
                        cb_magnetometro_potencia.isChecked() || cb_magnetometro_resolucion.isChecked() ||
                        cb_magnetometro_version.isChecked()) &&
                        (!cb_magnetometro_actual.isChecked() && !btn_resultados_magnetometro.isEnabled())
        ) {
            Log.e("if", "2");
            BotonHabilitado();
        }
        else if (
                !cb_magnetometro_fabricante.isChecked() && !cb_magnetometro_max.isChecked() &&
                        !cb_magnetometro_potencia.isChecked() && !cb_magnetometro_resolucion.isChecked() &&
                        !cb_magnetometro_version.isChecked() && !cb_magnetometro_actual.isChecked() && btn_resultados_magnetometro.isEnabled()
        ) {
            Log.e("if", "2-1");
            BotonDeshabilitado();
        }

        ctv_magnetometro_calculo_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ctv_magnetometro_calculo_1.isChecked() ) {
                    ctv_magnetometro_calculo_1.setChecked(false);
                    ctv_magnetometro_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                } else {
                    ctv_magnetometro_calculo_1.setChecked(true);
                    ctv_magnetometro_calculo_1.setBackgroundColor(Color.parseColor("#A8EA8C"));
                    if (ctv_magnetometro_calculo_2.isChecked()) {
                        ctv_magnetometro_calculo_2.setChecked(false);
                        ctv_magnetometro_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    }
                }
            }
        });

        ctv_magnetometro_calculo_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ctv_magnetometro_calculo_2.isChecked() ) {
                    ctv_magnetometro_calculo_2.setChecked(false);
                    ctv_magnetometro_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                } else {
                    ctv_magnetometro_calculo_2.setChecked(true);
                    ctv_magnetometro_calculo_2.setBackgroundColor(Color.parseColor("#A8EA8C"));
                    if (ctv_magnetometro_calculo_1.isChecked()) {
                        ctv_magnetometro_calculo_1.setChecked(false);
                        ctv_magnetometro_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    }
                }
            }
        });

        if (cb_magnetometro_actual.isChecked() && (ctv_magnetometro_calculo_1.isChecked() || ctv_magnetometro_calculo_2.isChecked()) &&
                !btn_resultados_magnetometro.isEnabled()) {
            Log.e("if", "3");
            BotonHabilitado();
        } else if (cb_magnetometro_actual.isChecked() && !ctv_magnetometro_calculo_1.isChecked() && !ctv_magnetometro_calculo_2.isChecked() &&
                btn_resultados_magnetometro.isEnabled()) {
            Log.e("if", "3-1");
            BotonDeshabilitado();
        }

    }
    private void BotonHabilitado() {
        Log.e("Boton", "Habilitado");
        btn_resultados_magnetometro.setTextColor(ContextCompat.getColor(context, R.color.black));
        btn_resultados_magnetometro.setBackgroundColor(ContextCompat.getColor(context, R.color.celeste));
        btn_resultados_magnetometro.setEnabled(true);
    }
    private void BotonDeshabilitado() {
        Log.e("Boton", "Deshabilitado");
        btn_resultados_magnetometro.setTextColor(ContextCompat.getColor(context, R.color.gris_oscuro));
        btn_resultados_magnetometro.setBackgroundColor(ContextCompat.getColor(context, R.color.gris_claro));
        btn_resultados_magnetometro.setEnabled(false);
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

                        //doc.put("isExists", true);
                        ValidarCheckBoxDatos();
                        IsRegisterDB();

                    } else if (dispositivoConInternet.equals("SinInternet")) {
                        tv_dialog_subtitle_GD.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                sensorManager.unregisterListener(SensorMagnetometroFragment.this);
                                dialogGD.dismiss();
                                Log.e("DialogConexionRed", "mostrarDialogConexionRed");
                                mostrarDialogConexionRed();
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
        Log.i("Sensor", sensorMagnetometro.getName());
        doc.put("nombre", sensorMagnetometro.getName());
        if (cb_magnetometro_fabricante.isChecked()) {
            doc.put("fabricante", sensorMagnetometro.getVendor());
            Log.e("Fabricante", String.valueOf(sensorMagnetometro.getVendor()));
        }
        if (cb_magnetometro_version.isChecked()) {
            doc.put("version", sensorMagnetometro.getVersion());
            Log.e("Versión", String.valueOf(sensorMagnetometro.getVersion()));
        }
        if (cb_magnetometro_potencia.isChecked()) {
            doc.put("potencia", sensorMagnetometro.getPower());
            Log.e("Potencia", sensorMagnetometro.getPower() + " mA");
        }
        if (cb_magnetometro_resolucion.isChecked()) {
            doc.put("resolucion", sensorMagnetometro.getResolution());
            Log.e("Resolución", sensorMagnetometro.getResolution() + "  μT");
        }
        if (cb_magnetometro_max.isChecked()) {
            doc.put("rangoMax", sensorMagnetometro.getMaximumRange());
            Log.e("Máx", sensorMagnetometro.getMaximumRange() + "  μT");
        }
        if (ctv_magnetometro_calculo_1.isChecked()) {
            HashMap<String, Serializable> magnetismo_1 = new HashMap<String, Serializable>();
            magnetismo_1.put("x", magnetismoEncontrada[0]);
            magnetismo_1.put("y", magnetismoEncontrada[1]);
            magnetismo_1.put("z", magnetismoEncontrada[2]);
            doc.put("magnetismo_1", magnetismo_1);
        }
        if (ctv_magnetometro_calculo_2.isChecked()) {
            HashMap<String, Serializable> magnetismo_2 = new HashMap<String, Serializable>();
            magnetismo_2.put("x", magnetismoEncontrada[0]);
            magnetismo_2.put("y", magnetismoEncontrada[1]);
            magnetismo_2.put("z", magnetismoEncontrada[2]);
            doc.put("magnetismo_2", magnetismo_2);
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
        sensorManager.unregisterListener(SensorMagnetometroFragment.this);

        Log.e("DOCResultsAEnviar1", String.valueOf(docIsRegister));
        for (Map.Entry entry : docIsRegister.entrySet()) {
            if ((doc.containsKey(entry.getKey().toString())) &&
                    (!Objects.equals(docIsRegister.get(entry.getKey().toString()).toString(), doc.get(entry.getKey().toString()).toString()))) {

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
                docIsRegister.put(entry.getKey().toString(), doc.get(entry.getKey().toString()));
            }
        }

        Log.e("DOCResultsAEnviar2", String.valueOf(docIsRegister));

        documentReference.update(sensorDB, docIsRegister)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        dialogGD.dismiss();
                        Log.e("Datos", "Actualizados");
                        ((MainActivity) getActivity()).replaceFragmentResultados(sensorDB, docIsRegister);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Datos", "Error", e);
                    }
                });
    }

    private boolean IsOnline(Context context) {
        Boolean bandIsOnline;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        try {
            if ((networkInfo != null) && (networkInfo.isAvailable()) && (networkInfo.isConnected())) {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com/").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(500);
                urlc.connect();
                dispositivoConInternet = "ConInternet";
                bandIsOnline = true;
            } else {
                dispositivoConInternet = "SinInternet";
                bandIsOnline = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
            dispositivoConInternet = "SinInternet";
            bandIsOnline = false;
        }

        Log.e("Conexión", dispositivoConInternet);

        return bandIsOnline;
    }

    private void mostrarDialogConexionRed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);

        LayoutInflater inflater = getLayoutInflater();

        View view = inflater.inflate(R.layout.dialog_sin_conexion_red, null);

        builder.setView(view);
        builder.setCancelable(false);

        AlertDialog dialog = builder.create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        dialog.show();

        Button btn_wifi_error = view.findViewById(R.id.dialog_btn_wifi_error);
        btn_wifi_error.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (contEvent == 0) {
            contEvent++;
            magnetismoEncontrada[0] = event.values[0];
            magnetismoEncontrada[1] = event.values[1];
            magnetismoEncontrada[2] = event.values[2];
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
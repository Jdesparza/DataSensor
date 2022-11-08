package com.example.datasensor.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
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
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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

public class SensorRitmoCardiacoFragment extends Fragment implements SensorEventListener {

    private Button btn_resultados_ritmoCardiaco;

    private CheckBox cb_ritmoCardiaco_version, cb_ritmoCardiaco_max,
            cb_ritmoCardiaco_fabricante, cb_ritmoCardiaco_potencia, cb_ritmoCardiaco_resolucion,
            cb_ritmoCardiaco_actual;


    private CheckedTextView ctv_ritmoCardiaco_calculo_1, ctv_ritmoCardiaco_calculo_2;

    // Sensor
    private SensorManager sensorManager;
    private Sensor sensorRitmoCardiaco;
    private Float countRitmoCardiaco = 0f;
    private int contEvent = 0;

    private Context context;
    private String dispositivoConInternet;

    // Archivo App
    private SharedPreferences sharedPreferences;
    private String idSmartphone;

    // Dialog Calcular Datos
    AlertDialog dialogCD;
    String tipoCalculo;
    int contClickCargar = 0;
    int contTiempo = 30;

    // Dialog Guardar Datos
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
        View view = inflater.inflate(R.layout.fragment_sensor_ritmo_cardiaco, container, false);

        // Toolbar
        ((MainActivity) getActivity()).BtnRegresarMostrar();
        ((MainActivity) getActivity()).LogoToolbarOcultar();
        ((MainActivity) getActivity()).TitleToolbar("Sensor Ritmo Cardíaco");

        //Sensor Title DB
        sensorDB = "sensorRitmoCardiaco";

        // check Box ritmo cardiaco
        cb_ritmoCardiaco_version = view.findViewById(R.id.cb_ritmoCardiaco_version);
        cb_ritmoCardiaco_max = view.findViewById(R.id.cb_ritmoCardiaco_max);
        cb_ritmoCardiaco_fabricante = view.findViewById(R.id.cb_ritmoCardiaco_fabricante);
        cb_ritmoCardiaco_potencia = view.findViewById(R.id.cb_ritmoCardiaco_potencia);
        cb_ritmoCardiaco_resolucion = view.findViewById(R.id.cb_ritmoCardiaco_resolucion);
        cb_ritmoCardiaco_actual = view.findViewById(R.id.cb_ritmoCardiaco_actual);
        ctv_ritmoCardiaco_calculo_1 = view.findViewById(R.id.ctv_ritmoCardiaco_calculo_1);
        ctv_ritmoCardiaco_calculo_2 = view.findViewById(R.id.ctv_ritmoCardiaco_calculo_2);

        // Sensor
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sensorRitmoCardiaco = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        // boton Registrar
        btn_resultados_ritmoCardiaco = view.findViewById(R.id.btn_resultados_ritmoCardiaco);

        // Habilitar boton registrar
        final Handler handler= new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                HabilitarDesabilitarBotonResult();
                handler.postDelayed(this,500);
            }
        },500);


        btn_resultados_ritmoCardiaco.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (cb_ritmoCardiaco_actual.isChecked() && (ctv_ritmoCardiaco_calculo_1.isChecked() || ctv_ritmoCardiaco_calculo_2.isChecked())) {
                    if (ctv_ritmoCardiaco_calculo_1.isChecked()) tipoCalculo = String.valueOf(ctv_ritmoCardiaco_calculo_1.getText());
                    else if (ctv_ritmoCardiaco_calculo_2.isChecked()) tipoCalculo = String.valueOf(ctv_ritmoCardiaco_calculo_2.getText());

                    DialogCalcularDato();
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

        if (cb_ritmoCardiaco_actual.isChecked() && !ctv_ritmoCardiaco_calculo_1.isEnabled() && !ctv_ritmoCardiaco_calculo_2.isEnabled()) {
            ctv_ritmoCardiaco_calculo_1.setEnabled(true);
            ctv_ritmoCardiaco_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            ctv_ritmoCardiaco_calculo_1.setTextColor(ContextCompat.getColor(context, R.color.black));
            ctv_ritmoCardiaco_calculo_2.setEnabled(true);
            ctv_ritmoCardiaco_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            ctv_ritmoCardiaco_calculo_2.setTextColor(ContextCompat.getColor(context, R.color.black));
        }
        else if (!cb_ritmoCardiaco_actual.isChecked() && ctv_ritmoCardiaco_calculo_1.isEnabled() && ctv_ritmoCardiaco_calculo_2.isEnabled()) {
            ctv_ritmoCardiaco_calculo_1.setEnabled(false);
            ctv_ritmoCardiaco_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.gris_claro));
            ctv_ritmoCardiaco_calculo_1.setTextColor(ContextCompat.getColor(context, R.color.gris_oscuro));
            if (ctv_ritmoCardiaco_calculo_1.isChecked()) ctv_ritmoCardiaco_calculo_1.setChecked(false);
            ctv_ritmoCardiaco_calculo_2.setEnabled(false);
            ctv_ritmoCardiaco_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.gris_claro));
            ctv_ritmoCardiaco_calculo_2.setTextColor(ContextCompat.getColor(context, R.color.gris_oscuro));
            if (ctv_ritmoCardiaco_calculo_2.isChecked()) ctv_ritmoCardiaco_calculo_2.setChecked(false);
        }

        if (
                (cb_ritmoCardiaco_fabricante.isChecked() || cb_ritmoCardiaco_max.isChecked() ||
                        cb_ritmoCardiaco_potencia.isChecked() || cb_ritmoCardiaco_resolucion.isChecked() ||
                        cb_ritmoCardiaco_version.isChecked()) &&
                        (!cb_ritmoCardiaco_actual.isChecked() && !btn_resultados_ritmoCardiaco.isEnabled())
        ) {
            BotonHabilitado();
        }
        else if (
                !cb_ritmoCardiaco_fabricante.isChecked() && !cb_ritmoCardiaco_max.isChecked() &&
                        !cb_ritmoCardiaco_potencia.isChecked() && !cb_ritmoCardiaco_resolucion.isChecked() &&
                        !cb_ritmoCardiaco_version.isChecked() && !cb_ritmoCardiaco_actual.isChecked() && btn_resultados_ritmoCardiaco.isEnabled()
        ) {
            BotonDeshabilitado();
        }

        ctv_ritmoCardiaco_calculo_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ctv_ritmoCardiaco_calculo_1.isChecked() ) {
                    ctv_ritmoCardiaco_calculo_1.setChecked(false);
                    ctv_ritmoCardiaco_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                } else {
                    ctv_ritmoCardiaco_calculo_1.setChecked(true);
                    ctv_ritmoCardiaco_calculo_1.setBackgroundColor(Color.parseColor("#A8EA8C"));
                    if (ctv_ritmoCardiaco_calculo_2.isChecked()) {
                        ctv_ritmoCardiaco_calculo_2.setChecked(false);
                        ctv_ritmoCardiaco_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    }
                }
            }
        });

        ctv_ritmoCardiaco_calculo_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ctv_ritmoCardiaco_calculo_2.isChecked() ) {
                    ctv_ritmoCardiaco_calculo_2.setChecked(false);
                    ctv_ritmoCardiaco_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                } else {
                    ctv_ritmoCardiaco_calculo_2.setChecked(true);
                    ctv_ritmoCardiaco_calculo_2.setBackgroundColor(Color.parseColor("#A8EA8C"));
                    if (ctv_ritmoCardiaco_calculo_1.isChecked()) {
                        ctv_ritmoCardiaco_calculo_1.setChecked(false);
                        ctv_ritmoCardiaco_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    }
                }
            }
        });

        if (cb_ritmoCardiaco_actual.isChecked() && (ctv_ritmoCardiaco_calculo_1.isChecked() || ctv_ritmoCardiaco_calculo_2.isChecked()) &&
                !btn_resultados_ritmoCardiaco.isEnabled()) {
            BotonHabilitado();
        } else if (cb_ritmoCardiaco_actual.isChecked() && !ctv_ritmoCardiaco_calculo_1.isChecked() && !ctv_ritmoCardiaco_calculo_2.isChecked() &&
                btn_resultados_ritmoCardiaco.isEnabled()) {
            BotonDeshabilitado();
        }

    }
    private void BotonHabilitado() {
        btn_resultados_ritmoCardiaco.setTextColor(ContextCompat.getColor(context, R.color.black));
        btn_resultados_ritmoCardiaco.setBackgroundColor(ContextCompat.getColor(context, R.color.celeste));
        btn_resultados_ritmoCardiaco.setEnabled(true);
    }
    private void BotonDeshabilitado() {
        btn_resultados_ritmoCardiaco.setTextColor(ContextCompat.getColor(context, R.color.gris_oscuro));
        btn_resultados_ritmoCardiaco.setBackgroundColor(ContextCompat.getColor(context, R.color.gris_claro));
        btn_resultados_ritmoCardiaco.setEnabled(false);
    }

    @SuppressLint("SetTextI18n")
    private void DialogCalcularDato() {
        String typeCalString = "";
        if (ctv_ritmoCardiaco_calculo_1.isChecked()) typeCalString = "correr";
        else if (ctv_ritmoCardiaco_calculo_2.isChecked()) typeCalString = "caminar";

        AlertDialog.Builder builderCD = new AlertDialog.Builder(context);
        LayoutInflater inflaterCD = getActivity().getLayoutInflater();
        View viewCD = inflaterCD.inflate(R.layout.dialog_calculo_dato_sensor, null);

        builderCD.setView(viewCD);
        builderCD.setCancelable(false);

        dialogCD = builderCD.create();
        if (dialogCD.getWindow() != null) {
            dialogCD.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        dialogCD.show();

        TextView tv_title_dialog_dato_sensor = viewCD.findViewById(R.id.tv_title_dialog_dato_sensor);
        TextView tv_subtitle_dialog_dato_sensor = viewCD.findViewById(R.id.tv_subtitle_dialog_dato_sensor);
        TextView tv_mensaje_dialog_dato_sensor = viewCD.findViewById(R.id.tv_mensaje_dialog_dato_sensor);
        Button btn_iniciar_dialog_calculo_dato_sensor = viewCD.findViewById(R.id.btn_iniciar_dialog_calculo_dato_sensor);
        Button btn_cargar_dialog_calculo_dato_sensor = viewCD.findViewById(R.id.btn_cargar_dialog_calculo_dato_sensor);
        ImageButton btn_close_dialog_calculo_dato_sensor = viewCD.findViewById(R.id.btn_close_dialog_calculo_dato_sensor);
        LinearLayout ll_tiempo_dialog_calculo_dato_sensor = viewCD.findViewById(R.id.ll_tiempo_dialog_calculo_dato_sensor);
        TextView tv_tiempo_dialog_calculo_dato_sensor = viewCD.findViewById(R.id.tv_tiempo_dialog_calculo_dato_sensor);

        tv_title_dialog_dato_sensor.setText("Calcular Pasos");
        tv_subtitle_dialog_dato_sensor.setText(tipoCalculo);
        tv_mensaje_dialog_dato_sensor.setText("Una vez iniciado el cálculo, se iniciará una cuenta regresiva de 30 " +
                "segundos, en donde deberás " + typeCalString + ".\nDeja de hacerlo al escuchar el sonido" +
                " que se reproducirá al pasar los 30 segundos. \n\nComienza a capturar el dato colocando " +
                "el dedo sobre la parte del sensor, la mayoría de las veces es en donde esta el flash y " +
                "quita el dedo una vez veas el mensaje de que se capturó el dato y luego procede a dar clic en cargar.");
        btn_close_dialog_calculo_dato_sensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contClickCargar++;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (contClickCargar == 1 || contClickCargar > 2) {
                            Toast.makeText(context, "Dar 2 veces click", Toast.LENGTH_SHORT).show();
                        } else if (contClickCargar == 2) {
                            if (contEvent >= 1) sensorManager.unregisterListener(SensorRitmoCardiacoFragment.this);
                            contEvent = 0;
                            dialogCD.dismiss();
                        }
                        contClickCargar = 0;
                    }
                }, 500);
            }
        });
        btn_iniciar_dialog_calculo_dato_sensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // desactivar boton calculo
                btn_iniciar_dialog_calculo_dato_sensor.setEnabled(false);
                btn_iniciar_dialog_calculo_dato_sensor.setBackgroundColor(ContextCompat.getColor(context, R.color.gris_medio));
                btn_iniciar_dialog_calculo_dato_sensor.setTextColor(ContextCompat.getColor(context, R.color.white));

                final Handler handler= new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (contTiempo >= 1) {
                            if (ll_tiempo_dialog_calculo_dato_sensor.getVisibility() == View.GONE) {
                                btn_close_dialog_calculo_dato_sensor.setVisibility(View.GONE);
                                btn_iniciar_dialog_calculo_dato_sensor.setVisibility(View.GONE);
                                btn_cargar_dialog_calculo_dato_sensor.setVisibility(View.GONE);
                                ll_tiempo_dialog_calculo_dato_sensor.setVisibility(View.VISIBLE);
                            }
                            tv_tiempo_dialog_calculo_dato_sensor.setText(String.valueOf(contTiempo));
                            handler.postDelayed(this,1000);
                        } else if (contTiempo <= 0) {
                            Sound();
                            ll_tiempo_dialog_calculo_dato_sensor.setVisibility(View.GONE);
                            btn_close_dialog_calculo_dato_sensor.setVisibility(View.VISIBLE);
                            btn_iniciar_dialog_calculo_dato_sensor.setVisibility(View.VISIBLE);
                            btn_cargar_dialog_calculo_dato_sensor.setVisibility(View.VISIBLE);

                            // Iniciar evento
                            sensorManager.registerListener(SensorRitmoCardiacoFragment.this, sensorRitmoCardiaco, SensorManager.SENSOR_DELAY_UI);
                            contEvent = 1;

                            // Habilitar Calcular Ritmo
                            final Handler handler= new Handler();
                            handler.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if (countRitmoCardiaco > 0.0) {
                                        Toast.makeText(context, "Dato obtenido", Toast.LENGTH_SHORT).show();
                                        contEvent = 0;
                                        sensorManager.unregisterListener(SensorRitmoCardiacoFragment.this);
                                    } else if (countRitmoCardiaco == 0.0) {
                                        handler.postDelayed(this,500);
                                    }
                                }
                            },500);

                            if (!btn_cargar_dialog_calculo_dato_sensor.isEnabled()) {
                                btn_cargar_dialog_calculo_dato_sensor.setEnabled(true);
                                btn_cargar_dialog_calculo_dato_sensor.setBackgroundColor(ContextCompat.getColor(context, R.color.partSup));
                            }

                            contTiempo = 30;
                        }
                        contTiempo--;
                    }
                },1000);
            }
        });
        btn_cargar_dialog_calculo_dato_sensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                contClickCargar++;
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if (contClickCargar == 1 || contClickCargar > 2) {
                            Toast.makeText(context, "Dar 2 veces click", Toast.LENGTH_SHORT).show();
                        } else if (contClickCargar == 2) {
                            dialogCD.dismiss();
                            GuardarDatos();
                        }
                        contClickCargar = 0;
                    }
                }, 500);
            }
        });
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
                                if (contEvent >= 1) sensorManager.unregisterListener(SensorRitmoCardiacoFragment.this);
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
        Log.i("Sensor", sensorRitmoCardiaco.getName());
        doc.put("nombre", sensorRitmoCardiaco.getName());
        if (cb_ritmoCardiaco_fabricante.isChecked()) {
            doc.put("fabricante", sensorRitmoCardiaco.getVendor());
            Log.e("Fabricante", String.valueOf(sensorRitmoCardiaco.getVendor()));
        }
        if (cb_ritmoCardiaco_version.isChecked()) {
            doc.put("version", sensorRitmoCardiaco.getVersion());
            Log.e("Versión", String.valueOf(sensorRitmoCardiaco.getVersion()));
        }
        if (cb_ritmoCardiaco_potencia.isChecked()) {
            doc.put("potencia", sensorRitmoCardiaco.getPower());
            Log.e("Potencia", sensorRitmoCardiaco.getPower() + " mA");
        }
        if (cb_ritmoCardiaco_resolucion.isChecked()) {
            doc.put("resolucion", sensorRitmoCardiaco.getResolution());
            Log.e("Resolución", String.valueOf(sensorRitmoCardiaco.getResolution()));
        }
        if (cb_ritmoCardiaco_max.isChecked()) {
            doc.put("rangoMax", sensorRitmoCardiaco.getMaximumRange());
            Log.e("Máx", String.valueOf(sensorRitmoCardiaco.getMaximumRange()));
        }
        if (ctv_ritmoCardiaco_calculo_1.isChecked()) {
            doc.put("calRitmoCardiaco_1", countRitmoCardiaco);
            Log.e("calRitmoCardiaco_1", String.valueOf(countRitmoCardiaco));
        }
        if (ctv_ritmoCardiaco_calculo_2.isChecked()) {
            doc.put("calRitmoCardiaco_2", countRitmoCardiaco);
            Log.e("calRitmoCardiaco_2", String.valueOf(countRitmoCardiaco));
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
        Log.e("DOCResultsAEnviar1", String.valueOf(docIsRegister));
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

    private void Sound() {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.latigo);
        mediaPlayer.start();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.values[0] > 0.0 && contEvent == 1) {
            countRitmoCardiaco = event.values[0];
            contEvent++;
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
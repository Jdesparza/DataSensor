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
import androidx.cardview.widget.CardView;
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

import org.w3c.dom.Text;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SensorPodometroFragment extends Fragment implements SensorEventListener {

    private Button btn_resultados_podometro;
    private ResultadosFragment resultadosFragment = new ResultadosFragment();

    private CheckBox cb_podometro_version, cb_podometro_max,
            cb_podometro_fabricante, cb_podometro_potencia, cb_podometro_resolucion,
            cb_podometro_actual;


    private CheckedTextView ctv_podometro_calculo_1, ctv_podometro_calculo_2;

    // Sensor
    private SensorManager sensorManager;
    private Sensor sensorPodometro;
    private int contEvent = 0;
    private Float countStep = 0f;
    private Float pasosPrevios = 0f;

    private Context context;
    private String dispositivoConInternet;

    // Archivo App
    private SharedPreferences sharedPreferences;
    private String idSmartphone;

    // Dialog Calcular Datos
    AlertDialog dialogCD;
    String tipoCalculo;
    int contClickCargar = 0;
    int contTiempo = 0;

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
    public void onPause() {
        super.onPause();
        //sensorManager.unregisterListener(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sensor_podometro, container, false);

        // Toolbar
        ((MainActivity) getActivity()).BtnRegresarMostrar();
        ((MainActivity) getActivity()).LogoToolbarOcultar();
        ((MainActivity) getActivity()).TitleToolbar("Sensor Podómetro");

        //Sensor Title DB
        sensorDB = "sensorPodometro";

        // check Box podometro
        cb_podometro_version = view.findViewById(R.id.cb_podometro_version);
        cb_podometro_max = view.findViewById(R.id.cb_podometro_max);
        cb_podometro_fabricante = view.findViewById(R.id.cb_podometro_fabricante);
        cb_podometro_potencia = view.findViewById(R.id.cb_podometro_potencia);
        cb_podometro_resolucion = view.findViewById(R.id.cb_podometro_resolucion);
        cb_podometro_actual = view.findViewById(R.id.cb_podometro_actual);
        ctv_podometro_calculo_1 = view.findViewById(R.id.ctv_podometro_calculo_1);
        ctv_podometro_calculo_2 = view.findViewById(R.id.ctv_podometro_calculo_2);

        // Sensor
        sensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        sensorPodometro = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        // boton Registrar
        btn_resultados_podometro = view.findViewById(R.id.btn_resultados_podometro);

        // Habilitar Calcular Pasos
        final Handler handler= new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                HabilitarDesabilitarBotonResult();
                handler.postDelayed(this,500);
            }
        },500);


        btn_resultados_podometro.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (cb_podometro_actual.isChecked() && (ctv_podometro_calculo_1.isChecked() || ctv_podometro_calculo_2.isChecked())) {
                    Log.e("Click", "Con Calculo");
                    if (ctv_podometro_calculo_1.isChecked()) tipoCalculo = String.valueOf(ctv_podometro_calculo_1.getText());
                    else if (ctv_podometro_calculo_2.isChecked()) tipoCalculo = String.valueOf(ctv_podometro_calculo_2.getText());
                    // Iniciar evento
                    contEvent = 0;
                    sensorManager.registerListener(SensorPodometroFragment.this, sensorPodometro, SensorManager.SENSOR_DELAY_UI);

                    DialogCalcularDato();
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

        if (cb_podometro_actual.isChecked() && !ctv_podometro_calculo_1.isEnabled() && !ctv_podometro_calculo_2.isEnabled()) {
            Log.e("if", "1");
            ctv_podometro_calculo_1.setEnabled(true);
            ctv_podometro_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            ctv_podometro_calculo_1.setTextColor(ContextCompat.getColor(context, R.color.black));
            ctv_podometro_calculo_2.setEnabled(true);
            ctv_podometro_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            ctv_podometro_calculo_2.setTextColor(ContextCompat.getColor(context, R.color.black));
        }
        else if (!cb_podometro_actual.isChecked() && ctv_podometro_calculo_1.isEnabled() && ctv_podometro_calculo_2.isEnabled()) {
            Log.e("if", "1-1");
            ctv_podometro_calculo_1.setEnabled(false);
            ctv_podometro_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.gris_claro));
            ctv_podometro_calculo_1.setTextColor(ContextCompat.getColor(context, R.color.gris_oscuro));
            if (ctv_podometro_calculo_1.isChecked()) ctv_podometro_calculo_1.setChecked(false);
            ctv_podometro_calculo_2.setEnabled(false);
            ctv_podometro_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.gris_claro));
            ctv_podometro_calculo_2.setTextColor(ContextCompat.getColor(context, R.color.gris_oscuro));
            if (ctv_podometro_calculo_2.isChecked()) ctv_podometro_calculo_2.setChecked(false);
        }

        if (
                (cb_podometro_fabricante.isChecked() || cb_podometro_max.isChecked() ||
                        cb_podometro_potencia.isChecked() || cb_podometro_resolucion.isChecked() ||
                        cb_podometro_version.isChecked()) &&
                        (!cb_podometro_actual.isChecked() && !btn_resultados_podometro.isEnabled())
        ) {
            Log.e("if", "2");
            BotonHabilitado();
        }
        else if (
                !cb_podometro_fabricante.isChecked() && !cb_podometro_max.isChecked() &&
                        !cb_podometro_potencia.isChecked() && !cb_podometro_resolucion.isChecked() &&
                        !cb_podometro_version.isChecked() && !cb_podometro_actual.isChecked() && btn_resultados_podometro.isEnabled()
        ) {
            Log.e("if", "2-1");
            BotonDeshabilitado();
        }

        ctv_podometro_calculo_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ctv_podometro_calculo_1.isChecked() ) {
                    ctv_podometro_calculo_1.setChecked(false);
                    ctv_podometro_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                } else {
                    ctv_podometro_calculo_1.setChecked(true);
                    ctv_podometro_calculo_1.setBackgroundColor(Color.parseColor("#A8EA8C"));
                    if (ctv_podometro_calculo_2.isChecked()) {
                        ctv_podometro_calculo_2.setChecked(false);
                        ctv_podometro_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    }
                }
            }
        });

        ctv_podometro_calculo_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ctv_podometro_calculo_2.isChecked() ) {
                    ctv_podometro_calculo_2.setChecked(false);
                    ctv_podometro_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                } else {
                    ctv_podometro_calculo_2.setChecked(true);
                    ctv_podometro_calculo_2.setBackgroundColor(Color.parseColor("#A8EA8C"));
                    if (ctv_podometro_calculo_1.isChecked()) {
                        ctv_podometro_calculo_1.setChecked(false);
                        ctv_podometro_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    }
                }
            }
        });

        if (cb_podometro_actual.isChecked() && (ctv_podometro_calculo_1.isChecked() || ctv_podometro_calculo_2.isChecked()) &&
                !btn_resultados_podometro.isEnabled()) {
            Log.e("if", "3");
            BotonHabilitado();
        } else if (cb_podometro_actual.isChecked() && !ctv_podometro_calculo_1.isChecked() && !ctv_podometro_calculo_2.isChecked() &&
                btn_resultados_podometro.isEnabled()) {
            Log.e("if", "3-1");
            BotonDeshabilitado();
        }

    }
    private void BotonHabilitado() {
        Log.e("Boton", "Habilitado");
        btn_resultados_podometro.setTextColor(ContextCompat.getColor(context, R.color.black));
        btn_resultados_podometro.setBackgroundColor(ContextCompat.getColor(context, R.color.celeste));
        btn_resultados_podometro.setEnabled(true);
    }
    private void BotonDeshabilitado() {
        Log.e("Boton", "Deshabilitado");
        btn_resultados_podometro.setTextColor(ContextCompat.getColor(context, R.color.gris_oscuro));
        btn_resultados_podometro.setBackgroundColor(ContextCompat.getColor(context, R.color.gris_claro));
        btn_resultados_podometro.setEnabled(false);
    }

    @SuppressLint("SetTextI18n")
    private void DialogCalcularDato() {
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
        tv_mensaje_dialog_dato_sensor.setText("Una vez iniciado el cálculo, contarás con 6 " +
                "segundos para introducir el smartphone en el bolsillo.\n\nEmpieza a caminar luego " +
                "de escuchar el sonido que se reproducirá al terminar los 6 segundos y deja " +
                "de caminar al " + tipoCalculo.toLowerCase());
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
                            sensorManager.unregisterListener(SensorPodometroFragment.this);
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
                        contTiempo++;
                        if (contTiempo <= 6) {
                            if (ll_tiempo_dialog_calculo_dato_sensor.getVisibility() == View.GONE) {
                                btn_close_dialog_calculo_dato_sensor.setVisibility(View.GONE);
                                btn_iniciar_dialog_calculo_dato_sensor.setVisibility(View.GONE);
                                btn_cargar_dialog_calculo_dato_sensor.setVisibility(View.GONE);
                                ll_tiempo_dialog_calculo_dato_sensor.setVisibility(View.VISIBLE);
                            }
                            tv_tiempo_dialog_calculo_dato_sensor.setText(String.valueOf(contTiempo));
                            handler.postDelayed(this,1000);
                        } else if (contTiempo > 6) {
                            Sound();
                            contEvent++;
                            ll_tiempo_dialog_calculo_dato_sensor.setVisibility(View.GONE);
                            btn_close_dialog_calculo_dato_sensor.setVisibility(View.VISIBLE);
                            btn_iniciar_dialog_calculo_dato_sensor.setVisibility(View.VISIBLE);
                            btn_cargar_dialog_calculo_dato_sensor.setVisibility(View.VISIBLE);

                            if (!btn_cargar_dialog_calculo_dato_sensor.isEnabled()) {
                                btn_cargar_dialog_calculo_dato_sensor.setEnabled(true);
                                btn_cargar_dialog_calculo_dato_sensor.setBackgroundColor(ContextCompat.getColor(context, R.color.partSup));
                            }

                            contTiempo = 0;
                        }
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

                        //doc.put("isExists", true);
                        ValidarCheckBoxDatos();
                        IsRegisterDB();

                    } else if (dispositivoConInternet.equals("SinInternet")) {
                        tv_dialog_subtitle_GD.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                sensorManager.unregisterListener(SensorPodometroFragment.this);
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
        Log.i("Sensor", sensorPodometro.getName());
        doc.put("nombre", sensorPodometro.getName());
        if (cb_podometro_fabricante.isChecked()) {
            doc.put("fabricante", sensorPodometro.getVendor());
            Log.e("Fabricante", String.valueOf(sensorPodometro.getVendor()));
        }
        if (cb_podometro_version.isChecked()) {
            doc.put("version", sensorPodometro.getVersion());
            Log.e("Versión", String.valueOf(sensorPodometro.getVersion()));
        }
        if (cb_podometro_potencia.isChecked()) {
            doc.put("potencia", sensorPodometro.getPower());
            Log.e("Potencia", sensorPodometro.getPower() + " mA");
        }
        if (cb_podometro_resolucion.isChecked()) {
            doc.put("resolucion", sensorPodometro.getResolution());
            Log.e("Resolución", String.valueOf(sensorPodometro.getResolution()));
        }
        if (cb_podometro_max.isChecked()) {
            doc.put("rangoMax", sensorPodometro.getMaximumRange());
            Log.e("Máx", String.valueOf(sensorPodometro.getMaximumRange()));
        }
        if (ctv_podometro_calculo_1.isChecked()) {
            doc.put("calPasos_10", countStep);
            Log.e("calPasos_10", String.valueOf(countStep));
        }
        if (ctv_podometro_calculo_2.isChecked()) {
            doc.put("calPasos_15", countStep);
            Log.e("calPasos_15", String.valueOf(countStep));
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
        sensorManager.unregisterListener(SensorPodometroFragment.this);

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
            Log.e("Modificado", String.valueOf(isModificado));
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
        } else {
            Log.e("Modificado", String.valueOf(isModificado));
            dialogGD.dismiss();
            ((MainActivity) getActivity()).replaceFragmentResultados(sensorDB, docIsRegister);
        }

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

    private void Sound() {
        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.latigo);
        mediaPlayer.start();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.e("SensorEvent", String.valueOf(event.sensor));
        if (contEvent == 0) {
            pasosPrevios = event.values[0];
            Log.e("Ingreso", "Ingreso Nuevo Al Activity");
        } else if (contEvent == 1) {
            Log.e("Ingreso", "Listo para contar pasos");
            countStep = event.values[0] - pasosPrevios;
            Log.e("Pasos", String.valueOf(countStep));
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
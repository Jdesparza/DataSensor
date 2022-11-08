package com.example.datasensor.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
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
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class SensorMicrofonoFragment extends Fragment {

    private Button btn_resultados_micro;
    private ResultadosFragment resultadosFragment = new ResultadosFragment();

    private CheckBox cb_frecuencia_micro, cb_formato_micro, cb_decibel_micro;

    private CheckedTextView ctv_micro_calculo_1, ctv_micro_calculo_2;

    private Context context;
    private String dispositivoConInternet;

    // Archivo App
    private SharedPreferences sharedPreferences;
    private String idSmartphone;

    private AudioRecord audioRecord;
    int frecuencia = 8000;
    int channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
    int audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
    int bufferSize = AudioRecord.getMinBufferSize(frecuencia, channelConfiguration, audioEncoding);
    boolean isGetVoiceRun = false;
    Object mLock = new Object();
    Thread thread;

    // Firebase
    private FirebaseFirestore db;
    private DocumentReference documentReference;
    private Map<String, Serializable> doc = new HashMap<String, Serializable>();
    private Map<String, Serializable> docIsRegister = new HashMap<String, Serializable>();
    private String sensorDB;
    private boolean isModificado = false;

    private String formatoMicro =  "16BIT PCM";
    private String canalMicro =  "";
    private double decibelMax = 0.0;
    private double decibelMin = 1000.0;

    // Dialog Guardar Datos
    AlertDialog dialogGD;

    // Dialog Calcular Datos
    AlertDialog dialogCD;
    String tipoCalculo;
    int contClickCargar = 0;
    int contTiempo = 15;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sensor_microfono, container, false);

        // Toolbar
        ((MainActivity) getActivity()).BtnRegresarMostrar();
        ((MainActivity) getActivity()).LogoToolbarOcultar();
        ((MainActivity) getActivity()).TitleToolbar("Sensor Micrófono");

        //Sensor Title DB
        sensorDB = "sensorMicrofono";

        // check Box microfono
        cb_frecuencia_micro = view.findViewById(R.id.cb_frecuencia_micro);
        cb_formato_micro = view.findViewById(R.id.cb_formato_micro);
        cb_decibel_micro = view.findViewById(R.id.cb_decibel_micro);
        ctv_micro_calculo_1 = view.findViewById(R.id.ctv_micro_calculo_1);
        ctv_micro_calculo_2 = view.findViewById(R.id.ctv_micro_calculo_2);

        // boton Registrar
        btn_resultados_micro = view.findViewById(R.id.btn_resultados_micro);

        // Habilitar Calcular Pasos
        final Handler handler= new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                HabilitarDesabilitarBotonResult();
                handler.postDelayed(this,500);
            }
        },500);


        btn_resultados_micro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (cb_decibel_micro.isChecked() && (ctv_micro_calculo_1.isChecked() || ctv_micro_calculo_2.isChecked())) {
                    if (ctv_micro_calculo_1.isChecked()) tipoCalculo = String.valueOf(ctv_micro_calculo_1.getText());
                    else if (ctv_micro_calculo_2.isChecked()) tipoCalculo = String.valueOf(ctv_micro_calculo_2.getText());

                    DialogCalcularDato();
                } else {
                    GuardarDatos();
                }
            }
        });

        return view;
    }

    private void HabilitarDesabilitarBotonResult() {

        if (cb_decibel_micro.isChecked() && !ctv_micro_calculo_1.isEnabled() && !ctv_micro_calculo_2.isEnabled()) {
            ctv_micro_calculo_1.setEnabled(true);
            ctv_micro_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            ctv_micro_calculo_1.setTextColor(ContextCompat.getColor(context, R.color.black));
            ctv_micro_calculo_2.setEnabled(true);
            ctv_micro_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
            ctv_micro_calculo_2.setTextColor(ContextCompat.getColor(context, R.color.black));
        }
        else if (!cb_decibel_micro.isChecked() && ctv_micro_calculo_1.isEnabled() && ctv_micro_calculo_2.isEnabled()) {
            ctv_micro_calculo_1.setEnabled(false);
            ctv_micro_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.gris_claro));
            ctv_micro_calculo_1.setTextColor(ContextCompat.getColor(context, R.color.gris_oscuro));
            if (ctv_micro_calculo_1.isChecked()) ctv_micro_calculo_1.setChecked(false);
            ctv_micro_calculo_2.setEnabled(false);
            ctv_micro_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.gris_claro));
            ctv_micro_calculo_2.setTextColor(ContextCompat.getColor(context, R.color.gris_oscuro));
            if (ctv_micro_calculo_2.isChecked()) ctv_micro_calculo_2.setChecked(false);
        }

        if (
                (cb_formato_micro.isChecked() || cb_frecuencia_micro.isChecked()) &&
                        (!cb_decibel_micro.isChecked() && !btn_resultados_micro.isEnabled())
        ) {
            BotonHabilitado();
        }
        else if (
                !cb_formato_micro.isChecked() && !cb_frecuencia_micro.isChecked() &&
                        !cb_decibel_micro.isChecked() && btn_resultados_micro.isEnabled()
        ) {
            BotonDeshabilitado();
        }

        ctv_micro_calculo_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ctv_micro_calculo_1.isChecked() ) {
                    ctv_micro_calculo_1.setChecked(false);
                    ctv_micro_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                } else {
                    ctv_micro_calculo_1.setChecked(true);
                    ctv_micro_calculo_1.setBackgroundColor(Color.parseColor("#A8EA8C"));
                    if (ctv_micro_calculo_2.isChecked()) {
                        ctv_micro_calculo_2.setChecked(false);
                        ctv_micro_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    }
                }
            }
        });

        ctv_micro_calculo_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ctv_micro_calculo_2.isChecked() ) {
                    ctv_micro_calculo_2.setChecked(false);
                    ctv_micro_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                } else {
                    ctv_micro_calculo_2.setChecked(true);
                    ctv_micro_calculo_2.setBackgroundColor(Color.parseColor("#A8EA8C"));
                    if (ctv_micro_calculo_1.isChecked()) {
                        ctv_micro_calculo_1.setChecked(false);
                        ctv_micro_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    }
                }
            }
        });

        if (cb_decibel_micro.isChecked() && (ctv_micro_calculo_1.isChecked() || ctv_micro_calculo_2.isChecked()) &&
                !btn_resultados_micro.isEnabled()) {
            BotonHabilitado();
        } else if (cb_decibel_micro.isChecked() && !ctv_micro_calculo_1.isChecked() && !ctv_micro_calculo_2.isChecked() &&
                btn_resultados_micro.isEnabled()) {
            BotonDeshabilitado();
        }

    }
    private void BotonHabilitado() {
        btn_resultados_micro.setTextColor(ContextCompat.getColor(context, R.color.black));
        btn_resultados_micro.setBackgroundColor(ContextCompat.getColor(context, R.color.celeste));
        btn_resultados_micro.setEnabled(true);
    }
    private void BotonDeshabilitado() {
        btn_resultados_micro.setTextColor(ContextCompat.getColor(context, R.color.gris_oscuro));
        btn_resultados_micro.setBackgroundColor(ContextCompat.getColor(context, R.color.gris_claro));
        btn_resultados_micro.setEnabled(false);
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

        tv_title_dialog_dato_sensor.setText("Medir Sonido");
        tv_subtitle_dialog_dato_sensor.setText(tipoCalculo);
        tv_mensaje_dialog_dato_sensor.setText("Una vez iniciado el cálculo, se iniciará una cuenta regresiva de 15 " +
                "segundos, en donde se medirá el sonido en decibel(dB) a través del micrófono.\n\n No " +
                "causes ruidos que puedan afectar el cálculo del sonido.");
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
                            isGetVoiceRun = false;
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
                            if (!isGetVoiceRun) {
                                isGetVoiceRun = true;
                                sonometro_dB();
                            }
                            if (ll_tiempo_dialog_calculo_dato_sensor.getVisibility() == View.GONE) {
                                btn_close_dialog_calculo_dato_sensor.setVisibility(View.GONE);
                                btn_iniciar_dialog_calculo_dato_sensor.setVisibility(View.GONE);
                                btn_cargar_dialog_calculo_dato_sensor.setVisibility(View.GONE);
                                ll_tiempo_dialog_calculo_dato_sensor.setVisibility(View.VISIBLE);
                            }
                            tv_tiempo_dialog_calculo_dato_sensor.setText(String.valueOf(contTiempo));
                            handler.postDelayed(this,1000);
                        } else if (contTiempo <= 0) {
                            isGetVoiceRun = false;
                            thread.interrupt();
                            ll_tiempo_dialog_calculo_dato_sensor.setVisibility(View.GONE);
                            btn_close_dialog_calculo_dato_sensor.setVisibility(View.VISIBLE);
                            btn_iniciar_dialog_calculo_dato_sensor.setVisibility(View.VISIBLE);
                            btn_cargar_dialog_calculo_dato_sensor.setVisibility(View.VISIBLE);

                            if (!btn_cargar_dialog_calculo_dato_sensor.isEnabled()) {
                                btn_cargar_dialog_calculo_dato_sensor.setEnabled(true);
                                btn_cargar_dialog_calculo_dato_sensor.setBackgroundColor(ContextCompat.getColor(context, R.color.partSup));
                            }

                            contTiempo = 15;
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

    private void ValidarCheckBoxDatos() {

        if (cb_formato_micro.isChecked()) {
            doc.put("formato", formatoMicro);
            Log.e("Formato", String.valueOf(formatoMicro));
        }
        if (cb_frecuencia_micro.isChecked()) {
            doc.put("frecuencia", frecuencia);
            Log.e("Frecuencia", frecuencia + " Hz");
        }
        if (ctv_micro_calculo_1.isChecked()) {
            HashMap<String, Serializable> sonido_1 = new HashMap<String, Serializable>();
            if (Objects.equals(canalMicro, String.valueOf(AudioFormat.CHANNEL_IN_MONO))) {
                canalMicro = "mono";
            }
            sonido_1.put("canal", canalMicro);
            sonido_1.put("max", decibelMax);
            sonido_1.put("min", decibelMin);
            doc.put("calSonido_1", sonido_1);
            Log.e("Sonido 01", String.valueOf(sonido_1));

        }
        if (ctv_micro_calculo_2.isChecked()) {
            HashMap<String, Serializable> sonido_2 = new HashMap<String, Serializable>();
            if (Objects.equals(canalMicro, String.valueOf(AudioFormat.CHANNEL_IN_MONO))) {
                canalMicro = "mono";
            } else if (Objects.equals(canalMicro, String.valueOf(AudioFormat.CHANNEL_IN_STEREO))) {
                canalMicro = "stereo";
            } else {
                canalMicro = "default";
            }
            sonido_2.put("canal", canalMicro);
            sonido_2.put("max", decibelMax);
            sonido_2.put("min", decibelMin);
            doc.put("calSonido_2", sonido_2);
            Log.e("Sonido 02", String.valueOf(sonido_2));
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
                    (!entry.getKey().toString().equals("calSonido_1") && !entry.getKey().toString().equals("calSonido_2")) &&
                    (!Objects.equals(docIsRegister.get(entry.getKey().toString()).toString(), doc.get(entry.getKey().toString()).toString()))) {
                isModificado = true;
                docIsRegister.put(entry.getKey().toString(), doc.get(entry.getKey().toString()));
                doc.remove(entry.getKey().toString());
            }
            else if ((doc.containsKey(entry.getKey().toString())) &&
                    (!entry.getKey().toString().equals("calSonido_1") && !entry.getKey().toString().equals("calSonido_2")) &&
                    (Objects.equals(docIsRegister.get(entry.getKey().toString()).toString(), doc.get(entry.getKey().toString()).toString()))) {
                doc.remove(entry.getKey().toString());
            } else if ((doc.containsKey(entry.getKey().toString()) &&
                    (entry.getKey().toString().equals("calSonido_1") || entry.getKey().toString().equals("calSonido_2")))
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

    private void sonometro_dB() {
        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO);
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, frecuencia, channelConfiguration, audioEncoding, bufferSize);

        thread = new Thread(new Runnable() {
            @Override
            public void run() {
                audioRecord.startRecording();
                short[] buffer = new short[bufferSize];
                while (isGetVoiceRun) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    // r is the actual length of data read, generally speaking, r will be less than buffersize
                    int r = audioRecord.read(buffer, 0, bufferSize);
                    long v = 0;
                    // Saque el contenido del búfer. Haz la suma de cuadrados
                    for (int i = 0; i <buffer.length; i++) {
                        v += buffer[i] * buffer[i];
                    }
                    // Divide la suma de los cuadrados por la longitud total de los datos para obtener el volumen.
                    double mean = v / (double) r;
                    double volume = 10 * Math.log10(mean);
                    DecimalFormat df = new DecimalFormat("####.00");
                    Log.d("Decibels_dB", "Decibel value:" + df.format(volume));
                    Log.d("CanalSonometro", String.valueOf(audioRecord.getChannelConfiguration()));
                    canalMicro = String.valueOf(audioRecord.getChannelConfiguration());
                    if (volume > decibelMax) decibelMax = volume;
                    if (volume < decibelMin) decibelMin = volume;
                    synchronized (mLock) {
                        try {
                            mLock.wait(100);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                audioRecord.stop();
                audioRecord.release();
                audioRecord = null;
            }
        });

        thread.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isGetVoiceRun) {
            isGetVoiceRun = false;
        }
    }

    private class IsOnlineTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            IsOnline(context);

            return null;
        }
    }
}
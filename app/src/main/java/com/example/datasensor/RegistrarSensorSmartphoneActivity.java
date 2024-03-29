package com.example.datasensor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.ColorDrawable;
import android.hardware.ConsumerIrManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MicrophoneInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RegistrarSensorSmartphoneActivity extends AppCompatActivity {

    private Button btn_Comenzar_registro_sensores;
    private ImageView toolbar_logo;
    private ImageButton toolbar_to_return;
    private TextView toolbar_title;

    private SharedPreferences sharedPreferences;
    SharedPreferences.Editor editorInfoApp;
    private String idSmartphone;
    private String numSerieSmartphone;
    private String android_id;

    // Sensor Manager
    private SensorManager sensorManager = null;
    private Sensor sensorTermometro = null;
    private Sensor sensorDeProximidad = null;
    private Sensor sensorDeLuz = null;
    private Sensor sensorAcelerometro = null;
    private Sensor sensorDeGiroscopio = null;
    private Sensor sensorMagnetometro = null;
    private Sensor sensorPodometro = null;
    private Sensor sensorBarometro = null;
    private Sensor sensorRitmoCardiaco = null;

    // Audio Manager
    private AudioManager audioManager = null;
    private List<MicrophoneInfo> sensorMicrofono = null;
    private AudioDeviceInfo[] sensorMicrofonoDevice = null;


    // Sensores Guardado
    private HashMap<String, java.io.Serializable> band_sensorTermometro = new HashMap<String, java.io.Serializable>();
    private HashMap<String, java.io.Serializable> band_sensorDeProximidad = new HashMap<String, java.io.Serializable>();
    private HashMap<String, java.io.Serializable> band_sensorDeLuz = new HashMap<String, java.io.Serializable>();
    private HashMap<String, java.io.Serializable> band_sensorDeAcelerometro = new HashMap<String, java.io.Serializable>();
    private HashMap<String, java.io.Serializable> band_sensorDeGiroscopio = new HashMap<String, java.io.Serializable>();
    private HashMap<String, java.io.Serializable> band_sensorMagnetometro = new HashMap<String, java.io.Serializable>();
    private HashMap<String, java.io.Serializable> band_sensorPodometro = new HashMap<String, java.io.Serializable>();
    private HashMap<String, java.io.Serializable> band_sensorCamara = new HashMap<String, java.io.Serializable>();
    private HashMap<String, java.io.Serializable> band_sensorGPS = new HashMap<String, java.io.Serializable>();
    private HashMap<String, java.io.Serializable> band_sensorBarometro = new HashMap<String, java.io.Serializable>();
    private HashMap<String, java.io.Serializable> band_sensorMicrofono = new HashMap<String, java.io.Serializable>();
    private HashMap<String, java.io.Serializable> band_sensorRitmoCardiaco = new HashMap<String, java.io.Serializable>();

    //Dialog Progress Horizontal Rigistrar Sensores
    private ProgressBar dialog_progressh_registrarSensor;
    private TextView dialog_tv_number_progress_registrarSensor,
            dialog_tv_cargando_registrarSensor;
    private int valorCargado = 1, valorACargar = 0;

    // Firebase
    private FirebaseFirestore db;
    private Map<String, java.io.Serializable> doc = new HashMap<String, java.io.Serializable>();
    private boolean bolRegistroCompletado = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registrar_sensor_smartphone);

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // Toolbar
        toolbar_logo = findViewById(R.id.toolbar_logo);
        toolbar_to_return = findViewById(R.id.toolbar_to_return);
        toolbar_title = findViewById(R.id.toolbar_title);
        toolbar_logo.setVisibility(View.GONE);
        toolbar_to_return.setVisibility(View.GONE);
        toolbar_title.setText("DataSensor");

        // Archivo de almacenamiento interno
        sharedPreferences = getSharedPreferences("ArchivoInfoApp_v1", MODE_PRIVATE);

        // Firebase
        db = FirebaseFirestore.getInstance();

        btn_Comenzar_registro_sensores = (Button) findViewById(R.id.btn_comenzar_registro_sensores);
        btn_Comenzar_registro_sensores.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                valorCargado = 0;
                valorACargar = 0;
                bolRegistroCompletado = false;

                ProgressRegistrarSensores();
            }
        });
    }

    private void ProgressRegistrarSensores() {

        AlertDialog.Builder builderProgressRegistrarS = new AlertDialog.Builder(RegistrarSensorSmartphoneActivity.this);

        LayoutInflater inflaterRS = getLayoutInflater();

        View viewRS = inflaterRS.inflate(R.layout.dialog_registrar_sensores_smartphone, null);

        builderProgressRegistrarS.setView(viewRS);
        builderProgressRegistrarS.setCancelable(false);

        AlertDialog dialogRS = builderProgressRegistrarS.create();
        if (dialogRS.getWindow() != null) {
            dialogRS.getWindow().setBackgroundDrawable(new ColorDrawable(0));
        }
        dialogRS.show();

        // Progress RegistrarSensor
        dialog_tv_number_progress_registrarSensor = viewRS.findViewById(R.id.dialog_tv_number_progress_registrarSensor);
        dialog_tv_cargando_registrarSensor = viewRS.findViewById(R.id.dialog_tv_cargando_registrarSensor);
        dialog_progressh_registrarSensor = viewRS.findViewById(R.id.dialog_progressh_registrarSensor);

        dialog_tv_cargando_registrarSensor.setText("Verificando...");
        dialog_tv_number_progress_registrarSensor.setText("0%");
        dialog_progressh_registrarSensor.setProgress(0);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (IsOnline(RegistrarSensorSmartphoneActivity.this)) {

                    numSerieSmartphone = Build.SERIAL;
                    android_id = Settings.Secure.getString(RegistrarSensorSmartphoneActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID);

                    //ID
                    idSmartphone = Build.MODEL + "-" + android_id + "-" + numSerieSmartphone;

                    doc.put("id", idSmartphone);
                    doc.put("modelo", Build.MODEL);
                    doc.put("fabricante", Build.MANUFACTURER);

                   dialog_tv_cargando_registrarSensor.setText("Buscando Sensores...");

                    editorInfoApp = sharedPreferences.edit();
                    ExistenSensores();

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {

                            if (valorCargado < valorACargar) {
                                valorCargado++;
                                dialog_progressh_registrarSensor.setProgress(valorCargado);
                                dialog_tv_number_progress_registrarSensor.setText(valorCargado + "%");
                            }

                            if (valorCargado < 100 || !bolRegistroCompletado) {
                                handler.postDelayed(this, 50);
                            } else if (valorCargado == 100 && bolRegistroCompletado) {
                                new Handler().postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        dialogRS.dismiss();
                                        finish();
                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                    }
                                }, 1500);
                            }
                        }
                    }, 50);

                    AddSensoresSmartphone();

                    dialog_tv_cargando_registrarSensor.setText("Cargando...");

                } else {
                    dialog_tv_cargando_registrarSensor.setText("Error de Conexión...");
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            dialogRS.dismiss();
                            mostrarDialogConexionRed();
                        }
                    }, 600);
                }
            }
        }, 1000);
    }

    @SuppressLint("UnsupportedChromeOsCameraSystemFeature")
    private void ExistenSensores() {
        //Registrando
        dialog_tv_cargando_registrarSensor.setText("Registrando...");

        // Sensor Manager
        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // sensores
        sensorDeProximidad = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorDeLuz = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorAcelerometro = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorDeGiroscopio = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorMagnetometro = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorPodometro = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        sensorBarometro = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
        sensorRitmoCardiaco = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);
        sensorTermometro = sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);//=>

        // Audio Manager
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        // Sensor Micrófono
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                sensorMicrofono = audioManager.getMicrophones();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                sensorMicrofonoDevice = audioManager.getDevices(AudioManager.GET_DEVICES_INPUTS);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        if(sensorAcelerometro == null){
            Log.e("Acelerometro: ", "No hay Sensor Acelerómetro");
            band_sensorDeAcelerometro.put("isExists", false);
            editorInfoApp.putBoolean("band_sensorDeAcelerometro", false);
        }
        else{
            Log.d("Acelerometro: ", "Hay Sensor Acelerómetro");
            band_sensorDeAcelerometro.put("isExists", true);
            editorInfoApp.putBoolean("band_sensorDeAcelerometro", true);
        }
        doc.put("sensorAcelerometro", band_sensorDeAcelerometro);
        valorACargar += 8;

        if(sensorDeProximidad == null){
            Log.e("De Proximidad: ", "No hay Sensor de Proximidad");
            band_sensorDeProximidad.put("isExists", false);
            editorInfoApp.putBoolean("band_sensorDeProximidad", false);
        }
        else{
            Log.d("De Proximidad: ", "Hay Sensor de Proximidad");
            band_sensorDeProximidad.put("isExists", true);
            editorInfoApp.putBoolean("band_sensorDeProximidad", true);
        }
        doc.put("sensorProximidad", band_sensorDeProximidad);
        valorACargar += 8;

        if(sensorDeLuz == null){
            Log.e("De Luz: ", "No hay Sensor de Luz");
            band_sensorDeLuz.put("isExists", false);
            editorInfoApp.putBoolean("band_sensorDeLuz", false);
        }
        else{
            Log.d("De Luz: ", "Hay Sensor de Luz");
            band_sensorDeLuz.put("isExists", true);
            editorInfoApp.putBoolean("band_sensorDeLuz", true);
        }
        doc.put("sensorLuz", band_sensorDeLuz);
        valorACargar += 8;

        if(sensorTermometro == null){
            Log.e("Termometro: ", "No hay sensor Termómetro");
            band_sensorTermometro.put("isExists", false);
            editorInfoApp.putBoolean("band_sensorTermometro", false);
        }
        else{
            Log.d("Termometro: ", "Hay sensor Termómetro");
            band_sensorTermometro.put("isExists", true);
            editorInfoApp.putBoolean("band_sensorTermometro", true);
        }
        doc.put("sensorTermometro", band_sensorTermometro);
        valorACargar += 8;

        if(sensorDeGiroscopio == null){
            Log.e("Giroscopio: ", "No hay sensor Giroscopio");
            band_sensorDeGiroscopio.put("isExists", false);
            editorInfoApp.putBoolean("band_sensorDeGiroscopio", false);
        }
        else{
            Log.d("Giroscopio: ", "Hay sensor Giroscopio");
            band_sensorDeGiroscopio.put("isExists", true);
            editorInfoApp.putBoolean("band_sensorDeGiroscopio", true);
        }
        doc.put("sensorGiroscopio", band_sensorDeGiroscopio);
        valorACargar += 8;

        if(sensorMagnetometro == null){
            Log.e("Magnetómetro: ", "No hay sensor Magnetómetro");
            band_sensorMagnetometro.put("isExists", false);
            editorInfoApp.putBoolean("band_sensorMagnetometro", false);
        }
        else{
            Log.d("Magnetómetro: ", "Hay sensor Magnetómetro");
            band_sensorMagnetometro.put("isExists", true);
            editorInfoApp.putBoolean("band_sensorMagnetometro", true);
        }
        doc.put("sensorMagnetometro", band_sensorMagnetometro);
        valorACargar += 8;

        if(sensorPodometro == null){
            Log.e("Podómetro: ", "No hay sensor Podómetro");
            band_sensorPodometro.put("isExists", false);
            editorInfoApp.putBoolean("band_sensorPodometro", false);
        }
        else{
            Log.d("Podómetro: ", "Hay sensor Podómetro");
            band_sensorPodometro.put("isExists", true);
            editorInfoApp.putBoolean("band_sensorPodometro", true);
        }
        doc.put("sensorPodometro", band_sensorPodometro);
        valorACargar += 8;

        if (RegistrarSensorSmartphoneActivity.this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Log.d("Cámara: ", "Hay sensor Cámara");
            band_sensorCamara.put("isExists", true);
            editorInfoApp.putBoolean("band_sensorCamara", true);
        }
        else {
            Log.e("Cámara: ", "No hay sensor Cámara");
            band_sensorCamara.put("isExists", false);
            editorInfoApp.putBoolean("band_sensorCamara", false);
        }
        doc.put("sensorCamara", band_sensorCamara);
        valorACargar += 8;

        if(sensorBarometro == null){
            Log.e("Barómetro: ", "No hay sensor Barómetro");
            band_sensorBarometro.put("isExists", false);
            editorInfoApp.putBoolean("band_sensorBarometro", false);
        }
        else{
            Log.d("Barómetro: ", "Hay sensor Barómetro");
            band_sensorBarometro.put("isExists", true);
            editorInfoApp.putBoolean("band_sensorBarometro", true);
        }
        doc.put("sensorBarometro", band_sensorBarometro);
        valorACargar += 8;

        if (RegistrarSensorSmartphoneActivity.this.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS)) {
            Log.d("GPS: ", "Hay sensor GPS");
            band_sensorGPS.put("isExists", true);
            editorInfoApp.putBoolean("band_sensorGPS", true);
        }
        else {
            Log.e("GPS: ", "No hay sensor GPS");
            band_sensorGPS.put("isExists", false);
            editorInfoApp.putBoolean("band_sensorGPS", false);
        }
        doc.put("sensorGPS", band_sensorGPS);
        valorACargar += 8;

        if(sensorMicrofono == null && sensorMicrofonoDevice == null){
            Log.e("Micrófono: ", "No hay sensor Micrófono");
            band_sensorMicrofono.put("isExists", false);
            editorInfoApp.putBoolean("band_sensorMicrofono", false);
        }
        else{
            Log.d("Micrófono: ", "Hay sensor Micrófono");
            band_sensorMicrofono.put("isExists", true);
            editorInfoApp.putBoolean("band_sensorMicrofono", true);
        }
        doc.put("sensorMicrofono", band_sensorMicrofono);
        valorACargar += 8;

        if(sensorRitmoCardiaco == null){
            Log.e("Ritmo cardíaco: ", "No hay sensor Ritmo cardíaco");
            band_sensorRitmoCardiaco.put("isExists", false);
            editorInfoApp.putBoolean("band_sensorRitmoCardiaco", false);
        }
        else{
            Log.d("Ritmo cardíaco: ", "Hay sensor Ritmo cardíaco");
            band_sensorRitmoCardiaco.put("isExists", true);
            editorInfoApp.putBoolean("band_sensorRitmoCardiaco", true);
        }
        doc.put("sensorRitmoCardiaco", band_sensorRitmoCardiaco);
        valorACargar += 8;
    }

    private boolean IsOnline(Context context) {
        Boolean bandIsOnline;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if ((networkInfo != null) && (networkInfo.isAvailable()) && (networkInfo.isConnected())) bandIsOnline = true;
        else bandIsOnline = false;


        return bandIsOnline;
    }

    private void mostrarDialogConexionRed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(RegistrarSensorSmartphoneActivity.this);

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

    private void AddSensoresSmartphone() {
        db.collection("SensoresSmartphones").document(idSmartphone).set(doc)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        editorInfoApp.putString("IdSmartphone", idSmartphone);
                        editorInfoApp.commit();
                        valorACargar += 4;
                        bolRegistroCompletado = true;
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("SensRegistrados Error", e.getMessage());
                    }
                });
    }
}
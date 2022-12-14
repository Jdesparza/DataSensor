package com.example.datasensor;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

public class SplashActivity extends AppCompatActivity {

    private SharedPreferences sharedPreferences;

    private String idSmartphone, numSerieSmartphone, android_id;
    private TextView tv_cargando_splash;

    private DocumentReference busquedaBD_ID;

    private static String dispositivoConInternet = "EnEspera";

    // Firebase
    FirebaseFirestore db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        sharedPreferences = getSharedPreferences("ArchivoInfoApp_v1", MODE_PRIVATE);
        idSmartphone = sharedPreferences.getString("IdSmartphone", "No hay modelo");

        tv_cargando_splash = findViewById(R.id.tv_cargando_splash);
        tv_cargando_splash.setText("Cargando...");

        if (!idSmartphone.equals("No hay modelo")) {
            TiempoEsperaActividades(true);
        } else {
            tv_cargando_splash.setText("Verificando...");
            IsOnlineTask isOnlineTask = new IsOnlineTask();
            isOnlineTask.execute();

            final Handler handler= new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (!dispositivoConInternet.equals("EnEspera")) {
                        EjecutarSinArchivoInfo();
                    } else {
                        handler.postDelayed(this,1500);
                    }
                }
            },1500);
        }
    }

    private void EjecutarSinArchivoInfo() {
        if (dispositivoConInternet.equals("ConInternet")) {
            BuscarDatosBDSmartphoneAlInicio();
        } else if (dispositivoConInternet.equals("SinInternet")){
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mostrarDialogConexionRed();
                }
            }, 1500);
        }
    }

    private void BuscarDatosBDSmartphoneAlInicio() {
        numSerieSmartphone = Build.SERIAL;
        android_id = Settings.Secure.getString(SplashActivity.this.getContentResolver(), Settings.Secure.ANDROID_ID);

        idSmartphone = Build.MODEL + "-" + android_id + "-" + numSerieSmartphone;

        tv_cargando_splash.setText("Cargando...");

        db = FirebaseFirestore.getInstance();
        busquedaBD_ID = db.collection("SensoresSmartphones").document(idSmartphone);
        busquedaBD_ID.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {

                        SharedPreferences.Editor editorInfoApp = sharedPreferences.edit();

                        editorInfoApp.putString("IdSmartphone", idSmartphone);
                        editorInfoApp.putBoolean("band_sensorTermometro",
                                (Boolean) ((HashMap) document.getData().get("sensorTermometro")).get("isExists"));
                        editorInfoApp.putBoolean("band_sensorDeProximidad",
                                (Boolean) ((HashMap) document.getData().get("sensorProximidad")).get("isExists"));
                        editorInfoApp.putBoolean("band_sensorDeLuz",
                                (Boolean) ((HashMap) document.getData().get("sensorLuz")).get("isExists"));
                        editorInfoApp.putBoolean("band_sensorDeAcelerometro",
                                (Boolean) ((HashMap) document.getData().get("sensorAcelerometro")).get("isExists"));
                        editorInfoApp.putBoolean("band_sensorDeGiroscopio",
                                (Boolean) ((HashMap) document.getData().get("sensorGiroscopio")).get("isExists"));
                        editorInfoApp.putBoolean("band_sensorMagnetometro",
                                (Boolean) ((HashMap) document.getData().get("sensorMagnetometro")).get("isExists"));
                        editorInfoApp.putBoolean("band_sensorPodometro",
                                (Boolean) ((HashMap) document.getData().get("sensorPodometro")).get("isExists"));
                        editorInfoApp.putBoolean("band_sensorCamara",
                                (Boolean) ((HashMap) document.getData().get("sensorCamara")).get("isExists"));
                        editorInfoApp.putBoolean("band_sensorBarometro",
                                (Boolean) ((HashMap) document.getData().get("sensorBarometro")).get("isExists"));
                        editorInfoApp.putBoolean("band_sensorGPS",
                                (Boolean) ((HashMap) document.getData().get("sensorGPS")).get("isExists"));
                        editorInfoApp.putBoolean("band_sensorMicrofono",
                                (Boolean) ((HashMap) document.getData().get("sensorMicrofono")).get("isExists"));
                        editorInfoApp.putBoolean("band_sensorRitmoCardiaco",
                                (Boolean) ((HashMap) document.getData().get("sensorRitmoCardiaco")).get("isExists"));


                        editorInfoApp.commit();

                        startActivity(new Intent(getApplicationContext(), MainActivity.class));

                    } else {
                        TiempoEsperaActividades(false);
                    }
                } else {
                    Log.e("ProblemaBD", "Error al buscar documento", task.getException());
                }
            }
        });
    }

    private void TiempoEsperaActividades(Boolean vista) {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (vista) {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                } else {
                    startActivity(new Intent(getApplicationContext(), RegistrarSensorSmartphoneActivity.class));
                }

                finish();
            }
        }, 2000);
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

    private void mostrarDialogConexionRed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

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
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_HOME);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            }
        });
    }


    private class IsOnlineTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            IsOnline(SplashActivity.this);

            return null;
        }
    }
}
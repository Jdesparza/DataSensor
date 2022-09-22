package com.example.datasensor.fragments;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.os.Handler;
import android.provider.Settings;
import android.text.Html;
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
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SensorGPSFragment extends Fragment {

    private Button btn_resultados_gps;
    private ResultadosFragment resultadosFragment = new ResultadosFragment();

    private CheckBox cb_gps_latitud, cb_gps_altitud,
            cb_gps_precision, cb_gps_longitud, cb_gps_velocidad;
    private CheckedTextView ctv_gps_calculo_1;
    private CheckedTextView ctv_gps_calculo_2;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private Location location_gps;
    private int conEvent = 0;

    private Context context;
    private String dispositivoConInternet = "EnEspera";

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
    HashMap<String, Serializable> ubicacion_1 = new HashMap<String, Serializable>();
    HashMap<String, Serializable> ubicacion_2 = new HashMap<String, Serializable>();
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
        View view = inflater.inflate(R.layout.fragment_sensor_g_p_s, container, false);

        // Toolbar
        ((MainActivity) getActivity()).BtnRegresarMostrar();
        ((MainActivity) getActivity()).LogoToolbarOcultar();
        ((MainActivity) getActivity()).TitleToolbar("Sensor GPS");

        //Sensor Title DB
        sensorDB = "sensorGPS";

        cb_gps_latitud = view.findViewById(R.id.cb_gps_latitud);
        cb_gps_altitud = view.findViewById(R.id.cb_gps_altitud);
        cb_gps_precision = view.findViewById(R.id.cb_gps_precision);
        cb_gps_longitud = view.findViewById(R.id.cb_gps_longitud);
        cb_gps_velocidad = view.findViewById(R.id.cb_gps_velocidad);
        ctv_gps_calculo_1 = view.findViewById(R.id.ctv_gps_calculo_1);
        ctv_gps_calculo_2 = view.findViewById(R.id.ctv_gps_calculo_2);

        locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            ActivarGPSDialog();
        }

        final Handler handler= new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                HabilitarDesabilitarBotonResult();
                handler.postDelayed(this,500);
            }
        },500);

        // Btn resultados
        btn_resultados_gps = view.findViewById(R.id.btn_resultados_gps);
        btn_resultados_gps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Conexión
                dispositivoConInternet = "EnEspera";

                //Traer IDSmartphone
                sharedPreferences = context.getSharedPreferences("ArchivoInfoApp_v1", context.MODE_PRIVATE);
                idSmartphone = sharedPreferences.getString("IdSmartphone", "No hay modelo");

                // Firebase
                db = FirebaseFirestore.getInstance();
                documentReference = db.collection("SensoresSmartphones").document(idSmartphone);

                conEvent = 0;

                DialogGuardarDatos();
            }
        });


        return view;
    }

    private void LocalizacionGPS() {
        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                if (conEvent == 0){
                    conEvent++;
                    location_gps = location;
                }
            }

            @Override
            public void onProviderEnabled(@NonNull String provider) {
            }

            @Override
            public void onProviderDisabled(@NonNull String provider) {
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {
            }
        };
        int permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 10, locationListener);
    }
    private void ValidarCheckBoxDatos(HashMap<String, Serializable> calculo, String key) {
        Log.i("Sensor", location_gps.getProvider());
        doc.put("proveedor", location_gps.getProvider());

        if (cb_gps_latitud.isChecked()) {
            calculo.put("latitud", location_gps.getLatitude());
            Log.e("Latitud", location_gps.getLatitude() + " º");
        }
        if (cb_gps_longitud.isChecked()) {
            calculo.put("longitud", location_gps.getLongitude());
            Log.e("Longitud", location_gps.getLongitude() + " º");
        }
        if (cb_gps_altitud.isChecked()) {
            calculo.put("altitud", location_gps.getAltitude());
            Log.e("Altitud", location_gps.getAltitude() + " m");
        }
        if (cb_gps_velocidad.isChecked()) {
            calculo.put("velocidad", location_gps.getSpeed());
            Log.e("Velocidad", location_gps.getSpeed() + " m/s");
        }
        if (cb_gps_precision.isChecked()) {
            calculo.put("precision", location_gps.getAccuracy());
            Log.e("Precisión", location_gps.getAccuracy() + " m");
        }
        doc.put(key, calculo);
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
                        LocalizacionGPS();

                        final Handler handler_1= new Handler();
                        handler_1.postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                if (conEvent > 0) {
                                    if (ctv_gps_calculo_1.isChecked()) {
                                        ValidarCheckBoxDatos(ubicacion_1, "ubicacion_1");
                                    } else if (ctv_gps_calculo_2.isChecked()) {
                                        ValidarCheckBoxDatos(ubicacion_2, "ubicacion_2");
                                    }

                                    IsRegisterDB();
                                } else {
                                    handler_1.postDelayed(this,500);
                                }
                            }
                        }, 500);

                    } else if (dispositivoConInternet.equals("SinInternet")) {
                        tv_dialog_subtitle_GD.setVisibility(View.VISIBLE);
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
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

    private void HabilitarDesabilitarBotonResult() {
        if (
                (cb_gps_latitud.isChecked() || cb_gps_longitud.isChecked() ||
                        cb_gps_velocidad.isChecked() || cb_gps_precision.isChecked() ||
                        cb_gps_altitud.isChecked()) &&
                        (!ctv_gps_calculo_1.isEnabled() && !ctv_gps_calculo_2.isEnabled())
        ) {
            CTVHabilitados();
        } else if (
                (!cb_gps_latitud.isChecked() && !cb_gps_longitud.isChecked() &&
                        !cb_gps_velocidad.isChecked() && !cb_gps_precision.isChecked() &&
                        !cb_gps_altitud.isChecked()) &&
                        (ctv_gps_calculo_1.isEnabled() && ctv_gps_calculo_2.isEnabled())
        ) {
            CTVDeshabilitados();
        }

        ctv_gps_calculo_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ctv_gps_calculo_1.isChecked() ) {
                    ctv_gps_calculo_1.setChecked(false);
                    ctv_gps_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                } else {
                    ctv_gps_calculo_1.setChecked(true);
                    ctv_gps_calculo_1.setBackgroundColor(Color.parseColor("#A8EA8C"));
                    if (ctv_gps_calculo_2.isChecked()) {
                        ctv_gps_calculo_2.setChecked(false);
                        ctv_gps_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    }
                }
            }
        });

        ctv_gps_calculo_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ctv_gps_calculo_2.isChecked() ) {
                    ctv_gps_calculo_2.setChecked(false);
                    ctv_gps_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                } else {
                    ctv_gps_calculo_2.setChecked(true);
                    ctv_gps_calculo_2.setBackgroundColor(Color.parseColor("#A8EA8C"));
                    if (ctv_gps_calculo_1.isChecked()) {
                        ctv_gps_calculo_1.setChecked(false);
                        ctv_gps_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    }
                }
            }
        });

        if ((ctv_gps_calculo_1.isChecked() || ctv_gps_calculo_2.isChecked()) && !btn_resultados_gps.isEnabled()) {
            BotonHabilitado();
        } else if ((!ctv_gps_calculo_1.isChecked() && !ctv_gps_calculo_2.isChecked()) && btn_resultados_gps.isEnabled()) {
            BotonDeshabilitado();
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
                    (!entry.getKey().toString().equals("ubicacion_1") && !entry.getKey().toString().equals("ubicacion_2")) &&
                    (!Objects.equals(docIsRegister.get(entry.getKey().toString()).toString(), doc.get(entry.getKey().toString()).toString()))) {
                isModificado = true;
                docIsRegister.put(entry.getKey().toString(), doc.get(entry.getKey().toString()));
                doc.remove(entry.getKey().toString());
            }
            else if ((doc.containsKey(entry.getKey().toString())) &&
                    (!entry.getKey().toString().equals("ubicacion_1") && !entry.getKey().toString().equals("ubicacion_2")) &&
                    (Objects.equals(docIsRegister.get(entry.getKey().toString()).toString(), doc.get(entry.getKey().toString()).toString()))) {
                doc.remove(entry.getKey().toString());
            } else if ((doc.containsKey(entry.getKey().toString()) &&
                    (entry.getKey().toString().equals("ubicacion_1") || entry.getKey().toString().equals("ubicacion_2")))
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

    private void CTVHabilitados() {
        Log.e("CTV", "Habilitados");
        ctv_gps_calculo_1.setEnabled(true);
        ctv_gps_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
        ctv_gps_calculo_1.setTextColor(ContextCompat.getColor(context, R.color.black));
        ctv_gps_calculo_2.setEnabled(true);
        ctv_gps_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
        ctv_gps_calculo_2.setTextColor(ContextCompat.getColor(context, R.color.black));
    }

    private void CTVDeshabilitados() {
        Log.e("CTV", "Desabilitados");
        ctv_gps_calculo_1.setEnabled(false);
        ctv_gps_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.gris_claro));
        ctv_gps_calculo_1.setTextColor(ContextCompat.getColor(context, R.color.gris_oscuro));
        ctv_gps_calculo_2.setEnabled(false);
        ctv_gps_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.gris_claro));
        ctv_gps_calculo_2.setTextColor(ContextCompat.getColor(context, R.color.gris_oscuro));
        if (ctv_gps_calculo_1.isChecked()) ctv_gps_calculo_1.setChecked(false);
        if (ctv_gps_calculo_2.isChecked()) ctv_gps_calculo_2.setChecked(false);
    }

    private void BotonHabilitado() {
        Log.e("Boton", "Habilitado");
        btn_resultados_gps.setTextColor(ContextCompat.getColor(context, R.color.black));
        btn_resultados_gps.setBackgroundColor(ContextCompat.getColor(context, R.color.celeste));
        btn_resultados_gps.setEnabled(true);
    }
    private void BotonDeshabilitado() {
        Log.e("Boton", "Deshabilitado");
        btn_resultados_gps.setTextColor(ContextCompat.getColor(context, R.color.gris_oscuro));
        btn_resultados_gps.setBackgroundColor(ContextCompat.getColor(context, R.color.gris_claro));
        btn_resultados_gps.setEnabled(false);
    }

    private void ActivarGPSDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        AlertDialog dialog = builder.setMessage(
                        Html.fromHtml("<font color='#000000'>El sistema GPS se encuentra deshabilitado.<br><br>¿Desea activar el GPS?</font>")
                )
                .setPositiveButton("Si", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        }
        dialog.setOnShowListener( new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(getResources().getColor(R.color.rosado_medio));
                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(getResources().getColor(R.color.rosado_medio));
            }
        });
        dialog.show();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (!dispositivoConInternet.equals("EnEspera")) locationManager.removeUpdates(locationListener);
    }

    private class IsOnlineTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            IsOnline(context);

            return null;
        }
    }
}
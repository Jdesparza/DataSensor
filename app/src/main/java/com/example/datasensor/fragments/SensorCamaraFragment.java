package com.example.datasensor.fragments;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.graphics.ImageFormat;
import android.graphics.Picture;
import android.graphics.PixelFormat;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.ColorDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
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
import android.util.Range;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.TextView;

import com.example.datasensor.MainActivity;
import com.example.datasensor.R;
import com.google.android.gms.common.images.ImageManager;
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

public class SensorCamaraFragment extends Fragment {

    private Button btn_resultados_camara;
    private ResultadosFragment resultadosFragment = new ResultadosFragment();

    private CheckBox cb_ancho_camara, cb_altura_camara, cb_resolucion_camara;

    private CheckedTextView ctv_camara_calculo_1, ctv_camara_calculo_2;

    private Context context;
    private String dispositivoConInternet;

    // Archivo App
    private SharedPreferences sharedPreferences;
    private String idSmartphone;

    // Firebase
    private FirebaseFirestore db;
    private DocumentReference documentReference;
    private Map<String, Serializable> doc = new HashMap<String, Serializable>();
    private Map<String, Serializable> docIsRegister = new HashMap<String, Serializable>();
    private String sensorDB;
    private boolean isModificado = false;

    public static final CameraCharacteristics.Key<Range[]> CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES = null;

    CameraManager cameraManager;
    String[] getCameraIdList = null;
    CameraCharacteristics cameraCharacteristics = null;
    StreamConfigurationMap streamConfigurationMap = null;
    Size[] sizes;
    Size maxResolucion = null;
    float maxResolucionMP = 0;

    // Dialog Guardar Datos
    AlertDialog dialogGD;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sensor_camara, container, false);

        // Toolbar
        ((MainActivity) getActivity()).BtnRegresarMostrar();
        ((MainActivity) getActivity()).LogoToolbarOcultar();
        ((MainActivity) getActivity()).TitleToolbar("Sensor Cámara");

        //Sensor Title DB
        sensorDB = "sensorCamara";

        // check Box camara
        ctv_camara_calculo_1 = view.findViewById(R.id.ctv_camara_calculo_1);
        ctv_camara_calculo_2 = view.findViewById(R.id.ctv_camara_calculo_2);

        //sensor
        cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);

        // boton Registrar
        btn_resultados_camara = view.findViewById(R.id.btn_resultados_camara);

        // Habilitar Calcular Pasos
        final Handler handler= new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                HabilitarDesabilitarBotonResult();
                handler.postDelayed(this,500);
            }
        },500);

        btn_resultados_camara.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ctv_camara_calculo_1.isChecked()) camaraId("1");
                if(ctv_camara_calculo_2.isChecked()) camaraId("0");
                GuardarDatos();
            }
        });

        return view;
    }

    private void HabilitarDesabilitarBotonResult() {
        ctv_camara_calculo_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ctv_camara_calculo_1.isChecked() ) {
                    ctv_camara_calculo_1.setChecked(false);
                    ctv_camara_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                } else {
                    ctv_camara_calculo_1.setChecked(true);
                    ctv_camara_calculo_1.setBackgroundColor(Color.parseColor("#A8EA8C"));
                    if (ctv_camara_calculo_2.isChecked()) {
                        ctv_camara_calculo_2.setChecked(false);
                        ctv_camara_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    }
                }
            }
        });

        ctv_camara_calculo_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ctv_camara_calculo_2.isChecked() ) {
                    ctv_camara_calculo_2.setChecked(false);
                    ctv_camara_calculo_2.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                } else {
                    ctv_camara_calculo_2.setChecked(true);
                    ctv_camara_calculo_2.setBackgroundColor(Color.parseColor("#A8EA8C"));
                    if (ctv_camara_calculo_1.isChecked()) {
                        ctv_camara_calculo_1.setChecked(false);
                        ctv_camara_calculo_1.setBackgroundColor(ContextCompat.getColor(context, R.color.white));
                    }
                }
            }
        });

        if ((ctv_camara_calculo_1.isChecked() || ctv_camara_calculo_2.isChecked()) &&
                !btn_resultados_camara.isEnabled()) {
            BotonHabilitado();
        } else if (!ctv_camara_calculo_1.isChecked() && !ctv_camara_calculo_2.isChecked() &&
                btn_resultados_camara.isEnabled()) {
            BotonDeshabilitado();
        }

    }
    private void BotonHabilitado() {
        btn_resultados_camara.setTextColor(ContextCompat.getColor(context, R.color.black));
        btn_resultados_camara.setBackgroundColor(ContextCompat.getColor(context, R.color.celeste));
        btn_resultados_camara.setEnabled(true);
    }
    private void BotonDeshabilitado() {
        btn_resultados_camara.setTextColor(ContextCompat.getColor(context, R.color.gris_oscuro));
        btn_resultados_camara.setBackgroundColor(ContextCompat.getColor(context, R.color.gris_claro));
        btn_resultados_camara.setEnabled(false);
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
        if (ctv_camara_calculo_1.isChecked()) {
            HashMap<String, Serializable> camFrontal = new HashMap<String, Serializable>();

            camFrontal.put("ancho", maxResolucion.getWidth());
            camFrontal.put("altura", maxResolucion.getHeight());
            camFrontal.put("resolucion", maxResolucionMP);
            doc.put("camFrontal", camFrontal);
            Log.e("camFrontal", String.valueOf(camFrontal));

        }
        if (ctv_camara_calculo_2.isChecked()) {
            HashMap<String, Serializable> camTrasera = new HashMap<String, Serializable>();

            camTrasera.put("ancho", maxResolucion.getWidth());
            camTrasera.put("altura", maxResolucion.getHeight());
            camTrasera.put("resolucion", maxResolucionMP);
            doc.put("camTrasera", camTrasera);
            Log.e("camTrasera", String.valueOf(camTrasera));
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
            if ((doc.containsKey(entry.getKey().toString()) &&
                    (entry.getKey().toString().equals("camFrontal") || entry.getKey().toString().equals("camTrasera")))
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

    private void camaraId(String idCamara) {
        try {
            getCameraIdList = cameraManager.getCameraIdList();
            cameraCharacteristics = cameraManager.getCameraCharacteristics(idCamara);
            streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            sizes = streamConfigurationMap.getOutputSizes(streamConfigurationMap.getOutputFormats()[0]);
            for (int i = 0; i < sizes.length; i++) {
                maxResolucion = sizes[i];
                maxResolucionMP = (sizes[i].getWidth() * sizes[i].getHeight()) / 1000000.0f;
                break;
            }
        } catch (CameraAccessException e) {
            Log.e("Error", "Error", e);
            e.printStackTrace();
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
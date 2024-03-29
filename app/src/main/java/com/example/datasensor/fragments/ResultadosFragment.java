package com.example.datasensor.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.example.datasensor.MainActivity;
import com.example.datasensor.R;
import com.example.datasensor.adapters.ItemInformacion_Resultados_RV_Adapter;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class ResultadosFragment extends Fragment {

    private Button btn_aceptar_regresar_sensores;
    private ScrollView sv_contenedor_resultados;
    private LinearLayout ll_progressb_resultados;
    private TextView tv_title_sensor_result;
    private ImageView iv_sensor_result;

    //DatoEnciado Fragment
    private Bundle datosEnviados;
    private String tipoSensor;
    private Map<String, Serializable> doc = new HashMap<String, Serializable>();

    private Context context;

    // Cargar Info
    private RecyclerView recyclerView;
    private ArrayList<String> tituloInfo = new ArrayList<String>();
    private ArrayList<String> resultadoInfo = new ArrayList<String>();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_resultados, container, false);

        // Toolbar
        ((MainActivity) getActivity()).LogoToolbarOcultar();
        ((MainActivity) getActivity()).BtnRegresarMostrar();
        ((MainActivity) getActivity()).TitleToolbar("Resultados");

        //Traer Sensor Enviado
        datosEnviados = getArguments();
        tipoSensor = datosEnviados.getString("tipoSensor");
        doc = (HashMap<String, Serializable>) datosEnviados.getSerializable("doc");

        sv_contenedor_resultados = view.findViewById(R.id.sv_contenedor_resultados);
        tv_title_sensor_result = view.findViewById(R.id.tv_title_sensor_result);
        iv_sensor_result = view.findViewById(R.id.iv_sensor_result);
        btn_aceptar_regresar_sensores = view.findViewById(R.id.btn_aceptar_regresar_sensores);
        ll_progressb_resultados = view.findViewById(R.id.ll_progressb_resultados);

        recyclerView = view.findViewById(R.id.rv_info_results);
        recyclerView.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false));

        ActualizarImgV();
        ActualizarDatos();
        ItemInformacion_Resultados_RV_Adapter adapter = new ItemInformacion_Resultados_RV_Adapter(tituloInfo, resultadoInfo);
        recyclerView.setAdapter(adapter);

        btn_aceptar_regresar_sensores.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).FragResultadosBackPressed();
            }
        });

        return view;
    }

    private void ActualizarDatos() {
        String nombre = "";
        String claveString = "";
        String valorString = "";
        TreeMap<String, Serializable> docSort = new TreeMap<>(doc);
        Map<String, Serializable> docTemp = new HashMap<String, Serializable>();
        docTemp.putAll(docSort);
        Map<String, Serializable> calculo;

        if (docTemp.containsKey("nombre")) {
            nombre = docTemp.get("nombre").toString();
        } else if (docTemp.containsKey("proveedor")) {
            nombre = docTemp.get("proveedor").toString().toUpperCase(Locale.ROOT);
        } else {
            nombre = tipoSensor.replace("sensor", "");
        }

        if (nombre.equals("Microfono")) {
            nombre = "Micrófono";
        } else if (nombre.equals("Camara")) {
            nombre = "Cámara";
        }
        tv_title_sensor_result.setText(nombre);

        for (Map.Entry mapDOC1 : docSort.entrySet()) {
            if (!mapDOC1.getKey().toString().equals("isExists") && !mapDOC1.getKey().toString().equals("nombre") &&
                    !mapDOC1.getKey().toString().equals("proveedor")) {
                if (!mapDOC1.getKey().toString().equals("presion_1") && !mapDOC1.getKey().toString().equals("presion_2") &&
                        !mapDOC1.getKey().toString().equals("ubicacion_1") && !mapDOC1.getKey().toString().equals("ubicacion_2") &&
                        !mapDOC1.getKey().toString().equals("calSonido_1") && !mapDOC1.getKey().toString().equals("calSonido_2") &&
                        !mapDOC1.getKey().toString().equals("aceleracion_1") && !mapDOC1.getKey().toString().equals("aceleracion_2") &&
                        !mapDOC1.getKey().toString().equals("rotacion_1") && !mapDOC1.getKey().toString().equals("rotacion_2") &&
                        !mapDOC1.getKey().toString().equals("magnetismo_1") && !mapDOC1.getKey().toString().equals("magnetismo_2") &&
                        !mapDOC1.getKey().toString().equals("camTrasera") && !mapDOC1.getKey().toString().equals("camFrontal")){
                    claveString = mapDOC1.getKey().toString();

                    if (claveString.equals("resolucion") || claveString.equals("potencia")
                            || claveString.equals("calRitmoCardiaco_1") || claveString.equals("calRitmoCardiaco_2")
                            || claveString.equals("rangoMax")) {
                        valorString = String.valueOf(new BigDecimal(Double.parseDouble(mapDOC1.getValue().toString()))
                                .setScale(5, RoundingMode.DOWN).doubleValue());
                    } else {
                        valorString = mapDOC1.getValue().toString();
                    }

                    if (claveString.equals("rangoMax")) {
                        claveString = "Rango Máx";
                    } else if (claveString.equals("version")) {
                        claveString = "Versión";
                    } else if (claveString.equals("resolucion")) {
                        claveString = "Resolución";
                    } else if (claveString.equals("proximidad_1") || claveString.equals("proximidad_2")) {
                        if (claveString.equals("proximidad_1")) claveString = "Prox. Corta";
                        else claveString = "Prox. Larga";
                    } else if (claveString.equals("iluminacion_1") || claveString.equals("iluminacion_2")) {
                        tituloInfo.add("Iluminación Espacio");
                        resultadoInfo.add("");
                        if (claveString.equals("iluminacion_1")) claveString = "Interno";
                        else claveString = "Externo";
                    } else if (claveString.equals("temperatura_1") || claveString.equals("temperatura_2")) {
                        tituloInfo.add("Temperatura");
                        resultadoInfo.add("");
                        if (claveString.equals("temperatura_1")) claveString = "Mañana";
                        else claveString = "Medio Día";
                    } else if (claveString.equals("calPasos_10") || claveString.equals("calPasos_15")) {
                        if (claveString.equals("calPasos_10")) claveString = "Dar 10 Pasos";
                        else claveString = "Dar 15 Pasos";
                    } else if (claveString.equals("calRitmoCardiaco_1") || claveString.equals("calRitmoCardiaco_2")) {
                        if (claveString.equals("calRitmoCardiaco_1")) claveString = "Al Correr";
                        else claveString = "Al Caminar";
                    } else {
                        claveString = mapDOC1.getKey().toString().substring(0, 1).toUpperCase(Locale.ROOT) +
                                mapDOC1.getKey().toString().substring(1).toLowerCase(Locale.ROOT);
                    }

                    claveString += ":";

                    tituloInfo.add(claveString);
                    resultadoInfo.add(valorString);
                    docTemp.remove(mapDOC1.getKey().toString());
                }
            } else {
                docTemp.remove(mapDOC1.getKey().toString());
            }
        }

        if (docSort.size() > 0) {
            for (Map.Entry entry : docTemp.entrySet()) {
                claveString = entry.getKey().toString();

                if (claveString.equals("aceleracion_1") || claveString.equals("aceleracion_2"))
                    if (claveString.equals("aceleracion_1")) claveString = "Dispos. Vertical";
                    else claveString = "Dispos. Horizontal";
                else if (claveString.equals("rotacion_1") || claveString.equals("rotacion_2"))
                    if (claveString.equals("rotacion_1")) claveString = "Dispos. Vertical";
                    else claveString = "Dispos. Horizontal";
                else if (claveString.equals("magnetismo_1") || claveString.equals("magnetismo_2"))
                    claveString = claveString.replace("magnetismo_", "Cálculo ");
                else if (claveString.equals("ubicacion_1") || claveString.equals("ubicacion_2"))
                    if (claveString.equals("ubicacion_1")) claveString = "La Cruz UTPL";
                    else claveString = "Canchas Deport. UTPL";
                else if (claveString.equals("presion_1") || claveString.equals("presion_2"))
                    if (claveString.equals("presion_1")) claveString = "Presión Mañana";
                    else claveString = "Presión Tarde";
                else if (claveString.equals("calSonido_1") || claveString.equals("calSonido_2"))
                    if (claveString.equals("calSonido_1")) claveString = "Cafetería UTPL";
                    else claveString = "Biblioteca UTPL";
                else if (claveString.equals("camFrontal") || claveString.equals("camTrasera"))
                    claveString = claveString.replace("cam", "Cám. ");

                tituloInfo.add(claveString);
                resultadoInfo.add("");

                calculo = (Map<String, Serializable>) entry.getValue();
                for (Map.Entry cal : calculo.entrySet()) {
                    if (!cal.getKey().toString().equals("canal") && cal.getValue() != null) {
                        valorString = String.valueOf(new BigDecimal(Double.parseDouble(cal.getValue().toString()))
                                .setScale(5, RoundingMode.DOWN).doubleValue());
                    } else {
                        valorString = cal.getValue().toString();
                    }

                    if (cal.getKey().toString().equals("presion")) {
                        claveString = "Presión";
                    } else if (cal.getKey().toString().equals("precision")) {
                        claveString = "Precisión";
                    } else if (cal.getKey().toString().equals("max")) {
                        claveString = "Máx";
                    } else if (cal.getKey().toString().equals("resolucion")) {
                        claveString = "Resolución (MP)";
                    } else {
                        claveString = cal.getKey().toString().substring(0, 1).toUpperCase(Locale.ROOT) +
                                cal.getKey().toString().substring(1).toLowerCase(Locale.ROOT);
                    }

                    claveString += ":";

                    tituloInfo.add(claveString);
                    resultadoInfo.add(valorString);
                }
            }
        }

        sv_contenedor_resultados.setVisibility(View.VISIBLE);
        ll_progressb_resultados.setVisibility(View.GONE);
        btn_aceptar_regresar_sensores.setVisibility(View.VISIBLE);
    }

    private void ActualizarImgV() {
        if (tipoSensor == "sensorTermometro") {
            iv_sensor_result.setImageResource(R.drawable.ic_termometro);
        } else if (tipoSensor == "sensorProximidad") {
            iv_sensor_result.setImageResource(R.drawable.ic_proximidad);
        } else if (tipoSensor == "sensorLuz") {
            iv_sensor_result.setImageResource(R.drawable.ic_luz);
        } else if (tipoSensor == "sensorAcelerometro") {
            iv_sensor_result.setImageResource(R.drawable.ic_acelerometro);
        } else if (tipoSensor == "sensorGiroscopio") {
            iv_sensor_result.setImageResource(R.drawable.ic_giroscopio);
        } else if (tipoSensor == "sensorMagnetometro") {
            iv_sensor_result.setImageResource(R.drawable.ic_magnetometro);
        } else if (tipoSensor == "sensorPodometro") {
            iv_sensor_result.setImageResource(R.drawable.ic_podometro);
        } else if (tipoSensor == "sensorCamara") {
            iv_sensor_result.setImageResource(R.drawable.ic_camara);
        } else if (tipoSensor == "sensorBarometro") {
            iv_sensor_result.setImageResource(R.drawable.ic_barometro);
        } else if (tipoSensor == "sensorGPS") {
            iv_sensor_result.setImageResource(R.drawable.ic_gps);
        } else if (tipoSensor == "sensorMicrofono") {
            iv_sensor_result.setImageResource(R.drawable.ic_microfono);
        } else if (tipoSensor == "sensorRitmoCardiaco") {
            iv_sensor_result.setImageResource(R.drawable.ic_ritmo_cardiaco);
        }
    }

}
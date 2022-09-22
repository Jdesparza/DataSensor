package com.example.datasensor.fragments;

import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.icu.util.Output;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.datasensor.MainActivity;
import com.example.datasensor.R;

import java.util.Arrays;

public class SensorCamaraFragment extends Fragment {

    public static final CameraCharacteristics.Key<Range[]> CONTROL_AE_AVAILABLE_TARGET_FPS_RANGES = null;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_sensor_camara, container, false);

        // Toolbar
        ((MainActivity) getActivity()).BtnRegresarMostrar();
        ((MainActivity) getActivity()).LogoToolbarOcultar();
        ((MainActivity) getActivity()).TitleToolbar("Sensor Cámara");

        TextView tv_resolucion = view.findViewById(R.id.tv_resolucion);

        CameraManager cameraManager = (CameraManager) getActivity().getSystemService(Context.CAMERA_SERVICE);
        String[] getCameraIdList = null;
        CameraCharacteristics cameraCharacteristics = null;
        StreamConfigurationMap streamConfigurationMap = null;
        Size[] sizes;
        Size maxResolucion = null;
        float maxResolucionMP = -1;
        StringBuilder cad = new StringBuilder();

        try {
            getCameraIdList = cameraManager.getCameraIdList();
            for (String cam: getCameraIdList) {
                cameraCharacteristics = cameraManager.getCameraCharacteristics(cam);
                streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                sizes = streamConfigurationMap.getOutputSizes(SurfaceTexture.class);
                //Log.e("", String.valueOf(streamConfigurationMap));
                //Log.e("getOutputFormats()", Arrays.toString(streamConfigurationMap.getOutputFormats()));
                for (int i = 0; i < sizes.length; i++) {
                    //Log.e("W", String.valueOf(sizes[i].getWidth()));
                    //Log.e("H", String.valueOf(sizes[i].getHeight()));
                    maxResolucion = sizes[i];
                    maxResolucionMP = (sizes[i].getWidth() * sizes[i].getHeight()) / 1000000.0f;
                    break;
                }
                if (cam.equals("0")) cad.append("Cámara Trasera:\n").append(maxResolucionMP).append(" MP (").append(maxResolucion).append(")\n\n");
                else if (cam.equals("1")) cad.append("Cámara Frontal:\n").append(maxResolucionMP).append(" MP (").append(maxResolucion).append(")\n");
            }
        } catch (CameraAccessException e) {
            Log.e("Error", "Error", e);
            e.printStackTrace();
        }

        tv_resolucion.setText(cad);

        return view;
    }
}
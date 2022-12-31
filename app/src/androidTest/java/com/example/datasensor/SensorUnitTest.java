package com.example.datasensor;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioDeviceInfo;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.MicrophoneInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Size;

import androidx.annotation.NonNull;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import junit.framework.TestCase;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

@RunWith(AndroidJUnit4.class)
public class SensorUnitTest extends TestCase {

    private static final int MAX_OFFICIAL_ANDROID_SENSOR_TYPE = 100;

    private Context context;
    private SensorManager mSensorManager;
    private AudioManager mAudioManager;
    private CameraManager mCameraManager;
    private NullSensorEventListener mNullSensorEventListener;

    @Before
    public void setUp() {
        context = InstrumentationRegistry.getInstrumentation().getContext();

        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        mNullSensorEventListener = new NullSensorEventListener();

        mAudioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);

        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    @After
    public void tearDown() {
        if (mSensorManager != null)
            mSensorManager.unregisterListener(mNullSensorEventListener);
    }

    @Test
    public void test_sensor_proximidad() {
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

        if (sensor != null) {
            System.out.println("Sensor: " +sensor.getName());
            assertSensorValues(sensor);
            assertSensorEventListener(sensor);
        } else {
            System.out.println("Sensor: NO EXISTE...");
        }
    }

    @Test
    public void test_sensor_luz() {
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);

        if (sensor != null) {
            System.out.println("Sensor: " +sensor.getName());
            assertSensorValues(sensor);
            assertSensorEventListener(sensor);
        } else {
            System.out.println("Sensor: NO EXISTE...");
        }
    }

    @Test
    public void test_sensor_acelerometro() {
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        if (sensor != null) {
            System.out.println("Sensor: " +sensor.getName());
            assertSensorValues(sensor);
            assertSensorEventListener(sensor);
        } else {
            System.out.println("Sensor: NO EXISTE...");
        }
    }

    @Test
    public void test_sensor_giroscopio() {
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);

        if (sensor != null) {
            System.out.println("Sensor: " +sensor.getName());
            assertSensorValues(sensor);
            assertSensorEventListener(sensor);
        } else {
            System.out.println("Sensor: NO EXISTE...");
        }
    }

    @Test
    public void test_sensor_magnetometro() {
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        if (sensor != null) {
            System.out.println("Sensor: " +sensor.getName());
            assertSensorValues(sensor);
            assertSensorEventListener(sensor);
        } else {
            System.out.println("Sensor: NO EXISTE...");
        }
    }

    @Test
    public void test_sensor_podometro() {
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (sensor != null) {
            System.out.println("Sensor: " +sensor.getName());
            assertSensorValues(sensor);
            assertSensorEventListener(sensor);
        } else {
            System.out.println("Sensor: NO EXISTE...");
        }
    }

    @Test
    public void test_sensor_barometro() {
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

        if (sensor != null) {
            System.out.println("Sensor: " +sensor.getName());
            assertSensorValues(sensor);
            assertSensorEventListener(sensor);
        } else {
            System.out.println("Sensor: NO EXISTE...");
        }
    }

    @Test
    public void test_sensor_ritmo_cardiaco() {
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE);

        if (sensor != null) {
            System.out.println("Sensor: " +sensor.getName());
            assertSensorValues(sensor);
            System.out.println(sensor.getType());
        } else {
            System.out.println("Sensor: NO EXISTE...");
        }
    }

    @Test
    public void test_sensor_termometro() {
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);

        if (sensor != null) {
            System.out.println("Sensor: " +sensor.getName());
            assertSensorValues(sensor);
            assertSensorEventListener(sensor);
        } else {
            System.out.println("Sensor: NO EXISTE...");
        }
    }

    @Test
    public void test_sensor_camara() {
        boolean hasCamara = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
        String[] getCameraIdList = null;

        try {
            getCameraIdList = mCameraManager.getCameraIdList();
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

        if (hasCamara) {
            System.out.println("Sensor: Cámara");
            assertEquals(2, getCameraIdList.length);
            assertSensorCameraValues(getCameraIdList[0]);
            assertSensorCameraValues(getCameraIdList[1]);
        } else {
            System.out.println("Sensor: NO EXISTE...");
        }
    }

    @Test
    public void test_sensor_GPS() {
        boolean hasCamara = context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS);

        if (hasCamara) {
            System.out.println("Sensor: GPS");
        } else {
            System.out.println("Sensor: NO EXISTE...");
        }
    }

    @Test
    public void test_sensor_microfono() {
        List<MicrophoneInfo>  sensorMicrofono = null;
        AudioDeviceInfo[] sensorMicrofonoDevice = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                sensorMicrofono = mAudioManager.getMicrophones();
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                sensorMicrofonoDevice = mAudioManager.getDevices(AudioManager.GET_DEVICES_INPUTS);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (sensorMicrofono == null) {
            System.out.println("Sensor: Micrófono");
            assertNotNull(sensorMicrofonoDevice);
        } else {
            assertNotNull(sensorMicrofono);
        }
    }

    private void assertSensorCameraValues(String idCamera) {
        CameraCharacteristics cameraCharacteristics;
        StreamConfigurationMap streamConfigurationMap;
        Size[] sizes;
        Float resolucion; // la resolución más alta de la cámara

        try {
            cameraCharacteristics = mCameraManager.getCameraCharacteristics(idCamera);
            assertNotNull("Debe contener caracteristicas de la cámara", cameraCharacteristics);

            streamConfigurationMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            assertNotNull("Deeben existir las diferentes configuraciones de la cámara", streamConfigurationMap);

            sizes = streamConfigurationMap.getOutputSizes(streamConfigurationMap.getOutputFormats()[0]);
            assertNotNull("Debemos obtener las resoluciones de la cámara", sizes);

            resolucion = (sizes[0].getWidth() * sizes[0].getHeight()) / 1000000.0f;
            assertNotNull(resolucion);
            assertEquals("Debe ser mayor", true, resolucion > (sizes[1].getWidth() * sizes[1].getHeight()) / 1000000.0f);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    private void assertSensorValues(Sensor sensor) {
        assertTrue("El rango máximo debe ser positivo. Rango Máx: " + sensor.getMaximumRange()
                , sensor.getMaximumRange() >= 0);
        assertTrue("La potencia máxima debe ser positiva. Potencia: " + sensor.getPower()
                , sensor.getPower() >= 0);

        if (sensor.getType() < MAX_OFFICIAL_ANDROID_SENSOR_TYPE) {
            assertTrue("La resolución máxima debe ser distinta de cero y positiva. Resolución: " + sensor.getResolution()
                    , sensor.getResolution() > 0);
        } else {
            assertTrue("La resolución máxima debe ser positiva. Resolución: " + sensor.getResolution()
                    , sensor.getResolution() >= 0);
        }

        assertNotNull("El nombre del proveedor no debe ser nulo. Fabricante: " + sensor.getVendor(), sensor.getVendor());
        assertTrue("La versión debe ser un número positivo. Versión: " + sensor.getVersion(), sensor.getVersion() > 0);
    }

    private void assertSensorEventListener(Sensor sensor) {
        boolean result = mSensorManager.registerListener(mNullSensorEventListener, sensor, SensorManager.SENSOR_DELAY_NORMAL);
        assertTrue(result);

    }

    private class NullSensorEventListener implements SensorEventListener {
        @Override
        public void onSensorChanged(SensorEvent event) {
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}
    }
}

package com.telenor.possumlib.detectortests;


import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.PowerManager;

import com.telenor.possumlib.JodaInit;

import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.shadows.ShadowApplication;

import java.io.File;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

/**
 * Basic class for sensor tests, incorporating mocking of sensorevent and other stuff
 */
public class GeneralSensorTest {
    boolean sensorIsEnabled = true;
    int storingData;

    @Mock
    protected Sensor mockedSensor;
    @Mock
    protected File mockedFile;
    @Mock
    protected Context mockedContext;
    @Mock
    protected PowerManager mockedPowerManager;
    @Mock
    protected SensorManager mockedSensorManager;
    @Mock
    protected AlarmManager mockedAlarmManager;

    public void setUp(int sensorType) throws Exception {
        MockitoAnnotations.initMocks(this);
        JodaInit.initializeJodaTime();
        when(mockedContext.getSystemService(Context.POWER_SERVICE)).thenReturn(mockedPowerManager);
        when(mockedContext.getSystemService(Context.ALARM_SERVICE)).thenReturn(mockedAlarmManager);
        when(mockedContext.getSystemService(Context.SENSOR_SERVICE)).thenReturn(mockedSensorManager);
        ShadowApplication.getInstance().grantPermissions(Manifest.permission.WAKE_LOCK);
        when(mockedContext.getPackageName()).thenReturn(RuntimeEnvironment.application.getPackageName());
        when(mockedSensor.getType()).thenReturn(sensorType);
        when(mockedSensorManager.getDefaultSensor(eq(sensorType))).thenReturn(mockedSensor);
        when(mockedContext.getFilesDir()).thenReturn(RuntimeEnvironment.application.getFilesDir());
        when(mockedContext.checkPermission(anyString(), anyInt(), anyInt())).thenReturn(PackageManager.PERMISSION_GRANTED);
    }

    public void tearDown() throws Exception {
        for (File file : RuntimeEnvironment.application.getFilesDir().listFiles()) {
            if (file.isDirectory()) {
                for (File actualFile : file.listFiles()) {
                    if (actualFile.exists() && actualFile.isFile()) {
                        if (!actualFile.delete()) {
                            throw new RuntimeException("Failed to delete leftover file");
                        }
                    }
                }
            }
        }
    }
}
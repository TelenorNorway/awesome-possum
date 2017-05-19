package com.telenor.possumlib;

import android.Manifest;
import android.os.Build;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.shadows.ShadowApplication;

import java.util.List;

@RunWith(PossumTestRunner.class)
public class PermissionsTest {

    @Test
    public void testHasCorrectPermissions() {
        AndroidManifest androidManifest = ShadowApplication.getInstance().getAppManifest();
        List<String> usedPermissions = androidManifest.getUsedPermissions();
        Assert.assertTrue(usedPermissions.contains(Manifest.permission.ACCESS_FINE_LOCATION));
        Assert.assertTrue(usedPermissions.contains(Manifest.permission.ACCESS_NETWORK_STATE));
        Assert.assertTrue(usedPermissions.contains(Manifest.permission.CHANGE_WIFI_STATE));
        Assert.assertTrue(usedPermissions.contains(Manifest.permission.ACCESS_WIFI_STATE));
        Assert.assertTrue(usedPermissions.contains(Manifest.permission.CAMERA));
        Assert.assertTrue(usedPermissions.contains(Manifest.permission.BLUETOOTH));
        Assert.assertTrue(usedPermissions.contains(Manifest.permission.BLUETOOTH_ADMIN));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            Assert.assertTrue(usedPermissions.contains(Manifest.permission.BODY_SENSORS));
        }
        Assert.assertTrue(usedPermissions.contains(Manifest.permission.RECORD_AUDIO));
        Assert.assertTrue(usedPermissions.contains(Manifest.permission.INTERNET));
        Assert.assertEquals(9, usedPermissions.size());
    }
}

package com.telenor.possumlib;


import android.support.annotation.NonNull;

import org.junit.runners.model.InitializationError;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.FileFsFile;
import org.robolectric.res.FsFile;
import org.robolectric.shadows.ShadowWifiManager;


@Config(shadows = {ShadowWifiManager.class})
public class PossumTestRunner extends RobolectricTestRunner {
    /**
     * Creates a runner to run {@code testClass}. Looks in your working directory for your AndroidManifest.xml file
     * and res directory by default. Use the {@link Config} annotation to configure.
     *
     * @param testClass the test class to be run
     * @throws InitializationError if junit says so
     */
    public PossumTestRunner(Class<?> testClass) throws InitializationError {
        super(testClass);
    }

    @NonNull
    @Override
    public InstrumentationConfiguration createClassLoaderConfig(Config config) {
        InstrumentationConfiguration.Builder builder = InstrumentationConfiguration.newBuilder();
        builder.addInstrumentedClass(ShadowWifiManager.class.getName());
        return builder.build();
    }

    @Override
    protected AndroidManifest getAppManifest(Config config) {
        AndroidManifest appManifest = super.getAppManifest(config);
        FsFile androidManifestFile = appManifest.getAndroidManifestFile();
        String moduleRoot = getModuleRootPath(config);

        if (androidManifestFile.exists()) {
            return appManifest;
        } else {
            androidManifestFile = FileFsFile.from(moduleRoot, "src/main/AndroidManifest.xml"); //appManifest.getAndroidManifestFile().getPath()
            FsFile resDirectory = FileFsFile.from(moduleRoot, "src/main/res");//appManifest.getAndroidManifestFile().getPath().replace("AndroidManifest.xml", "res"));
            FsFile assetsDirectory = FileFsFile.from(moduleRoot, "src/main/assets");//appManifest.getAndroidManifestFile().getPath().replace("AndroidManifest.xml", "assets"));
            return new AndroidManifest(androidManifestFile, resDirectory, assetsDirectory);
        }
    }
    private String getModuleRootPath(Config config) {
        String moduleRoot = config.constants().getResource("").toString().replace("file:", "").replace("jar:", "");
        return moduleRoot.substring(0, moduleRoot.indexOf("/build"));
    }

    @Override
    protected Config buildGlobalConfig() {
        Config.Builder builder = new Config.Builder();
        builder.setConstants(BuildConfig.class);
        builder.setSdk(19);
        return builder.build();
    }
}
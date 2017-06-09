package com.apkupdater.util.yalp;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ConfigurationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.github.yeriomin.playstoreapi.AndroidBuildProto;
import com.github.yeriomin.playstoreapi.AndroidCheckinProto;
import com.github.yeriomin.playstoreapi.AndroidCheckinRequest;
import com.github.yeriomin.playstoreapi.DeviceConfigurationProto;
import com.github.yeriomin.playstoreapi.DeviceInfoProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.TimeZone;

public class NativeDeviceInfoProvider implements DeviceInfoProvider {

    static private final int GOOGLE_SERVICES_VERSION_CODE = 10548448;

    private Context context;
    private String localeString;

    public void setContext(Context context) {
        this.context = context;
    }

    public void setLocaleString(String localeString) {
        this.localeString = localeString;
    }

    public int getSdkVersion() {
        return Build.VERSION.SDK_INT;
    }

    public String getUserAgentString() {
        return "Android-Finsky/7.1.15 ("
            + "api=3"
            + ",versionCode=" + getGsfVersionCode(context)
            + ",sdk=" + Build.VERSION.SDK_INT
            + ",device=" + Build.DEVICE
            + ",hardware=" + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? Build.HARDWARE : Build.PRODUCT)
            + ",product=" + Build.PRODUCT
            + ")"
            ;
    }

    public AndroidCheckinRequest generateAndroidCheckinRequest() {
        return AndroidCheckinRequest
            .newBuilder()
            .setId(0)
            .setCheckin(getCheckinProto())
            .setLocale(this.localeString)
            .setTimeZone(TimeZone.getDefault().getID())
            .setVersion(3)
            .setDeviceConfiguration(getDeviceConfigurationProto())
            .setFragment(0)
            .build()
            ;
    }

    private AndroidCheckinProto getCheckinProto() {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return AndroidCheckinProto.newBuilder()
            .setBuild(getBuildProto())
            .setLastCheckinMsec(0)
            .setCellOperator(tm.getNetworkOperator())
            .setSimOperator(tm.getSimOperator())
            .setRoaming("mobile-notroaming")
            .setUserNumber(0)
            .build()
            ;
    }

    private AndroidBuildProto getBuildProto() {
        return AndroidBuildProto.newBuilder()
            .setId(Build.FINGERPRINT)
            .setProduct(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? Build.HARDWARE : Build.PRODUCT)
            .setCarrier(Build.BRAND)
            .setRadio(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? Build.RADIO : Build.MODEL)
            .setBootloader(Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO ? Build.BOOTLOADER : Build.MODEL)
            .setDevice(Build.DEVICE)
            .setSdkVersion(Build.VERSION.SDK_INT)
            .setModel(Build.MODEL)
            .setManufacturer(Build.MANUFACTURER)
            .setBuildProduct(Build.PRODUCT)
            .setClient("android-google")
            .setOtaInstalled(false)
            .setTimestamp(System.currentTimeMillis() / 1000)
            .setGoogleServices(getGsfVersionCode(context))
            .build()
            ;
    }

    public DeviceConfigurationProto getDeviceConfigurationProto() {
        DeviceConfigurationProto.Builder builder = DeviceConfigurationProto.newBuilder();
        addDisplayMetrics(builder);
        addConfiguration(builder);
        ConfigurationInfo configurationInfo = ((ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE)).getDeviceConfigurationInfo();
        return builder
            .addAllNativePlatform(getPlatforms())
            .addAllSystemSharedLibrary(getSharedLibraries(context))
            .addAllSystemAvailableFeature(getFeatures(context))
            .addAllSystemSupportedLocale(getLocales(context))
            .setGlEsVersion(configurationInfo.reqGlEsVersion)
            .addAllGlExtension(EglExtensionRetriever.getEglExtensions())
            .build()
            ;
    }

    private DeviceConfigurationProto.Builder addDisplayMetrics(DeviceConfigurationProto.Builder builder) {
        DisplayMetrics metrics = this.context.getResources().getDisplayMetrics();
        builder
            .setScreenDensity((int) (metrics.density * 160f))
            .setScreenWidth(metrics.widthPixels)
            .setScreenHeight(metrics.heightPixels)
        ;
        return builder;
    }

    private DeviceConfigurationProto.Builder addConfiguration(DeviceConfigurationProto.Builder builder) {
        Configuration config = this.context.getResources().getConfiguration();
        builder
            .setTouchScreen(config.touchscreen)
            .setKeyboard(config.keyboard)
            .setNavigation(config.navigation)
            .setScreenLayout(config.screenLayout & 15)
            .setHasHardKeyboard(config.keyboard == Configuration.KEYBOARD_QWERTY)
            .setHasFiveWayNavigation(config.navigation == Configuration.NAVIGATIONHIDDEN_YES)
        ;
        return builder;
    }

    static public List<String> getPlatforms() {
        List<String> platforms = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= 21) {
            platforms = Arrays.asList(Build.SUPPORTED_ABIS);
        } else {
            if (!TextUtils.isEmpty(Build.CPU_ABI)) {
                platforms.add(Build.CPU_ABI);
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO && !TextUtils.isEmpty(Build.CPU_ABI2)) {
                platforms.add(Build.CPU_ABI2);
            }
        }
        return platforms;
    }

    static public List<String> getFeatures(Context context) {
        PackageManager packageManager = context.getPackageManager();
        FeatureInfo[] featuresList = packageManager.getSystemAvailableFeatures();
        List<String> featureStringList = new ArrayList<>();
        for (FeatureInfo feature: featuresList) {
            if (!TextUtils.isEmpty(feature.name)) {
                featureStringList.add(feature.name);
            }
        }
        return featureStringList;
    }

    static public List<String> getLocales(Context context) {
        List<String> locales = new ArrayList<>();
        for (String locale: context.getAssets().getLocales()) {
            if (TextUtils.isEmpty(locale)) {
                continue;
            }
            locales.add(locale.replace("-", "_"));
        }
        Collections.sort(locales);
        return locales;
    }

    static public List<String> getSharedLibraries(Context context) {
        return Arrays.asList(context.getPackageManager().getSystemSharedLibraryNames());
    }

    static public int getGsfVersionCode(Context context) {
        try {
            int versionCode = context.getPackageManager().getPackageInfo("com.google.android.gms", 0).versionCode;
            return versionCode > GOOGLE_SERVICES_VERSION_CODE ? versionCode : GOOGLE_SERVICES_VERSION_CODE;
        } catch (PackageManager.NameNotFoundException e) {
            return GOOGLE_SERVICES_VERSION_CODE;
        }
    }
}
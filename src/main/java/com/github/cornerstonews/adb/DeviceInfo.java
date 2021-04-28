package com.github.cornerstonews.adb;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.TimeoutException;

public class DeviceInfo {
    private static final Logger LOG = LogManager.getLogger(DeviceInfo.class);

    private static final String PROP_PRODUCT_DEVICE = "ro.product.product.device";
    private static final String PROP_SIM_STATE = "gsm.sim.state";
    private static final String PROP_SIM_OPERATOR = "gsm.operator.alpha";
    private static final String PROP_GSM_NETWORK_TYPE = "gsm.network.type";

    // How to use use service call command
    // https://android.stackexchange.com/questions/34625/where-to-find-description-of-all-system-bin-service-calls

    // https://android.googlesource.com/platform/frameworks/base/+/master/telephony/java/com/android/internal/telephony/IPhoneSubInfo.aidl
    // https://android.googlesource.com/platform/frameworks/opt/telephony/+/master/src/java/com/android/internal/telephony/PhoneSubInfoController.java

    private static final String CMD_GET_IMEI = "service call iphonesubinfo 2 | cut -c 52-66 | tr -d '.[:space:]'";
    private static final String CMD_GET_IMEI_ANDROID_8_to_10 = "service call iphonesubinfo 1 | cut -c 52-66 | tr -d '.[:space:]'";

    private static final String CMD_GET_IMSI = "service call iphonesubinfo 9 | cut -c 52-66 | tr -d '.[:space:]'";
    private static final String CMD_GET_IMSI_ANDROID_8_to_10 = "service call iphonesubinfo 7 | cut -c 52-66 | tr -d '.[:space:]'";

    // getIccSerialNumberWithFeature()
    private static final String CMD_GET_ICCID = "service call iphonesubinfo 13 | cut -c 52-66 | tr -d '.[:space:]'";
    // getIccSerialNumber() or getIccSerialNumberForSubscriber()
    private static final String CMD_GET_ICCID_ANDROID_8_to_10 = "service call iphonesubinfo 11 | cut -c 52-66 | tr -d '.[:space:]'";
    // getIccSerialNumber()
//    private static final String CMD_GET_ICCID_ANDROID_8 = "service call iphonesubinfo 11 | cut -c 52-66 | tr -d '.[:space:]'";

    private static final String CMD_GET_NUMBER = "service call iphonesubinfo 16 | cut -c 52-66 | tr -d '.[:space:]'";
    private static final String CMD_GET_NUMBER_ANDROID_9_to_10 = "service call iphonesubinfo 13 | cut -c 52-66 | tr -d '.[:space:]'";
    private static final String CMD_GET_NUMBER_ANDROID_8 = "service call iphonesubinfo 14 | cut -c 52-66 | tr -d '.[:space:]'";

    private static final String CMD_GET_WIFI_ON = "settings get global wifi_on";
    private static final String CMD_GET_MOBILE_DATA = "settings get global mobile_data";

    private AdbExecutor adbExecutor;
    private IDevice adbDevice;
    private BatteryFetcher batteryFetcher;

    private DeviceDO deviceDO;

    public DeviceInfo(AdbExecutor adbExcutor, IDevice device) {
        this.adbExecutor = adbExcutor;
        this.adbDevice = device;
        this.batteryFetcher = new BatteryFetcher(device);
    }

    // -----------------------------------------------------------------------
    // Device Info
    // -----------------------------------------------------------------------
    public DeviceDO getDeviceInfo() {
        if (this.deviceDO == null) {
            this.deviceDO = new DeviceDO();

            // Cache data that do not change frequently
            deviceDO.setSerialNumber(this.adbDevice.getSerialNumber());
            deviceDO.setAndroidVersion(this.getAndroidVersion());
            deviceDO.setManufacturer(this.getProductManufacturer());
            deviceDO.setModel(this.getProductModel());
            deviceDO.setProduct(this.getProductDevice());
        }

        deviceDO.setState(this.getState());
        deviceDO.setIsOnline(this.adbDevice.isOnline());

        if (this.adbDevice.isOnline()) {
            deviceDO.setBatteryLevel(this.getBatteryLevel());
            deviceDO.setBatteryHealth(this.getBatteryHealth());
            deviceDO.setBatteryTemperature(this.getBatteryTemperature());

            deviceDO.setNetwork(this.getNetwork());
            deviceDO.setWifiEnabled(this.isWifiEnabled());
            deviceDO.setIsSimPresent(this.isSimPresent());
            deviceDO.setICCID(this.getICCID());
            deviceDO.setIMSI(this.getIMSI());
            deviceDO.setIMEI(this.getIMEI());
            deviceDO.setPhoneNumber(this.getPhoneNumber());
            deviceDO.setSimOperator(this.getSimOperator());
            deviceDO.setRssi(this.getRssi());
            deviceDO.setMobileDataEnabled(this.isMobileDataEnabled());
            deviceDO.setMobileDataType(this.getMobileDataType());
        }

        return deviceDO;
    }

    public String getState() {
        return this.adbDevice.getState().name().toLowerCase();
    }

    public boolean isOnline() {
        return this.adbDevice.isOnline();
    }

    public boolean isOffline() {
        return this.adbDevice.isOffline();
    }

    public String getAndroidVersion() {
//        return this.executeShellCommand("getprop ro.build.version.release);
        if (this.deviceDO == null || this.deviceDO.getAndroidVersion() == null) {
            return this.getProperty(IDevice.PROP_BUILD_VERSION);
        }
        return this.deviceDO.getAndroidVersion();
    }

    public String getProductManufacturer() {
        return this.getProperty(IDevice.PROP_DEVICE_MANUFACTURER);
    }

    public String getProductModel() {
        return this.getProperty(IDevice.PROP_DEVICE_MODEL);
    }

    public String getProductDevice() {
        return this.getProperty(PROP_PRODUCT_DEVICE);
    }

    public Integer getBatteryLevel() {
        try {
            return this.adbDevice.getBattery(60, TimeUnit.SECONDS).get();
        } catch (InterruptedException | ExecutionException e) {
            LOG.debug("Error getting battery level for device: {}", this.adbDevice.getSerialNumber(), e);
            return null;
        }
    }

    public String getBatteryHealth() {
        // use default of 5 minutes
        Future<String> futureBattery = batteryFetcher.getBatteryHealth(5 * 60 * 1000, TimeUnit.MILLISECONDS);
        try {
            return futureBattery.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

    public Double getBatteryTemperature() {
        // use default of 5 minutes
        Future<Double> futureBattery = batteryFetcher.getBatteryTemperature(5 * 60 * 1000, TimeUnit.MILLISECONDS);
        try {
            return futureBattery.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

    public String getNetwork() {
        // TODO
        return null;
    }

    public Boolean isWifiEnabled() {
        Boolean wifiEnabled = false;
        if (!isOnline()) {
            return wifiEnabled;
        }
        String wifiOn = this.getDataFromShellCommand("WIFI_STATUS", CMD_GET_WIFI_ON);
        if ("1".equalsIgnoreCase(wifiOn)) {
            wifiEnabled = true;
        }
        return wifiEnabled;
    }

    public Boolean isSimPresent() {
        // https://android.googlesource.com/platform/frameworks/base.git/+/master/telephony/java/com/android/internal/telephony/IccCardConstants.java
        boolean isPresent = false;

        String rawSimState = getSimState();
        String[] simState = rawSimState.split(",");

        for (String state : simState) {
            switch (state.toUpperCase()) {
            case "PIN_REQUIRED":
            case "PUK_REQUIRED":
            case "NETWORK_LOCKED":
            case "READY":
            case "NOT_READY":
            case "PERM_DISABLED":
            case "CARD_RESTRICTED":
            case "LOADED":
                isPresent = true;
                break;

            case "ABSENT":
            case "UNKNOWN":
            case "CARD_IO_ERROR":
            default:
                isPresent = isPresent ? true : false;
            }
        }
        return isPresent;
    }

    public String getSimState() {
        if (!isOnline()) {
            return null;
        }

        return this.getProperty(PROP_SIM_STATE);
    }

    private int getMajorVersion() {
        return Integer.parseInt(String.valueOf(getAndroidVersion().split("\\.")[0]));
    }

    public String getIMEI() {
        if (!isOnline()) {
            return null;
        }

        if (getMajorVersion() <= 10) {
            return this.getDataFromShellCommand("IMEI", CMD_GET_IMEI_ANDROID_8_to_10);
        }
        return this.getDataFromShellCommand("IMEI", CMD_GET_IMEI);
    }

    public String getIMSI() {
        if (!isOnline()) {
            return null;
        }

        if (getMajorVersion() <= 10) {
            return this.getDataFromShellCommand("IMSI", CMD_GET_IMSI_ANDROID_8_to_10);
        }
        return this.getDataFromShellCommand("IMSI", CMD_GET_IMSI);
    }

    public String getICCID() {
        if (!isOnline()) {
            return null;
        }

//        if (getMajorVersion() == 8) {
//            return this.getDataFromShellCommand("ICCID", CMD_GET_ICCID_ANDROID_8);
//        } else 
        if (getMajorVersion() <= 10) {
            return this.getDataFromShellCommand("ICCID", CMD_GET_ICCID_ANDROID_8_to_10);
        }

        return this.getDataFromShellCommand("ICCID", CMD_GET_ICCID);
    }

    public String getPhoneNumber() {
        if (!isOnline()) {
            return null;
        }

        String phoneNumber = null;
        if (getMajorVersion() == 8) {
            phoneNumber = this.getDataFromShellCommand("Phone Number", CMD_GET_NUMBER_ANDROID_8);
        } else if (getMajorVersion() <= 10) {
            phoneNumber = this.getDataFromShellCommand("Phone Number", CMD_GET_NUMBER_ANDROID_9_to_10);
        } else {
            phoneNumber = this.getDataFromShellCommand("Phone Number", CMD_GET_NUMBER);
        }

        if (phoneNumber != null && !phoneNumber.isBlank()) {
            phoneNumber = phoneNumber.replaceAll("[^\\d]", "");
        }

        return (phoneNumber == null || phoneNumber.isBlank()) ? null : phoneNumber;
    }

    public String getSimOperator() {
        return this.getProperty(PROP_SIM_OPERATOR);
    }

    public Integer getRssi() {
        // TODO
        return null;
    }

    public Boolean isMobileDataEnabled() {
        Boolean mobileDataEnabled = false;
        if (!isOnline()) {
            return mobileDataEnabled;
        }
        String mobileDataOn = this.getDataFromShellCommand("MOBILE_DATA_STATUS", CMD_GET_MOBILE_DATA);
        if ("1".equalsIgnoreCase(mobileDataOn)) {
            mobileDataEnabled = true;
        }
        return mobileDataEnabled;
    }

    public String getMobileDataType() {
        if (!isOnline()) {
            return null;
        }

        return this.getProperty(PROP_GSM_NETWORK_TYPE);
    }

    private String getDataFromShellCommand(String cmdName, String cmd) {
        try {
            LOG.info("Getting '{}' for device: {}", cmdName, this.adbDevice.getSerialNumber());
            String result = this.adbExecutor.executeShellCommand(cmd);
            if (result != null) {
                result = result.trim();
            }
            LOG.debug("Result of command '{}' for device '{}, Result: '{}'", cmdName, this.adbDevice.getSerialNumber(), result);
            return result;
        } catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
            LOG.debug("Error getting '{}' for device: {}", cmdName, this.adbDevice.getSerialNumber(), e);
            return null;
        }
    }

    private String getProperty(String property) {
        try {
            LOG.info("Getting property '{}' for device: {}", property, this.adbDevice.getSerialNumber());
            String propResult = this.adbDevice.getSystemProperty(property).get();
            LOG.debug("Result of property '{}' for device '{}, Result: '{}'", property, this.adbDevice.getSerialNumber(), propResult);
            return (propResult == null || propResult.isBlank()) ? null : propResult;
        } catch (InterruptedException | ExecutionException e) {
            LOG.debug("Error getting property '{}' for device {}: ", property, this.adbDevice.getSerialNumber(), e);
            return null;
        }
    }
}

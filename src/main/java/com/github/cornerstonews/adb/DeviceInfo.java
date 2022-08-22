package com.github.cornerstonews.adb;

import java.io.IOException;
import java.util.Map;
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

    private AdbExecutor adbExecutor;
    private IDevice adbDevice;
    private BatteryFetcher batteryFetcher;

    private Map<String, String> commands;
    private DeviceDO deviceDO = new DeviceDO();

    public DeviceInfo(AdbExecutor adbExecutor, IDevice device) {
        this.adbExecutor = adbExecutor;
        this.adbDevice = device;

        this.batteryFetcher = new BatteryFetcher(device);
        setBasicDeviceInfo();
        commands = DeviceInfoAdbCommands.getCommands(this.deviceDO.getApiLevel());
        setFetchOnlyOnceData();
    }

    public DeviceDO getInfo() {
        deviceDO.setState(this.getState());
        deviceDO.setOnline(this.adbDevice.isOnline());

        if (this.adbDevice.isOnline()) {
            deviceDO.setBatteryLevel(this.getBatteryLevel());
            deviceDO.setBatteryHealth(this.getBatteryHealth());
            deviceDO.setBatteryTemperature(this.getBatteryTemperature());

            deviceDO.setWifiEnabled(this.isWifiEnabled());
            deviceDO.setSimPresent(this.isSimPresent());
            deviceDO.setICCID(this.getICCID());
            deviceDO.setIMSI(this.getIMSI());
            deviceDO.setIMEI(this.getIMEI());
            deviceDO.setPhoneNumber(this.getPhoneNumber());
            deviceDO.setSimOperator(this.getSimOperator());
            deviceDO.setRssi(this.getRssi());
            deviceDO.setMobileDataEnabled(this.isMobileDataEnabled());
            deviceDO.setMobileDataType(this.getMobileDataType());
            deviceDO.setAirplaneModeOn(this.isAirplaneModeOn());
            deviceDO.setBluetoothOn(this.isBluetoothOn());
            deviceDO.setNfcOn(this.isNFCOn());
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

    private void setBasicDeviceInfo() {
        // Cache data that does not change or change frequently
        this.deviceDO.setSerialNumber(this.adbDevice.getSerialNumber());
        this.deviceDO.setApiLevel(this.adbDevice.getVersion().getApiLevel());
        this.deviceDO.setAndroidVersion(this.getProperty(IDevice.PROP_BUILD_VERSION));
        this.deviceDO.setManufacturer(this.getProperty(IDevice.PROP_DEVICE_MANUFACTURER));
        this.deviceDO.setModel(this.getProperty(IDevice.PROP_DEVICE_MODEL));
        this.deviceDO.setProduct(this.getProperty(DeviceInfoAdbCommands.PROP_DEVICE_NAME));
    }

    private void setFetchOnlyOnceData() {
        if (this.deviceDO.getApiLevel() >= 32) {
            cachePhoneInfoWithDialerApp();
        }
    }

    private void cachePhoneInfoWithDialerApp() {
        this.getDataFromShellCommand("GET_SIM_INFO_FROM_DIALER", DeviceInfoAdbCommands.CMD_DIALER_DEVICE_INFO);
    }

    private Integer getBatteryLevel() {
        // Fetch every 1 minutes, The battery level may be cached.
        // Only queries the device for its battery level if 1 minutes have expired since the last successful query.
        Future<Integer> futureBattery = this.adbDevice.getBattery(60 * 1000, TimeUnit.MILLISECONDS);
        try {
            return futureBattery.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

    private String getBatteryHealth() {
        // use default of 5 minutes
        Future<String> futureBattery = batteryFetcher.getBatteryHealth(5 * 60 * 1000, TimeUnit.MILLISECONDS);
        try {
            return futureBattery.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

    private Double getBatteryTemperature() {
        // use default of 5 minutes
        Future<Double> futureBattery = batteryFetcher.getBatteryTemperature(5 * 60 * 1000, TimeUnit.MILLISECONDS);
        try {
            return futureBattery.get();
        } catch (InterruptedException | ExecutionException e) {
            return null;
        }
    }

    private Boolean isWifiEnabled() {
        String wifiStatus = this.getDataFromShellCommand("WIFI_STATUS", this.commands.get("CMD_GET_WIFI_ON"));
        if ("1".equalsIgnoreCase(wifiStatus) || "2".equalsIgnoreCase(wifiStatus)) {
            return true;
        }
        return false;
    }

    private boolean isNFCOn() {
        if (this.getDataFromShellCommand("NFC_STATUS", this.commands.get("CMD_GET_NFC_STATUS")).contains("mState=on")) {
            return true;
        }
        return false;
    }

    private boolean isBluetoothOn() {
        if ("1".equalsIgnoreCase(this.getDataFromShellCommand("BLUETOOTH_STATUS", this.commands.get("CMD_GET_BLUETOOTH_ON")))) {
            return true;
        }
        return false;
    }

    private boolean isAirplaneModeOn() {
        if ("1".equalsIgnoreCase(this.getDataFromShellCommand("AIRPLANE_MODE_STATUS", this.commands.get("CMD_GET_AIRPLANE_MODE")))) {
            return true;
        }
        return false;
    }

    private Boolean isSimPresent() {
        // https://android.googlesource.com/platform/frameworks/base.git/+/master/telephony/java/com/android/internal/telephony/IccCardConstants.java
        boolean isPresent = false;

        String rawSimState = this.getProperty(this.commands.get("PROP_SIM_STATE"));
        if (rawSimState != null) {
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
        }
        return isPresent;
    }

    private String getIMEI() {
        return this.getDataFromShellCommand("IMEI", this.commands.get("CMD_GET_IMEI"));
    }

    private String getIMSI() {
        return this.getDataFromShellCommand("IMSI", this.commands.get("CMD_GET_IMSI"));
    }

    private String getICCID() {
        return this.getDataFromShellCommand("ICCID", this.commands.get("CMD_GET_ICCID"));
    }

    private String getPhoneNumber() {
        String phoneNumber = this.getDataFromShellCommand("Phone Number", this.commands.get("CMD_GET_NUMBER"));
        if (phoneNumber != null && !phoneNumber.isBlank()) {
            phoneNumber = phoneNumber.replaceAll("[^\\d]", "");
        }
        return (phoneNumber == null || phoneNumber.isBlank()) ? null : phoneNumber;
    }
    
    private String getSimOperator() {
        return this.getProperty(this.commands.get("PROP_SIM_OPERATOR"));
    }

    private Integer getRssi() {
        // TODO
        return null;
    }

    private Boolean isMobileDataEnabled() {
        Boolean mobileDataEnabled = false;
        if (!isOnline()) {
            return mobileDataEnabled;
        }
        String mobileDataOn = this.getDataFromShellCommand("MOBILE_DATA_STATUS", this.commands.get("CMD_GET_MOBILE_DATA"));
        if ("1".equalsIgnoreCase(mobileDataOn)) {
            mobileDataEnabled = true;
        }
        return mobileDataEnabled;
    }

    private String getMobileDataType() {
        return this.getProperty(this.commands.get("PROP_GSM_NETWORK_TYPE"));
    }

    private String getDataFromShellCommand(String cmdName, String cmd) {
        try {
            String result = null;
            if (cmd != null) {
                LOG.info("Getting '{}' for device: {}", cmdName, this.adbDevice.getSerialNumber());
                String cmdResult = this.adbExecutor.executeShellCommand(cmd);
                if (cmdResult != null && !cmdResult.trim().isBlank()) {
                    result = cmdResult.trim();
                }
                LOG.debug("Result of command '{}' for device '{}, Result: '{}'", cmdName, this.adbDevice.getSerialNumber(), result);
            }
            return result;
        } catch (TimeoutException | AdbCommandRejectedException | ShellCommandUnresponsiveException | IOException e) {
            LOG.debug("Error getting '{}' for device: {}", cmdName, this.adbDevice.getSerialNumber(), e);
            return null;
        }
    }

    private String getProperty(String property) {
        LOG.debug("Getting property '{}' for device: {}", property, this.adbDevice.getSerialNumber());
//            String propResult = this.adbDevice.getSystemProperty(property).get();
		String propResult = this.adbDevice.getProperty(property);
		LOG.trace("Result of property '{}' for device '{}, Result: '{}'", property, this.adbDevice.getSerialNumber(), propResult);
		return (propResult == null || propResult.isBlank()) ? null : propResult;
    }
}

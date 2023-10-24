package com.github.cornerstonews.adb;

public class DeviceDO {

    private String serialNumber;
    private String androidVersion;
    private int apiLevel;
    private String manufacturer;
    private String model;
    private String product;
    private boolean isOnline;
    private String state;
    private Integer batteryLevel;
    private String batteryHealth;
    private Double batteryTemperature;
    private boolean isWifiEnabled;
    private boolean isSimPresent = false;
    private String ICCID;
    private String IMSI;
    private String IMEI;
    private String phoneNumber;
    private String simOperator;
    private Integer rssi;
    private boolean isMobileDataEnabled;
    private String mobileDataType;
    private boolean isAirplaneModeOn;
    private boolean isBluetoothOn;
    private boolean isNfcOn;

    public DeviceDO() {
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getAndroidVersion() {
        return androidVersion;
    }

    public void setAndroidVersion(String androidVersion) {
        this.androidVersion = androidVersion;
    }

    public int getApiLevel() {
        return apiLevel;
    }

    public void setApiLevel(int apiLevel) {
        this.apiLevel = apiLevel;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getProduct() {
        return product;
    }

    public void setProduct(String product) {
        this.product = product;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public Integer getBatteryLevel() {
        return batteryLevel;
    }

    public void setBatteryLevel(Integer batteryLevel) {
        this.batteryLevel = batteryLevel;
    }

    public String getBatteryHealth() {
        return batteryHealth;
    }

    public void setBatteryHealth(String batteryHealth) {
        this.batteryHealth = batteryHealth;
    }

    public Double getBatteryTemperature() {
        return batteryTemperature;
    }

    public void setBatteryTemperature(Double batteryTemperature) {
        this.batteryTemperature = batteryTemperature;
    }

    public boolean isWifiEnabled() {
        return isWifiEnabled;
    }

    public void setWifiEnabled(boolean isWifiEnabled) {
        this.isWifiEnabled = isWifiEnabled;
    }

    public boolean isSimPresent() {
        return isSimPresent;
    }

    public void setSimPresent(boolean isSimPresent) {
        this.isSimPresent = isSimPresent;
    }

    public String getICCID() {
        return ICCID;
    }

    public void setICCID(String iCCID) {
        ICCID = iCCID;
    }

    public String getIMSI() {
        return IMSI;
    }

    public void setIMSI(String iMSI) {
        IMSI = iMSI;
    }

    public String getIMEI() {
        return IMEI;
    }

    public void setIMEI(String iMEI) {
        IMEI = iMEI;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getSimOperator() {
        return simOperator;
    }

    public void setSimOperator(String simOperator) {
        this.simOperator = simOperator;
    }

    public Integer getRssi() {
        return rssi;
    }

    public void setRssi(Integer rssi) {
        this.rssi = rssi;
    }

    public boolean isMobileDataEnabled() {
        return isMobileDataEnabled;
    }

    public void setMobileDataEnabled(boolean isMobileDataEnabled) {
        this.isMobileDataEnabled = isMobileDataEnabled;
    }

    public String getMobileDataType() {
        return mobileDataType;
    }

    public void setMobileDataType(String mobileDataType) {
        this.mobileDataType = mobileDataType;
    }

    public boolean isAirplaneModeOn() {
        return isAirplaneModeOn;
    }

    public void setAirplaneModeOn(boolean isAirplaneModeOn) {
        this.isAirplaneModeOn = isAirplaneModeOn;
    }

    public boolean isBluetoothOn() {
        return isBluetoothOn;
    }

    public void setBluetoothOn(boolean isBluetoothOn) {
        this.isBluetoothOn = isBluetoothOn;
    }

    public boolean isNfcOn() {
        return isNfcOn;
    }

    public void setNfcOn(boolean isNfcOn) {
        this.isNfcOn = isNfcOn;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\", \"androidVersion\":\"");
        builder.append(androidVersion);
        builder.append("\", \"apiLevel\":\"");
        builder.append(apiLevel);
        builder.append("\", \"manufacturer\":\"");
        builder.append(manufacturer);
        builder.append("\", \"model\":\"");
        builder.append(model);
        builder.append("\", \"product\":\"");
        builder.append(product);
        builder.append("\", \"isOnline\":\"");
        builder.append(isOnline);
        builder.append("\", \"state\":\"");
        builder.append(state);
        builder.append("\", \"batteryLevel\":\"");
        builder.append(batteryLevel);
        builder.append("\", \"batteryHealth\":\"");
        builder.append(batteryHealth);
        builder.append("\", \"batteryTemperature\":\"");
        builder.append(batteryTemperature);
        builder.append("\", \"isWifiEnabled\":\"");
        builder.append(isWifiEnabled);
        builder.append("\", \"isSimPresent\":\"");
        builder.append(isSimPresent);
        builder.append("\", \"simOperator\":\"");
        builder.append(simOperator);
        builder.append("\", \"rssi\":\"");
        builder.append(rssi);
        builder.append("\", \"isMobileDataEnabled\":\"");
        builder.append(isMobileDataEnabled);
        builder.append("\", \"mobileDataType\":\"");
        builder.append(mobileDataType);
        builder.append("\", \"isAirplaneModeOn\":\"");
        builder.append(isAirplaneModeOn);
        builder.append("\", \"isBluetoothOn\":\"");
        builder.append(isBluetoothOn);
        builder.append("\", \"isNfcOn\":\"");
        builder.append(isNfcOn);
        builder.append("\"}");
        return builder.toString();
    }   
    
    
}

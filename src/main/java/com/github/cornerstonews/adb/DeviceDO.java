package com.github.cornerstonews.adb;

public class DeviceDO {

    private String serialNumber;
    private String androidVersion;
    private String manufacturer;
    private String model;
    private String product;
    private Boolean isOnline;
    private String state;
    private Integer batteryLevel;
    private String batteryHealth;
    private Double batteryTemperature;
    private String network;
    private Boolean wifiEnabled;
    private Boolean isSimPresent = false;
    private String ICCID;
    private String IMSI;
    private String IMEI;
    private String phoneNumber;
    private String simOperator;
    private Integer rssi;
    private Boolean mobileDataEnabled;
    private String mobileDataType;

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

    public Boolean getIsOnline() {
        return isOnline;
    }

    public void setIsOnline(Boolean isOnline) {
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

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }

    public Boolean getWifiEnabled() {
        return wifiEnabled;
    }

    public void setWifiEnabled(Boolean wifiEnabled) {
        this.wifiEnabled = wifiEnabled;
    }

    public Boolean getIsSimPresent() {
        return isSimPresent;
    }

    public void setIsSimPresent(Boolean isSimPresent) {
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

    public Boolean getMobileDataEnabled() {
        return mobileDataEnabled;
    }

    public void setMobileDataEnabled(Boolean mobileDataEnabled) {
        this.mobileDataEnabled = mobileDataEnabled;
    }

    public String getMobileDataType() {
        return mobileDataType;
    }

    public void setMobileDataType(String mobileDataType) {
        this.mobileDataType = mobileDataType;
    }
}

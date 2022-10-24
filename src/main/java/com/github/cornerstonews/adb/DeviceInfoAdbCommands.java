package com.github.cornerstonews.adb;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DeviceInfoAdbCommands {

    // https://android.googlesource.com/platform/tools/base/+/master/ddmlib
    //
    // How to use use service call command
    // https://android.stackexchange.com/questions/34625/where-to-find-description-of-all-system-bin-service-calls
    // https://android.googlesource.com/platform/frameworks/base/+/master/telephony/java/com/android/internal/telephony/IPhoneSubInfo.aidl
    // https://android.googlesource.com/platform/frameworks/opt/telephony/+/master/src/java/com/android/internal/telephony/PhoneSubInfoController.java
    //
    // https://developer.android.com/reference/android/view/KeyEvent.html
    // https://gist.github.com/Pulimet/5013acf2cd5b28e55036c82c91bd56d8
    // https://stackoverflow.com/questions/68848115/read-imsi-information-via-adb-on-android-12
    // https://gist.github.com/eladkarako/dbcaeae14e2fff9f72b275554762105e
    
    public static final String PROP_DEVICE_NAME = "ro.product.name";
    public static final String CMD_DIALER_DEVICE_INFO = "SCREEN_STATE=$(dumpsys power | grep 'mHolding'); "
            + "DISPLAY_STATUS=$(echo \"$SCREEN_STATE\" | grep mHoldingDisplaySuspendBlocker | cut -d '=' -f 2); "
            + "PHONE_LOCKED=$(dumpsys window | grep mDreamingLockscreen | tr -s ' ' | cut -d ' ' -f 3 | cut -d '=' -f 2); "
            + "if [[ \"$DISPLAY_STATUS\" == \"false\" ]]; then input keyevent 26; sleep 0.2; fi; "
            + "if [[ \"$PHONE_LOCKED\" == *\"true\"* ]]; then input keyevent 82; sleep 0.5; fi; "
            + "input keyevent 3; "
            + "service call phone 1 s16 '*#06#' && sleep 1 && input text '#06#'; "
            + "uiautomator dump /sdcard/device-info.xml; am force-stop com.google.android.dialer;";

    // Android 12L & 13 (API 32 & 33)
    private static Map<String, String> commands = new HashMap<String, String>() {
        {
            put("PROP_PRODUCT_DEVICE", "ro.product.product.device");
            put("PROP_SIM_STATE", "gsm.sim.state");
            put("PROP_SIM_OPERATOR", "gsm.operator.alpha");
            put("PROP_GSM_NETWORK_TYPE", "gsm.network.type");
            put("CMD_GET_IMEI", "imei=$(cat /sdcard/device-info.xml | sed s/\\>\\<\\/\\\\n/g | grep -A1 IMEI | tail -n1); echo ${imei} | awk -F 'text=\"' '{print $2}' | cut -c1-16");
//            put("CMD_GET_IMSI", "service call iphonesubinfo 9 | cut -c 52-66 | tr -d '.[:space:]'");
            put("CMD_GET_ICCID", "service call iphonesubinfo 13 | cut -c 52-66 | tr -d '.[:space:]'");
            put("CMD_GET_NUMBER", "service call iphonesubinfo 15 | cut -c 52-66 | tr -d '.[:space:]'");
            put("CMD_GET_WIFI_ON", "settings get global wifi_on");
            put("CMD_GET_MOBILE_DATA", "settings get global mobile_data");
            put("CMD_GET_AIRPLANE_MODE", "settings get global airplane_mode_on");
            put("CMD_GET_BLUETOOTH_ON", "settings get global bluetooth_on");
            put("CMD_GET_NFC_STATUS", "if dumpsys nfc | grep 'mState='; then echo $nfcdumpsys | grep 'mState='; return; else dumpsys nfc | grep '^State: '; fi");
        }
    };

    // Android 12
    // IMSI broke starting with this version
    private static Map<String, String> commands_API_31 = new HashMap<String, String>() {
        {
            put("CMD_GET_IMEI", "service call iphonesubinfo 2 | cut -c 52-66 | tr -d '.[:space:]'");
//            put("CMD_GET_IMSI", "service call iphonesubinfo 9 | cut -c 52-66 | tr -d '.[:space:]'");
            // getIccSerialNumberWithFeature()
            put("CMD_GET_ICCID", "service call iphonesubinfo 13 | cut -c 52-66 | tr -d '.[:space:]'");
            put("CMD_GET_NUMBER", "service call iphonesubinfo 15 | cut -c 52-66 | tr -d '.[:space:]'");
        }
    };

    // Android 11
    private static Map<String, String> commands_API_30 = new HashMap<String, String>() {
        {
            put("CMD_GET_IMEI", "service call iphonesubinfo 2 | cut -c 52-66 | tr -d '.[:space:]'");
            put("CMD_GET_IMSI", "service call iphonesubinfo 9 | cut -c 52-66 | tr -d '.[:space:]'");
            // getIccSerialNumberWithFeature()
            put("CMD_GET_ICCID", "service call iphonesubinfo 13 | cut -c 52-66 | tr -d '.[:space:]'");
            put("CMD_GET_NUMBER", "service call iphonesubinfo 15 | cut -c 52-66 | tr -d '.[:space:]'");
        }
    };
    
    // Android 9 & 10
    private static Map<String, String> commands_API_28_TO_29 = new HashMap<String, String>() {
        {
            put("CMD_GET_IMEI", "service call iphonesubinfo 1 | cut -c 52-66 | tr -d '.[:space:]'");
            put("CMD_GET_IMSI", "service call iphonesubinfo 7 | cut -c 52-66 | tr -d '.[:space:]'");
            // getIccSerialNumber() or getIccSerialNumberForSubscriber()
            put("CMD_GET_ICCID", "service call iphonesubinfo 11 | cut -c 52-66 | tr -d '.[:space:]'");
            put("CMD_GET_NUMBER", "service call iphonesubinfo 13 | cut -c 52-66 | tr -d '.[:space:]'");
        }
    };

    // Android 8.0.0 & 8.1.0
    private static Map<String, String> commands_API_26_TO_27 = new HashMap<String, String>() {
        {
            put("CMD_GET_IMEI", "service call iphonesubinfo 1 | cut -c 52-66 | tr -d '.[:space:]'");
            put("CMD_GET_IMSI", "service call iphonesubinfo 7 | cut -c 52-66 | tr -d '.[:space:]'");
            put("CMD_GET_ICCID", "service call iphonesubinfo 11 | cut -c 52-66 | tr -d '.[:space:]'");
            put("CMD_GET_NUMBER", "service call iphonesubinfo 14 | cut -c 52-66 | tr -d '.[:space:]'");
        }
    };

    
    
    public static Map<String, String> getCommands(int apiLevel) {
        Map<String, String> override;

        switch (apiLevel) {
        default:
            override = commands;
            break;

        case 31:
            override = commands_API_31;
            break;

        case 30:
            override = commands_API_30;
            break;

        case 29:
        case 28:
            override = commands_API_28_TO_29;
            break;

        case 27:
        case 26:
            override = commands_API_26_TO_27;
            break;
        }

//        return Stream.concat(commands.entrySet().stream(), override.entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        return Stream.of(commands, override).flatMap(map -> map.entrySet().stream())
                .collect(Collectors.toMap(Map.Entry::getKey, e -> e.getValue(), (m1, m2) -> {
                    return m2;
                }));
    }
}
package com.github.cornerstonews.adb;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.android.SdkConstants;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;

public class AdbManager {

    private static final Logger LOG = LogManager.getLogger(AdbManager.class);

    private String adbPath;
    private static final Object ADB_INIT_LOCK = new Object();
    private AndroidDebugBridge bridge;
    private boolean bridgeCreated = false;

    public AdbManager() throws FileNotFoundException {
        this(null);
    }

    public AdbManager(String adbPath) throws FileNotFoundException {
        if (adbPath != null) {
            File adb = new File(adbPath, SdkConstants.FN_ADB);
            if (adb == null || !adb.exists()) {
                throw new FileNotFoundException("Adb not found at path '" + adbPath + "'");
            }
            this.adbPath = adb.getAbsolutePath();
        } else {
            this.adbPath = "adb";
        }
    }

    public void shutdown() {
        if (bridge != null) {
            AndroidDebugBridge.terminate();
            if (this.bridgeCreated) {
                AndroidDebugBridge.disconnectBridge();
            }
        }
    }

    private AndroidDebugBridge getBridge() {
        bridge = AndroidDebugBridge.getBridge();
        if (bridge == null || !bridge.isConnected()) {
            synchronized (ADB_INIT_LOCK) {
                boolean clientSupport = false;
                AndroidDebugBridge.initIfNeeded(clientSupport);
                LOG.info("Initializing adb using: '{}', client support = {}", adbPath, clientSupport);
                bridge = AndroidDebugBridge.createBridge(this.adbPath, false);
                this.bridgeCreated = true;
            }
            while (bridge == null || !bridge.isConnected()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(200);
                } catch (InterruptedException e) {
                    // if cancelled, don't wait for connection and return immediately
                    return bridge;
                }
            }
        }

        LOG.debug("Successfully connected to adb");
        return bridge;
    }

    private <T> T executeAdbCommand(AdbTransaction<T> transaction) {
        return transaction.execute(getBridge());
    }

    private interface AdbTransaction<T> {
        public T execute(AndroidDebugBridge bridge);
    }

    public List<AdbExecutor> getDevices() {
        return new ArrayList<AdbExecutor>(getFilteredDevices(null).values());
    }

    public AdbExecutor getDevice(String deviceSerial) {
        Map<String, AdbExecutor> devices = getFilteredDevices(Arrays.asList(deviceSerial));
        if (devices == null || devices.isEmpty()) {
            return null;
        }

        return devices.get(deviceSerial);
    }

    public Map<String, AdbExecutor> getFilteredDevices(Collection<String> deviceFilter) {
        return executeAdbCommand((AndroidDebugBridge bridge) -> {
            final Map<String, AdbExecutor> filteredDevices = new HashMap<String, AdbExecutor>();
            for (IDevice device : bridge.getDevices()) {
                if (deviceFilter == null || deviceFilter.contains(device.getSerialNumber())) {
                    filteredDevices.put(device.getSerialNumber(), new AdbExecutor(device.getSerialNumber(), device));
                }
            }
            return filteredDevices;
        });
    }
}
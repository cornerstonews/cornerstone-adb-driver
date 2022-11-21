package com.github.cornerstonews.adb;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.android.SdkConstants;
import com.android.annotations.NonNull;
import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.AndroidDebugBridge.IDeviceChangeListener;
import com.google.common.collect.Sets;
import com.android.ddmlib.IDevice;

public class AdbManager {

    private static final Logger LOG = LogManager.getLogger(AdbManager.class);

    private String adbPath;
    private static final Object ADB_INIT_LOCK = new Object();
    private AndroidDebugBridge bridge;
    private boolean bridgeCreated = false;
    private ConcurrentHashMap<String, AdbExecutor> devices;

    private final Set<IDeviceStatusListener> deviceStatusListeners = Sets.newCopyOnWriteArraySet();
    
    public AdbManager() throws FileNotFoundException, CornerstoneADBException {
        this((String) null);
    }

    public AdbManager(AndroidDebugBridge bridge) {
        this.bridge = bridge;
    }
    
    public AdbManager(String adbPath) throws FileNotFoundException, CornerstoneADBException {
        if (adbPath != null) {
            File adb = new File(adbPath, SdkConstants.FN_ADB);
            if (adb == null || !adb.exists()) {
                throw new FileNotFoundException("Adb not found at path '" + adbPath + "'");
            }
            this.adbPath = adb.getAbsolutePath();
        } else {
            this.adbPath = "adb";
        }

        this.getBridge();
        AndroidDebugBridge.addDeviceChangeListener(getDeviceChangeListener());
    }

    public void shutdown() {
        if (this.bridge != null) {
            AndroidDebugBridge.terminate();
            if (this.bridgeCreated) {
                AndroidDebugBridge.disconnectBridge(120L, TimeUnit.SECONDS);
            }
        }
    }
    
    private AndroidDebugBridge getBridge() throws CornerstoneADBException {
        bridge = AndroidDebugBridge.getBridge();
        if (bridge == null || !bridge.isConnected()) {
            synchronized (ADB_INIT_LOCK) {
                if (bridge == null || !bridge.isConnected()) {
                    boolean clientSupport = false;
                    AndroidDebugBridge.init(clientSupport);
                    LOG.info("Initializing adb using: '{}', client support = {}", adbPath, clientSupport);
                    bridge = AndroidDebugBridge.createBridge(this.adbPath, /* forceNewBridge */ false, 120L, TimeUnit.SECONDS);
                    waitUntilAction(() -> bridge.isConnected(), 20000, 1000, "Waiting for ADB bridge connection...");

                    waitUntilAction(() -> this.bridge.hasInitialDeviceList(), 30000, 1000, "Getting initial device list...");
                    this.getDevices();
                    LOG.info("Found '{}' initial connected devices.", devices.size());
                    this.devices.values().forEach(device -> LOG.info(" |--> Serial: '{}'", device.getDeviceSerial()));
                    this.bridgeCreated = true;
                }
            }
        }

        LOG.debug("Successfully acquired adb connection.");
        return bridge;
    }

    private interface Transaction {
        public boolean execute();
    }

    private static void waitUntilAction(Transaction t, long timeOut, int sleepTime, String message) throws CornerstoneADBException {
//        long timeOut = 30000; // 30 sec
//        int sleepTime = 1000;
        while (!t.execute() && timeOut > 0) {
            try {
                LOG.info(message);
                Thread.sleep(sleepTime);
            } catch (InterruptedException e) {
                String error = "InterruptedException caught during action: " + message;
                LOG.error(error, e);
                throw new CornerstoneADBException(error);
            }
            timeOut -= sleepTime;
        }

        if (timeOut <= 0 && !t.execute()) {
            throw new CornerstoneADBException("TimeoutException: Timeout reached during action: " + message);
        }
    }
    
    public interface IDeviceStatusListener {
        void deviceStatusChanged(@NonNull DeviceDO device);
    }
    
    public void addDeviceStatusListener(@NonNull IDeviceStatusListener listener) {
        deviceStatusListeners.add(listener);
    }

    public void removeDeviceStatusListener(IDeviceStatusListener listener) {
        deviceStatusListeners.remove(listener);
    }

    private void updateDeviceStatusListener(AdbExecutor deviceExecutor) {
        for (IDeviceStatusListener listener : deviceStatusListeners) {
            try {
                listener.deviceStatusChanged(deviceExecutor.getDeviceInfo());
            } catch (Exception e) {
                // Catch possible exception thrown by listeners
                LOG.debug("Execption thrown by device status listener");
            }
        }
    }
    
    private IDeviceChangeListener getDeviceChangeListener() {
        return new IDeviceChangeListener() {

            @Override
            public void deviceDisconnected(IDevice device) {
                LOG.info("Device with serial '{}' disconnected.", device.getSerialNumber());
                AdbExecutor removedDevice = devices.remove(device.getSerialNumber());
                updateDeviceStatusListener(removedDevice);
            }

            @Override
            public void deviceConnected(IDevice device) {
                LOG.info("Device with serial '{}' connected.", device.getSerialNumber());
                AdbExecutor deviceAdbExecutor = new AdbExecutor(device.getSerialNumber(), device);
                deviceAdbExecutor.getDeviceInfo();
                devices.put(device.getSerialNumber(), deviceAdbExecutor);
                updateDeviceStatusListener(deviceAdbExecutor);
            }

            @Override
            public void deviceChanged(IDevice device, int changeMask) {
                LOG.debug("Changed device event detected on Device with serial '{}'. ChangeMask: {}", device.getSerialNumber(), changeMask);
                if (IDevice.CHANGE_STATE == changeMask && device.isOnline()) {
                    LOG.info("Device '{}' state changed to 'ONLINE'", device.getSerialNumber());
                    AdbExecutor deviceAdbExecutor = new AdbExecutor(device.getSerialNumber(), device);
                    deviceAdbExecutor.getDeviceInfo();
                    devices.put(device.getSerialNumber(), deviceAdbExecutor);
                    updateDeviceStatusListener(deviceAdbExecutor);
                }
            }
        };
    }

    public List<AdbExecutor> getDevices() throws CornerstoneADBException {
        if (devices == null || devices.isEmpty()) {
            this.devices = this.getFilteredDevices(null);
        }

        return new ArrayList<AdbExecutor>(this.devices.values());
    }

    public AdbExecutor getDevice(String deviceSerial) throws CornerstoneADBException {
        Optional<AdbExecutor> foundDevice = this.devices.values().stream().filter(device -> device.getDeviceSerial().equals(deviceSerial)).findFirst();
        if (foundDevice.isPresent()) {
            return foundDevice.get();
        }

        Map<String, AdbExecutor> filteredDevice = getFilteredDevices(Arrays.asList(deviceSerial));
        if (devices == null || devices.isEmpty()) {
            return null;
        }

        this.devices.putAll(filteredDevice);
        return this.devices.get(deviceSerial);
    }

    private ConcurrentHashMap<String, AdbExecutor> getFilteredDevices(Collection<String> deviceFilter) throws CornerstoneADBException {
        final ConcurrentHashMap<String, AdbExecutor> filteredDevices = new ConcurrentHashMap<String, AdbExecutor>();
//        for (IDevice device : bridge.getDevices()) {
//            if (deviceFilter == null || deviceFilter.contains(device.getSerialNumber())) {
//                filteredDevices.put(device.getSerialNumber(), new AdbExecutor(device.getSerialNumber(), device));
//            }
//        }

        Stream.of(this.getBridge().getDevices())
                .filter(device -> (deviceFilter == null || deviceFilter.contains(device.getSerialNumber())))
                .parallel()
                .forEach((device) -> {
                    LOG.debug("**** Processing '{}' in {}", device.getSerialNumber(), Thread.currentThread());
                    AdbExecutor deviceAdbExecutor = new AdbExecutor(device.getSerialNumber(), device);
                    deviceAdbExecutor.getDeviceInfo();
                    filteredDevices.put(device.getSerialNumber(), deviceAdbExecutor);
                    LOG.debug("**** Finished Processing '{}' in {}", device.getSerialNumber(), Thread.currentThread());
                });

        return filteredDevices;
    }

}
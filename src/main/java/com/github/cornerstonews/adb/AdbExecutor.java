package com.github.cornerstonews.adb;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.FileListingService.FileEntry;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;

public class AdbExecutor {

    private static final Logger LOG = LogManager.getLogger(AdbExecutor.class);

    private String deviceSerial;
    private IDevice adbDevice;
    private DeviceInfo deviceInfo;

    public AdbExecutor(String deviceSerial, IDevice adbDevice) {
        this.deviceSerial = deviceSerial;
        this.adbDevice = adbDevice;
        this.deviceInfo = new DeviceInfo(this, adbDevice);
    }

    public String getDeviceSerial() {
        return deviceSerial;
    }

    public DeviceDO getDeviceInfo() {
        return this.deviceInfo.getInfo();
    }

    public Boolean isOnline() {
        return this.deviceInfo.isOnline();
    }

    // -----------------------------------------------------------------------
    // File operations
    // -----------------------------------------------------------------------
    private FileEntry getRoot() {
        FileEntry root = this.adbDevice.getFileListingService().getRoot();
        this.adbDevice.getFileListingService().getChildren(root, false, null);
        return root;
    }

    public FileNode getPath(String searchPath, boolean fetchChildren) {
        String[] pathSegments = Paths.get(searchPath).toString().split(File.separator);
        FileEntry path = getRoot();

        int pathSegmentIndex = 0;
        while (pathSegmentIndex < pathSegments.length) {
            String pathName = pathSegments[pathSegmentIndex++];
            if (pathName == null || pathName.isEmpty()) {
                continue;
            }
            path = path.findChild(pathName);

            if (path.getCachedChildren().length == 0) {
                this.adbDevice.getFileListingService().getChildren(path, false, null);
            }
        }

        if (fetchChildren) {
            recursivePopulatePath(path);
        }

        return FileNodeConverter.convert(path);
    }

    private void recursivePopulatePath(FileEntry pathEntry) {
        this.adbDevice.getFileListingService().getChildren(pathEntry, false, null);

        for (FileEntry entry : pathEntry.getCachedChildren()) {
            if (entry.isDirectory()) {
                recursivePopulatePath(entry);
            }
        }
    }

    public void pullFile(String remote, String local) throws SyncException, IOException, AdbCommandRejectedException, TimeoutException {
        this.adbDevice.pullFile(remote, local);
    }

    public void pushFile(String local, String remote) throws SyncException, IOException, AdbCommandRejectedException, TimeoutException {
        this.adbDevice.pushFile(local, remote);
        String MEDIA_SCAN_COMMAND = String.format("am broadcast -a android.intent.action.MEDIA_SCANNER_SCAN_FILE -d file:%s", remote);
        try {
            String result = executeShellCommand(MEDIA_SCAN_COMMAND);
            LOG.debug(result);
        } catch (ShellCommandUnresponsiveException e) {
            LOG.warn(e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Package operations
    // -----------------------------------------------------------------------
    public void installPackage(String packagePath, boolean reinstall) throws InstallException {
        LOG.info("Installing package: '{}'", packagePath);
        LOG.trace("Installing package: '{}' on device: '{}' reinstall: '{}'", packagePath, this.getDeviceSerial(), reinstall);
        this.adbDevice.installRemotePackage(packagePath, reinstall);
    }

    public void uninstallPackage(String packageName) throws InstallException {
        LOG.info("Uninstalling package: '{}'", packageName);
        LOG.trace("Uninstalling package: '{}' from device: '{}'", packageName, this.getDeviceSerial());
        this.adbDevice.uninstallPackage(packageName);
    }

    // -----------------------------------------------------------------------
    // RAW Commands
    // -----------------------------------------------------------------------
    public String executeShellCommand(String command) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        LOG.info("Executing shell command: '{}'", command);
        LOG.trace("Executing shell command on device: '{}', command: '{}'", this.getDeviceSerial(), command);
        final StringBuilder commandOutput = new StringBuilder();
        this.adbDevice.executeShellCommand(command, new MultiLineReceiver() {

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public void processNewLines(String[] lines) {
                Arrays.stream(lines).forEach(line -> commandOutput.append(line).append(System.getProperty("line.separator")));
            }
        });

        return commandOutput.length() == 0 ? null : commandOutput.toString();
    }

    public void reboot() throws TimeoutException, AdbCommandRejectedException, IOException {
        LOG.info("Rebooting device");
        LOG.trace("Rebooting device: '{}'", this.getDeviceSerial());
        this.adbDevice.reboot(null);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{\"deviceSerial\":\"");
        builder.append(deviceSerial);
        builder.append("\"}");
        return builder.toString();
    }
}

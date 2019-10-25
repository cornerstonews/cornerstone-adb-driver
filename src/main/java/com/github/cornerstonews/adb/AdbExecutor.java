package com.github.cornerstonews.adb;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

import com.android.ddmlib.AdbCommandRejectedException;
import com.android.ddmlib.FileListingService.FileEntry;
import com.android.ddmlib.IDevice;
import com.android.ddmlib.InstallException;
import com.android.ddmlib.MultiLineReceiver;
import com.android.ddmlib.ShellCommandUnresponsiveException;
import com.android.ddmlib.SyncException;
import com.android.ddmlib.TimeoutException;

public class AdbExecutor {

    private String deviceSerial;
    private IDevice device;

    public AdbExecutor(String deviceSerial, IDevice device) {
        this.deviceSerial = deviceSerial;
        this.device = device;
    }

    public String getDeviceSerial() {
        return deviceSerial;
    }

    private FileEntry getRoot() {
        FileEntry root = this.device.getFileListingService().getRoot();
        this.device.getFileListingService().getChildren(root, false, null);
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
                this.device.getFileListingService().getChildren(path, false, null);
            }
        }

        if (fetchChildren) {
            recursivePopulatePath(path);
        }

        return FileNodeConverter.convert(path);
    }

    private void recursivePopulatePath(FileEntry pathEntry) {
        this.device.getFileListingService().getChildren(pathEntry, false, null);

        for (FileEntry entry : pathEntry.getCachedChildren()) {
            if (entry.isDirectory()) {
                recursivePopulatePath(entry);
            }
        }
    }

    public void pullFile(String remote, String local) throws SyncException, IOException, AdbCommandRejectedException, TimeoutException {
        this.device.pullFile(remote, local);
    }

    public void pushFile(String local, String remote) throws SyncException, IOException, AdbCommandRejectedException, TimeoutException {
        this.device.pushFile(local, remote);
    }

    public void installPackage(String packagePath, boolean reinstall) throws InstallException {
        this.device.installRemotePackage(packagePath, reinstall);
    }

    public void uninstallPackage(String packageName) throws InstallException {
        this.device.uninstallPackage(packageName);
    }
    
    public boolean isOnline() {
        return this.device.isOnline();
    }

    public boolean isOffline() {
        return this.device.isOffline();
    }

    public void reboot() throws TimeoutException, AdbCommandRejectedException, IOException {
        this.device.reboot(null);
    }
    
    public String executeShellCommand(String command) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        final StringBuilder commandOutput = new StringBuilder();;
        this.device.executeShellCommand(command, new MultiLineReceiver() {
            
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
}

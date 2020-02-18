package com.github.cornerstonews.adb;

import java.util.List;

import javax.xml.bind.JAXBException;

import com.android.ddmlib.FileListingService.FileEntry;

public class AdbManagerTest {

    public static void main(String[] args) throws Exception {
        AdbManager manager = null;;
        try {
            manager = new AdbManager("/Applications/adb/platform-tools_r29.0.1");
            List<AdbExecutor> devices = manager.getDevices();
            System.out.println("Found devices: " + devices);
    
            AdbExecutor adbExecutor = devices.get(0);
            FileNode node = adbExecutor.getPath("/sdcard/", true);
            AdbManagerTest.print(node);
    //		adbExecutor.printRoot();
            node = adbExecutor.getPath("/sdcard/Android/data/com.android.chrome/files/Download", false);
            AdbManagerTest.print(node);
    
    //        adbExecutor.pushFile("/Users/ketal.patel/Desktop/spider-man-mcu-venom-sony-disney.jpg", "/sdcard/Download/spider-man-mcu-venom-sony-disney.jpg");
    //        adbExecutor.pullFile("/sdcard/Download/spider-man-mcu-venom-sony-disney.jpg",
    //                "/Users/ketal.patel/Desktop/spider-man-mcu-venom-sony-disney-downloaded.jpg");
            
       } finally {
           if(manager != null) {
               manager.shutdown();
           }
       }
    }

    public static void print(FileNode node) throws JAXBException {
        System.out.println(node);
    }
    
    public static void print(FileEntry node) {
        if (node.isDirectory()) {
            System.out.println(node.getFullPath() + (node.isRoot() ? "" : "/"));
            FileEntry[] children = node.getCachedChildren();
            for (FileEntry entry : children) {
                if (!entry.isDirectory()) {
                    System.out.println(entry.getFullPath());
                }
            }
            for (FileEntry entry : children) {
                if (entry.isDirectory()) {
                    print(entry);
                }
            }
        } else {
            System.out.println(node.getFullPath());
        }
    }

}
